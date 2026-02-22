package com.example.app.domain;

import org.springframework.stereotype.Component;

import com.example.app.domain.area.GeoPoint;

/****************************************************************
 * 緯度経度 ⇒ 日本地域メッシュコード計算
 *
 * 対応：
 * ・1次メッシュ（80km）
 * ・2次メッシュ（10km）
 * ・3次メッシュ（1km）
 *
 * ※MeshPolygonFactoryと完全整合する計算式
 ****************************************************************/

@Component
public class MeshCodeCalculator {

	/*************************************************************
	 * 3次メッシュコード生成（推奨使用）
	 *
	 * @param lat 緯度
	 * @param lng 経度
	 * @return 8桁 meshCode
	 *************************************************************/
	public String toTertiaryMesh(double lat, double lng) {

		// ---------- 1次メッシュ ----------
		int lat1 = (int) Math.floor(lat * 1.5); // lat / (2/3)
		int lng1 = (int) Math.floor(lng - 100);

		// 1次メッシュ南西端
		double baseLat = lat1 * 2.0 / 3.0;
		double baseLng = lng1 + 100.0;

		// ---------- 2次メッシュ ----------
		double latOffset1 = lat - baseLat;
		double lngOffset1 = lng - baseLng;

		int lat2 = (int) Math.floor(latOffset1 / ((2.0 / 3.0) / 8.0));
		int lng2 = (int) Math.floor(lngOffset1 / (1.0 / 8.0));

		// 2次メッシュ南西端
		double baseLat2 = baseLat + lat2 * ((2.0 / 3.0) / 8.0);
		double baseLng2 = baseLng + lng2 * (1.0 / 8.0);

		// ---------- 3次メッシュ ----------
		double latOffset2 = lat - baseLat2;
		double lngOffset2 = lng - baseLng2;

		int lat3 = (int) Math.floor(latOffset2 / (((2.0 / 3.0) / 8.0) / 10.0));
		int lng3 = (int) Math.floor(lngOffset2 / ((1.0 / 8.0) / 10.0));

		return String.format(
				"%02d%02d%d%d%d%d",
				lat1,
				lng1,
				lat2,
				lng2,
				lat3,
				lng3);
	}

	/*************************************************************
	 * 1次メッシュ（必要なら使用）
	 *************************************************************/
	public String toPrimaryMesh(double lat, double lng) {
		int latCode = (int) Math.floor(lat * 1.5);
		int lngCode = (int) Math.floor(lng - 100);
		return String.format("%02d%02d", latCode, lngCode);
	}

	/*************************************************************
	 * 2次メッシュ（必要なら使用）
	 *************************************************************/
	public String toSecondaryMesh(double lat, double lng) {

		int lat1 = (int) Math.floor(lat * 1.5);
		int lng1 = (int) Math.floor(lng - 100);

		double baseLat = lat1 * 2.0 / 3.0;
		double baseLng = lng1 + 100.0;

		int lat2 = (int) Math.floor((lat - baseLat) / ((2.0 / 3.0) / 8.0));
		int lng2 = (int) Math.floor((lng - baseLng) / (1.0 / 8.0));

		return String.format("%02d%02d%d%d", lat1, lng1, lat2, lng2);
	}

	/**************************************************************
	 * 3次メッシュ → bounds取得
	 **************************************************************/
	public MeshBounds toBounds(String meshCode) {

		int p = Integer.parseInt(meshCode.substring(0, 2));
		int q = Integer.parseInt(meshCode.substring(2, 4));
		int r = Integer.parseInt(meshCode.substring(4, 5));
		int s = Integer.parseInt(meshCode.substring(5, 6));
		int t = Integer.parseInt(meshCode.substring(6, 7));
		int u = Integer.parseInt(meshCode.substring(7, 8));

		// 緯度計算
		double lat = p * 2.0 / 3.0;
		lat += r * (2.0 / 3.0 / 8);
		lat += t * (2.0 / 3.0 / 80);

		// 経度計算
		double lng = 100 + q;
		lng += s * (1.0 / 8);
		lng += u * (1.0 / 80);

		double latSize = 2.0 / 3.0 / 80;
		double lngSize = 1.0 / 80;

		return new MeshBounds(
				new GeoPoint(lat, lng),
				new GeoPoint(lat + latSize, lng + lngSize));
	}

}
