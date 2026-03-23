package com.example.app.infrastructure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.app.domain.FloodArea;
import com.example.app.domain.area.GeoPoint;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class FloodGeoJsonLoader {

	/*********************************************************
	 * flood_risk_tokyo.geojson 読み込みクラス
	 * ・GeoJSON ⇒ List<FloodArea> に変換
	 * ・起動時に1回だけ読み込み、キャッシュして再利用
	 * ・ストリーミング解析でメモリ消費を最小化
	 *
	 * 地震側の EarthquakeCsvLoader に相当
	 ********************************************************/

	// GeoJSONファイルパス
	private static final String GEOJSON_PATH = "static/geo/flood_risk_tokyo.geojson";
	private static final ObjectMapper mapper = new ObjectMapper();
	// キャッシュ（初回load時に読み込み、以降は再利用）
	private List<FloodArea> cache = null;

	/**
	 * GeoJSONを読み込んでFloodAreaのリストを返す
	 * ※2回目以降はキャッシュから返却
	 */
	public List<FloodArea> load() {
		if (cache != null) {
			return cache;
		}
		List<FloodArea> list = new ArrayList<>();
		try (InputStream is = new ClassPathResource(GEOJSON_PATH).getInputStream();
			 JsonParser parser = mapper.createParser(is)) {

			// ルートオブジェクトの開始 "{"
			expectToken(parser, JsonToken.START_OBJECT);

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = parser.currentName();
				parser.nextToken(); // フィールド値へ移動

				if ("features".equals(fieldName)) {
					// features配列を1件ずつ処理
					parseFeatures(parser, list);
				} else {
					// features以外のフィールド（type等）はスキップ
					parser.skipChildren();
				}
			}

			log.info("洪水GeoJSON読み込み完了: {}件", list.size());
		} catch (Exception e) {
			throw new RuntimeException("洪水GeoJSONの読み込みに失敗しました", e);
		}
		cache = list;
		return cache;
	}

	/**
	 * features配列を1件ずつストリーミング解析
	 */
	private void parseFeatures(JsonParser parser, List<FloodArea> list) throws Exception {
		// "[" の確認
		expectToken(parser, JsonToken.START_ARRAY);

		while (parser.nextToken() != JsonToken.END_ARRAY) {
			// 各feature "{" から始まる
			if (parser.currentToken() == JsonToken.START_OBJECT) {
				parseFeature(parser, list);
			}
		}
	}

	/**
	 * feature1件をストリーミング解析してlistへ追加
	 */
	private void parseFeature(JsonParser parser, List<FloodArea> list) throws Exception {
		String geometryType = null;
		int rank = -1;
		List<List<GeoPoint>> polygons = new ArrayList<>();

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = parser.currentName();
			parser.nextToken();

			switch (fieldName) {
				case "properties" -> rank = parseRank(parser);
				case "geometry"   -> {
					Object[] result = parseGeometry(parser);
					geometryType = (String) result[0];
					@SuppressWarnings("unchecked")
					List<List<GeoPoint>> parsed = (List<List<GeoPoint>>) result[1];
					polygons = parsed;
				}
				default -> parser.skipChildren();
			}
		}

		// rankが取得できていてポリゴンがある場合だけ追加
		if (rank >= 0 && !polygons.isEmpty()) {
			for (List<GeoPoint> polygon : polygons) {
				if (polygon.size() >= 3) {
					FloodArea area = new FloodArea();
					area.setPolygon(polygon);
					area.setRank(rank);
					list.add(area);
				}
			}
		} else if (rank < 0) {
			// propertiesにA31b_201がない場合はスキップ（正常動作）
		}
	}

	/**
	 * propertiesオブジェクトからA31b_201(rank)を取得
	 */
	private int parseRank(JsonParser parser) throws Exception {
		if (parser.currentToken() == JsonToken.VALUE_NULL) {
			return -1;
		}
		int rank = -1;
		// "{" は既にcurrentTokenで指している
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String key = parser.currentName();
			parser.nextToken();
			if ("A31b_201".equals(key)) {
				rank = parser.getIntValue();
			} else {
				parser.skipChildren();
			}
		}
		return rank;
	}

	/**
	 * geometryオブジェクトを解析し [type, List<List<GeoPoint>>] を返す
	 */
	private Object[] parseGeometry(JsonParser parser) throws Exception {
		if (parser.currentToken() == JsonToken.VALUE_NULL) {
			return new Object[]{"", new ArrayList<>()};
		}

		String type = "";
		List<List<GeoPoint>> polygons = new ArrayList<>();

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String key = parser.currentName();
			parser.nextToken();

			switch (key) {
				case "type"        -> type = parser.getText();
				case "coordinates" -> {
					if ("Polygon".equals(type)) {
						polygons.add(parsePolygonCoordinates(parser));
					} else if ("MultiPolygon".equals(type)) {
						polygons.addAll(parseMultiPolygonCoordinates(parser));
					} else {
						parser.skipChildren();
					}
				}
				default -> parser.skipChildren();
			}
		}
		return new Object[]{type, polygons};
	}

	/**
	 * Polygon座標配列 ⇒ List<GeoPoint>（外側リングのみ）
	 * GeoJSON構造: [ [ [lng,lat], ... ] ]
	 */
	private List<GeoPoint> parsePolygonCoordinates(JsonParser parser) throws Exception {
		List<GeoPoint> points = new ArrayList<>();
		boolean isOuterRing = true;

		// "[" (Polygon全体)
		expectToken(parser, JsonToken.START_ARRAY);
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			// 各リング "["
			if (parser.currentToken() == JsonToken.START_ARRAY) {
				if (isOuterRing) {
					points = parseRingCoordinates(parser);
					isOuterRing = false;
				} else {
					// 内側リング（穴）はスキップ
					parser.skipChildren();
				}
			}
		}
		return points;
	}

	/**
	 * MultiPolygon座標配列 ⇒ List<List<GeoPoint>>
	 * GeoJSON構造: [ [ [ [lng,lat], ... ] ] ]
	 */
	private List<List<GeoPoint>> parseMultiPolygonCoordinates(JsonParser parser) throws Exception {
		List<List<GeoPoint>> polygons = new ArrayList<>();

		// "[" (MultiPolygon全体)
		expectToken(parser, JsonToken.START_ARRAY);
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			// 各Polygon "[" → parsePolygonCoordinatesと同じ処理
			if (parser.currentToken() == JsonToken.START_ARRAY) {
				List<GeoPoint> poly = new ArrayList<>();
				boolean isOuterRing = true;
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					if (parser.currentToken() == JsonToken.START_ARRAY) {
						if (isOuterRing) {
							poly = parseRingCoordinates(parser);
							isOuterRing = false;
						} else {
							parser.skipChildren();
						}
					}
				}
				polygons.add(poly);
			}
		}
		return polygons;
	}

	/**
	 * リング（座標点の配列）⇒ List<GeoPoint>
	 * ※currentTokenはSTART_ARRAYの状態で呼ぶこと
	 * GeoJSON構造: [ [lng, lat], [lng, lat], ... ]
	 */
	private List<GeoPoint> parseRingCoordinates(JsonParser parser) throws Exception {
		List<GeoPoint> points = new ArrayList<>();
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			if (parser.currentToken() == JsonToken.START_ARRAY) {
				double lng = 0, lat = 0;
				int idx = 0;
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					double val = parser.getDoubleValue();
					if (idx == 0) lng = val;
					else if (idx == 1) lat = val;
					idx++;
				}
				points.add(new GeoPoint(lat, lng));
			}
		}
		return points;
	}

	/**
	 * 指定トークンであることを確認（違う場合は例外）
	 */
	private void expectToken(JsonParser parser, JsonToken expected) throws Exception {
		JsonToken actual = parser.currentToken() != null ? parser.currentToken() : parser.nextToken();
		if (actual != expected) {
			throw new RuntimeException(
				"GeoJSON parse error: expected " + expected + " but got " + actual
					+ " at " + parser.currentLocation());
		}
	}
}
