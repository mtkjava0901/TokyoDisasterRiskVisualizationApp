package com.example.app.domain.area;

/************************************
 * 
 * 境界探索の結果を表す値オブジェクト
 * 
 ***********************************/

public record NearestBoundary(
		GeoPoint point,
		double distanceMeter) {

}
