package com.example.app.domain;

import java.util.List;

import com.example.app.domain.area.GeoPoint;

/*******************************************
 * 
 * 地震リスクを表すドメインモデル
 * ・1つのポリゴン + リスクレベルを保持する
 * 
 ******************************************/
public class RiskArea {

	private final List<GeoPoint> polygon; // ポリゴン座標
	private final RiskLevel riskLevel; // HIGH/MEDIUM/LOW
	private final Double intensity; // 震度

	/****************************************
	 * コンストラクタ
	 * @param polygon ポリゴン座標リスト
	 * @param riskLevel リスクレベル
	 ***************************************/
	public RiskArea(
			List<GeoPoint> polygon,
			RiskLevel riskLevel,
			Double intensity) {
		if (polygon == null || polygon.size() < 3) {
			throw new IllegalArgumentException("ポリゴンは3点以上必要です");
		}
		if (riskLevel == null) {
			throw new IllegalArgumentException("riskLevelは必須です");
		}

		this.polygon = polygon;
		this.riskLevel = riskLevel;
		this.intensity = intensity;
	}

	public List<GeoPoint> getPolygon() {
		return polygon;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public Double getIntensity() {
		return intensity;
	}

	/**********************************************
	 * 指定座標がこのポリゴン内にあるか判定
	 * @param point 判定したい座標
	 * @return true: ポリゴン内, false: ポリゴン外
	 *********************************************/
	public boolean contains(GeoPoint point) {
		return PointInPolygon.contains(polygon, point);
	}

	@Override
	public String toString() {
		return "RiskArea{" +
				"riskLevel=" + riskLevel +
				", intensity=" + intensity +
				", polygonPoints=" + polygon.size() +
				'}';
	}
}
