package com.example.app.domain.area;

/********************************************************
 * 
 * 2次元ベクトル（平面上の矢印）を表す不変数学オブジェクト
 * 役割：
 * ・緯度経度の複雑さを切り離す
 * ・地理計算を高校数学レベルにする
 * ・DistanceCalculatorの可読性を高める
 * 
 *******************************************************/

public record Vector2D(double x, double y) {

	// 内積（dot）
	// ⇒点Pを線分ABにどれだけ投影できるか
	public double dot(Vector2D other) {
		return x * other.x + y * other.y;
	}

	// スカラー倍（scale）
	// ⇒ベクトルの長さをｋ倍、方向はそのまま
	// ABの方向にtだけ進んだ点 ＝ 「A + AB×t = 最近接点Q」
	public Vector2D scale(double k) {
		return new Vector2D(x * k, y * k);
	}

	// ベクトルの長さ（length）
	// 平面化した2点間の距離
	public double length() {
		return Math.sqrt(x * x + y * y);
	}

}
