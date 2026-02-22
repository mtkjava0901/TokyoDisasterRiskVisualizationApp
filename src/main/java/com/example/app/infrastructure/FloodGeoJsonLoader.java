package com.example.app.infrastructure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.app.domain.FloodArea;
import com.example.app.domain.area.GeoPoint;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class FloodGeoJsonLoader {

	/*********************************************************
	 * flood_risk_tokyo.geojson 読み込みクラス
	 * ・GeoJSON ⇒ List<FloodArea> に変換
	 * ・起動時に1回だけ読み込み、キャッシュして再利用
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
		try (InputStream is = new ClassPathResource(GEOJSON_PATH).getInputStream()) {
			JsonNode root = mapper.readTree(is);
			JsonNode features = root.get("features");
			if (features == null || !features.isArray()) {
				throw new RuntimeException("Invalid GeoJSON: features is missing");
			}
			for (JsonNode feature : features) {
				// geometry取得
				JsonNode geometry = feature.get("geometry");
				if (geometry == null || geometry.isNull()) {
					continue;
				}
				// properties.A31b_201 取得
				JsonNode properties = feature.get("properties");
				if (properties == null) {
					continue;
				}
				JsonNode rankNode = properties.get("A31b_201");
				if (rankNode == null) {
					continue;
				}
				int rank = rankNode.asInt();
				// geometry.type / coordinates 取得
				String type = geometry.get("type").asText();
				JsonNode coordinates = geometry.get("coordinates");
				if ("Polygon".equals(type)) {
					// Polygon ⇒ 1つのFloodArea
					List<GeoPoint> polygon = parsePolygon(coordinates);
					if (polygon.size() >= 3) {
						FloodArea area = new FloodArea();
						area.setPolygon(polygon);
						area.setRank(rank);
						list.add(area);
					}
				} else if ("MultiPolygon".equals(type)) {
					// MultiPolygon ⇒ 各サブポリゴンごとにFloodAreaを生成
					for (JsonNode polygonNode : coordinates) {
						List<GeoPoint> polygon = parsePolygon(polygonNode);
						if (polygon.size() >= 3) {
							FloodArea area = new FloodArea();
							area.setPolygon(polygon);
							area.setRank(rank);
							list.add(area);
						}
					}
				} else {
					log.warn("Skip unsupported geometry type: {}", type);
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
	 * Polygon座標配列 ⇒ List<GeoPoint>
	 * GeoJSON: [[[lng, lat], [lng, lat], ...]]
	 * ※外側リング(index=0)のみ使用
	 */
	private List<GeoPoint> parsePolygon(JsonNode polygonNode) {
		JsonNode outerRing = polygonNode.get(0);
		List<GeoPoint> points = new ArrayList<>();
		for (JsonNode coord : outerRing) {
			double lng = coord.get(0).asDouble();
			double lat = coord.get(1).asDouble();
			points.add(new GeoPoint(lat, lng));
		}
		return points;
	}

}
