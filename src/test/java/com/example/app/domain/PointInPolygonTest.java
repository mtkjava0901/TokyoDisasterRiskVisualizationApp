package com.example.app.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.app.domain.area.GeoPoint;

class PointInPolygonTest {

	@Test
	void testPointInsidePolygon() {
		List<GeoPoint> polygon = List.of(
				new GeoPoint(0, 0),
				new GeoPoint(0, 1),
				new GeoPoint(1, 1),
				new GeoPoint(1, 0));
		GeoPoint inside = new GeoPoint(0.5, 0.5);
		assertTrue(PointInPolygon.contains(polygon, inside));
	}

	@Test
	void testPointOutsidePolygon() {
		List<GeoPoint> polygon = List.of(
				new GeoPoint(0, 0),
				new GeoPoint(0, 1),
				new GeoPoint(1, 1),
				new GeoPoint(1, 0));
		GeoPoint outside = new GeoPoint(2, 2);
		assertFalse(PointInPolygon.contains(polygon, outside));
	}

	@Test
	void testPointOnEdge() {
		List<GeoPoint> polygon = List.of(
				new GeoPoint(0, 0),
				new GeoPoint(0, 1),
				new GeoPoint(1, 1),
				new GeoPoint(1, 0));
		GeoPoint edge = new GeoPoint(0, 0.5);
		assertTrue(PointInPolygon.contains(polygon, edge));
	}

}
