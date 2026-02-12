package com.example.app.domain;

import java.util.List;

import com.example.app.domain.area.GeoPoint;

public final class PointInPolygon {

	/*****************************
	 * 
	 * 位置判定用クラス
	 * ⇒「点がポリゴンの中にあるか？」（純数学ロジック）
	 * 
	 ****************************/

	private static final double EPS = 1e-10;

	// 1点(緯度経度)が、あるポリゴンの内側か外側かを判定
	private PointInPolygon() {
	}

	// RayCastingAlgorithm（レイキャスティング法）
	// ⇒｢点から右方向に一本の光線を伸ばす。ポリゴンの辺と何回交差するか？｣
	public static boolean contains(List<GeoPoint> polygon, GeoPoint point) {

		boolean inside = false;
		int n = polygon.size();

		// i = 現在の頂点, j = 1つ前の頂点
		// 辺(j→i)を順に処理
		// ⇒最後の辺は(n-1) → 0 = ポリゴンが閉じる
		for (int i = 0, j = n - 1; i < n; j = i++) {
			// double xi = polygon.get(i).lng();
			// double yi = polygon.get(i).lat();
			// double xj = polygon.get(j).lng();
			// double yj = polygon.get(j).lat();

			GeoPoint pi = polygon.get(i);
			GeoPoint pj = polygon.get(j);

			// 境界線上チェック（線分上にあったらtrue）
			if (onSegment(pj, pi, point)) {
				return true;
			}

			// ①点の水平線が、辺の上下をまたいでいるか？
			// ⇒満たさない辺は無視
			// ②点から右に伸ばした線が辺と交差するか？
			boolean intersect = ((pi.lat() > point.lat()) != (pj.lat() > point.lat())) &&
					(point.lng() < (pj.lng() - pi.lng()) *
							(point.lat() - pi.lat()) /
							(pj.lat() - pi.lat()) + pi.lng());

			// 両方true⇒交差有り
			if (intersect) {
				inside = !inside;
			}
		}

		// 最終的に奇数ならinside
		return inside;
	}

	private static boolean onSegment(GeoPoint a, GeoPoint b, GeoPoint p) {
		double cross = (p.lat() - a.lat()) * (b.lng() - a.lng()) -
				(p.lng() - a.lng()) * (b.lat() - a.lat());

		// ①外積で一直線か？
		// 点が線分ab上にあるならベクトルが平行⇒外積0
		if (Math.abs(cross) > EPS) {
			return false;
		}

		// ②内積で範囲内か？
		// aとbの間にあるか？延長線上ではないか？　⇒線分上かどうかの完全判定
		double dot = (p.lng() - a.lng()) * (p.lng() - b.lng()) +
				(p.lat() - a.lat()) * (p.lat() - b.lat());

		return dot <= EPS;
	}

}
