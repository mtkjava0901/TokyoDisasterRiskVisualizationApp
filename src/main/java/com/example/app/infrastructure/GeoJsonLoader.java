package com.example.app.infrastructure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.app.domain.area.GeoPoint;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class GeoJsonLoader {

	/************************************
	 * 
	 * GeoJSON 読み取りメソッド
	 * 
	 * GeoJSON ⇒ List<List<GeoPoint>>に変換する
	 * ※ List<List<GeoPoint>> = 複数ポリゴン(市区町村、断片化領域など)
	 ***********************************/

	private static final ObjectMapper mapper = new ObjectMapper();

	public static List<List<GeoPoint>> loadPolygons(InputStream is) {
		try {
			// InputStreamからGeoJSONを読み取る(JsonNodeに変換)
			JsonNode root = mapper.readTree(is);

			// featuresの存在チェック
			JsonNode features = root.get("features");
			if (features == null || !features.isArray()) {
				throw new IllegalArgumentException("Invalid GeoJSON: features is missing");
			}

			// 結果格納用リストを用意
			List<List<GeoPoint>> result = new ArrayList<>();

			// featureを1つずつ処理
			for (JsonNode feature : features) {
				// geometryがないfeatureはスキップ
				JsonNode geometry = feature.get("geometry");
				if (geometry == null || geometry.isNull()) {
					log.warn("Skip feature: geometry is missing");
					continue;
				}

				// geometry.type/coordinatesを取得
				JsonNode typeNode = geometry.get("type");
				JsonNode coordinates = geometry.get("coordinates");

				// Polygon/MultiPolygonの分岐
				if (typeNode == null || coordinates == null) {
					log.warn("Skip feature: invalid geometry {}", geometry);
					continue;
				}

				String type = typeNode.asText();

				if ("Polygon".equals(type)) {
					result.add(parsePolygon(coordinates));
				} else if ("MultiPolygon".equals(type)) {
					for (JsonNode polygonNode : coordinates) {
						result.add(parsePolygon(polygonNode));
					}
				} else {
					log.warn("Skip unsupported geometry type: {}", type);
				}
			}

			return result;

		} catch (Exception e) {
			throw new RuntimeException("Failed to load GeoJSON", e);
		}
	}

	// parsePolygon()の中身
	private static List<GeoPoint> parsePolygon(JsonNode polygonNode) {
		JsonNode outerRing = polygonNode.get(0);

		List<GeoPoint> points = new ArrayList<>();
		// 緯度･経度をGeoPointに変換
		for (JsonNode coord : outerRing) {
			double lng = coord.get(0).asDouble();
			double lat = coord.get(1).asDouble();
			points.add(new GeoPoint(lat, lng));
		}
		return points;
	}

	/*
	public static List<List<GeoPoint>> loadPolygons(InputStream is) {
		try {
			JsonNode root = mapper.readTree(is);
	
			JsonNode features = root.get("features");
			if (features == null || !features.isArray()) {
				// throw new IllegalArgumentException("Invalid GeoJSON: geometry is missing");
				log.error("Invalid feature: {}", feature);
				continue;
			}
	
			JsonNode typeNode = geometry.get("type");
			if (typeNode == null) {
				throw new IllegalArgumentException("Invalid GeoJSON: geometry.type is missing");
			}
			// IDE警告は無視
			String type = typeNode.asText();
	
			JsonNode coordinates = geometry.get("coordinates");
			if (coordinates == null || !coordinates.isArray()) {
				throw new IllegalArgumentException("Invalid GeoJSON: geometry.coordinates");
			}
	
			List<List<GeoPoint>> polygons = new ArrayList<>();
	
			if ("Polygon".equals(type)) {
				polygons.add(parsePolygon(coordinates));
			} else if ("MultiPolygon".equals(type)) {
				for (JsonNode polygonNode : coordinates) {
					polygons.add(parsePolygon(polygonNode));
				}
			} else {
				throw new IllegalArgumentException("Unsupported geometry type: " + type);
			}
	
			return polygons;
	
		} catch (Exception e) {
			throw new RuntimeException("Failed to load GeoJSON", e);
		}
	}
	
	private static List<GeoPoint> parsePolygon(JsonNode polygonNode) {
		// polygonNode: [[[lng, lat], ...]]
		JsonNode outerRing = polygonNode.get(0);
	
		List<GeoPoint> points = new ArrayList<>();
		for (JsonNode coord : outerRing) {
			double lng = coord.get(0).asDouble();
			double lat = coord.get(1).asDouble();
			points.add(new GeoPoint(lat, lng));
		}
		return points;
	}
	*/

}
