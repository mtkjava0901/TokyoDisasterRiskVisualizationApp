package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.domain.RiskArea;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;

/***********************************************
 * 
 * 複数のRiskAreaをまとめてチェックするロジック
 * 
********************************************* */

@Service
public class EarthquakeRiskService {

	private final List<RiskArea> riskAreas;

	public EarthquakeRiskService(List<RiskArea> riskAreas) {
		this.riskAreas = riskAreas;
		// 確認①.アプリ起動時にここが出るか？件数が0になっていないか？
		System.out.println("--- EarthquakeRiskService 起動 ---");
		System.out.println("ロードされたエリア数: " + (riskAreas != null ? riskAreas.size() : "null"));

	}

	public RiskLevel getRiskLevel(GeoPoint point) {
		// 確認②. Reactからクリックが届いているか、座標は正しいか
		System.out.println("--- 判定開始 ---");
		// System.out.println("リクエスト座標: Lat=" + point.getLat() + ", Lng=" + point.getLng());

		RiskLevel result = RiskLevel.LOW; // デフォルト

		for (RiskArea area : riskAreas) {
			if (area.contains(point)) {
				if (area.getRiskLevel() == RiskLevel.HIGH)
					return RiskLevel.HIGH;
				if (area.getRiskLevel() == RiskLevel.MEDIUM && result != RiskLevel.HIGH) {
					result = RiskLevel.MEDIUM;
				}
			}
		}

		return result;
	}

}
