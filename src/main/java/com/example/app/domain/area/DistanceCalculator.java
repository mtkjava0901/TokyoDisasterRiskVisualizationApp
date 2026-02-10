package com.example.app.domain.area;

public class DistanceCalculator {

	/**********************************
	 * 
	 * ”平面近似+線分最近接点”
	 * 『緯度経度を一旦メートル平面に落とし、
	 * 　ベクトル計算で距離・最近接点を高速に求めるクラス』
	 * 
	 * ・緯度、経度を扱うが球面三角法は使わない
	 * ・局所的には地球を平面として近似して
	 * 　⇒点と点の距離
	 * 　⇒点Pと線分ABの｢一番近い点｣
	 * これらを求めるためのユーティリティクラス
	 * 
	 * ※平面近似:3次元空間内の複雑な点群や曲面データを、
	 * 最もよく当てはまる単一の｢平らな面(平面)｣の方程式で代用する手法
	 * 
	 *********************************/

	// 定数定義：地球半径(m)
	private static final double R = 6378137.0;

	// 点Pと線分ABの最近接点を返す（平面近似）
	// ⇒点Pから、線分AB上で一番近い点Qを求めたい
	public static GeoPoint nearestPointOnSegment(
			GeoPoint p, GeoPoint a, GeoPoint b) {
		// Aを原点とした局所平面座標に変換
		Vector2D AP = toVector(a, p);
		Vector2D AB = toVector(a, b);

		double ab2 = AB.dot(AB);

		// AとBが同一点なら｢線分じゃない｣ ⇒最近接点は常にA
		if (ab2 == 0.0) {
			return a;
		}

		// 射影係数 t（内積）
		double t = AP.dot(AB) / ab2;

		// 線分内に制限(AB上に限定)	
		t = clamp(t, 0.0, 1.0);

		// 最近接点 Q = A + AB * t
		// ⇒平面座標から緯度経度に戻す
		return fromVector(a, AB.scale(t));
	}

	// 2点間距離(m) ※平面近似
	// Aを原点にしてBを平面化(√(x²+y²))
	public static double distanceMeter(GeoPoint a, GeoPoint b) {
		Vector2D v = toVector(a, b);
		return v.length();
	}

	/*********************************
	 * 内部ユーティリティ
	 ********************************/

	// 緯度経度 ⇒ 平面ベクトル変換
	private static Vector2D toVector(GeoPoint origin, GeoPoint p) {
		double latRad = Math.toRadians(origin.lat());

		// X方向（経度）
		double dx = Math.toRadians(p.lng() - origin.lng())
				* Math.cos(latRad) * R;

		// Y方向（緯度）
		double dy = Math.toRadians(p.lat() - origin.lat()) * R;

		return new Vector2D(dx, dy);
	}

	// 平面ベクトル ⇒ 緯度経度変換
	private static GeoPoint fromVector(GeoPoint origin, Vector2D v) {
		double lat = origin.lat()
				+ Math.toDegrees(v.y() / R);

		double lng = origin.lng()
				+ Math.toDegrees(v.x() / (R * Math.cos(Math.toRadians(origin.lat()))));

		return new GeoPoint(lat, lng);
	}

	// tを[0,1]に制限
	// ⇒線分判定のための安定装置
	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

}
