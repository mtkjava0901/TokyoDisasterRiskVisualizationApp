package com.example.app.domain;

import com.example.app.domain.area.GeoPoint;

/**************************************************************
 * メッシュ境界（矩形）
 *
 * southWest --- northEast
 **************************************************************/

public class MeshBounds {

	private final GeoPoint southWest;
	private final GeoPoint northEast;

	public MeshBounds(GeoPoint southWest, GeoPoint northEast) {
		this.southWest = southWest;
		this.northEast = northEast;
	}

	public GeoPoint getSouthWest() {
		return southWest;
	}

	public GeoPoint getNorthEast() {
		return northEast;
	}

	/******************************************************
	 * 中心点取得
	 ******************************************************/
	public GeoPoint getCenter() {
		double lat = (southWest.lat() + northEast.lat()) / 2;
		double lng = (southWest.lng() + northEast.lng()) / 2;
		return new GeoPoint(lat, lng);
	}

	/******************************************************
	 * BBox内判定
	 ******************************************************/
	public boolean intersects(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		return !(northEast.lat() < minLat
				|| southWest.lat() > maxLat
				|| northEast.lng() < minLng
				|| southWest.lng() > maxLng);
	}

}
