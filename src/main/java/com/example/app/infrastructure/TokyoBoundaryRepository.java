package com.example.app.infrastructure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.example.app.domain.area.GeoPoint;
import com.example.app.domain.area.TokyoBoundary;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Repository
public class TokyoBoundaryRepository {

	/************************************************************
	 * 
	 * 東京都の本土Polygonの外周リングを取り出す
	 * ・[lng, lat]をGeoPoint(lat, lng)に変換
	 * ・順序付きList<GeoPoint>を作ってTokyoBoundaryとして返す
	 * 
	 ***********************************************************/

	private final ObjectMapper mapper = new ObjectMapper();

	private TokyoBoundary cached;

	public synchronized TokyoBoundary load() {
		if (cached == null) {

			List<List<GeoPoint>> polygons = loadFromGeoJson();

			if (polygons == null || polygons.isEmpty()) {
				throw new IllegalStateException("Tokyo boundary polygons are empty");
			}

			cached = new TokyoBoundary(polygons);
		}
		return cached;
	}

	private List<List<GeoPoint>> loadFromGeoJson() {

		try (InputStream is = new ClassPathResource(
				"static/geo/tokyo_mainland.geojson").getInputStream()) {

			JsonNode root = mapper.readTree(is);
			JsonNode features = root.path("features");

			if (!features.isArray() || features.isEmpty()) {
				throw new IllegalStateException("GeoJSON features not found");
			}

			List<List<GeoPoint>> polygons = new ArrayList<>();

			for (JsonNode feature : features) {

				// 本土データのみ → 最初の Polygon を使う
				JsonNode geometry = feature.path("geometry");

				if (!"Polygon".equals(geometry.path("type").asText())) {
					// 無効なfeatureは無視して次へ
					continue;
				}

				JsonNode outerRing = geometry
						.path("coordinates")
						.path(0);

				if (!outerRing.isArray() || outerRing.isEmpty()) {
					// 無効なfeatureは無視して次へ
					continue;
				}

				List<GeoPoint> polygon = new ArrayList<>();

				for (JsonNode coord : outerRing) {
					double lng = coord.get(0).asDouble();
					double lat = coord.get(1).asDouble();
					polygon.add(new GeoPoint(lat, lng));
				}
				polygons.add(polygon);
			}

			return polygons;

		} catch (Exception e) {
			throw new IllegalStateException("Failed to load Tokyo boundary GeoJSON", e);
		}
	}

}
