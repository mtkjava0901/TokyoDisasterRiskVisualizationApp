package com.example.app.domain.area;

import java.util.List;

public class TokyoBoundary {

	/********************************************************
	 * 
	 * 東京都の境界線（ポリゴン）を表すドメインオブジェクト
	 * 
	 * ・「東京都の外形そのもの」
	 * ・ポリゴンを面として扱わず、線分の集まりとして扱う
	 * 
	 *******************************************************/

	// 境界ポリゴンの頂点リスト
	private final List<GeoPoint> points;

	// 既に順序保証された境界データを受け取る(コンストラクタ)
	// +不正な状態で作れない様にする
	public TokyoBoundary(List<GeoPoint> points) {
		if (points == null || points.isEmpty()) {
			throw new IllegalArgumentException("TokyoBoundary points must not be null or empty");
		}
		this.points = List.copyOf(points); // 不変化
	}

	// 境界を参照したい場合用 (アクセッサ)
	public List<GeoPoint> points() {
		return points;
	}

	// メインロジック：最短境界点を探す
	// ⇒指定した地点(targer)から東京都境界線上で一番近い点(とその距離)
	public NearestBoundary findNearest(GeoPoint target) {

		// 最短候補を初期化
		NearestBoundary best = null;

		// 境界線を線分の集合として走査
		int n = points.size();
		for (int i = 0; i < n; i++) {
			// 線分を1本ずつ取り出す
			GeoPoint a = points.get(i);
			GeoPoint b = points.get((i + 1) % n); // 閉曲線

			// targetから線分ABへの最近接点を計算
			GeoPoint nearest = DistanceCalculator.nearestPointOnSegment(target, a, b);

			// 最近接点までの距離を計算
			// target⇒境界線までの距離(m)
			double dist = DistanceCalculator.distanceMeter(target, nearest);

			// 最短かどうかを判定 初回orより短い距離なら更新
			// ⇒常に最短を保持
			if (best == null || dist < best.distanceMeter()) {
				best = new NearestBoundary(nearest, dist);
			}
		}
		// 全線分を見終わったら返す
		return best;
	}

}
