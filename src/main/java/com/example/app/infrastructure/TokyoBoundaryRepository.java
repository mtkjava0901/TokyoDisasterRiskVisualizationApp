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
			List<GeoPoint> points = loadFromGeoJson();

			if (points == null || points.isEmpty()) {
				throw new IllegalStateException("Tokyo boundary points are empty");
			}
			cached = new TokyoBoundary(points);
		}
		return cached;
	}

	private List<GeoPoint> loadFromGeoJson() {

		try (InputStream is = new ClassPathResource(
				"static/geo/tokyo_mainland.geojson").getInputStream()) {

			JsonNode root = mapper.readTree(is);

			JsonNode features = root.path("features");
			if (!features.isArray() || features.isEmpty()) {
				throw new IllegalStateException("GeoJSON features not found");
			}

			// 本土データのみ → 最初の Polygon を使う
			JsonNode geometry = features.get(0).path("geometry");

			if (!"Polygon".equals(geometry.path("type").asText())) {
				throw new IllegalStateException("Geometry is not Polygon");
			}

			// coordinates[0] = outer ring
			JsonNode outerRing = geometry
					.path("coordinates")
					.path(0);

			if (!outerRing.isArray() || outerRing.isEmpty()) {
				throw new IllegalStateException("Outer ring not found");
			}

			List<GeoPoint> points = new ArrayList<>();
			for (JsonNode coord : outerRing) {
				double lng = coord.get(0).asDouble();
				double lat = coord.get(1).asDouble();
				points.add(new GeoPoint(lat, lng));
			}

			return points;

		} catch (Exception e) {
			throw new IllegalStateException("Failed to load Tokyo boundary GeoJSON", e);
		}
	}

}
