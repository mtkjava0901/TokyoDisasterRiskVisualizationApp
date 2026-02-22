package com.example.app.domain;

import java.util.List;

import com.example.app.domain.area.GeoPoint;

import lombok.Data;

@Data
public class FloodArea {

	/**************************************************************
	 * 洪水リスクエリアのドメインモデル
	 * ・1つのGeoJSONポリゴン + 浸水深ランク(A31b_201) を保持
	 * ・地震側の EarthquakeMesh + RiskArea に相当
	 *************************************************************/
	// GeoJSONから取得したポリゴン座標
	private List<GeoPoint> polygon;
	// A31b_201 浸水深ランク（1〜5）
	private int rank;
	// 分位点で判定されるリスクレベル
	private RiskLevel riskLevel;

	/****************************************
	 * 指定座標がこのポリゴン内にあるか判定
	 * （RiskArea.contains()と同じロジック）
	 ***************************************/
	public boolean contains(GeoPoint point) {
		return PointInPolygon.contains(polygon, point);
	}

	/****************************************
	 * ランク値 ⇒ 浸水深の日本語説明
	 * ※リスクレベルとは独立（国の定義に基づく）
	 ***************************************/
	public static String getDepthDescription(int rank) {
		return switch (rank) {
		case 1 -> "0.5m未満";
		case 2 -> "0.5m〜3.0m未満";
		case 3 -> "3.0m〜5.0m未満";
		case 4 -> "5.0m〜10.0m未満";
		case 5 -> "10.0m以上";
		default -> "不明";
		};
	}
}
