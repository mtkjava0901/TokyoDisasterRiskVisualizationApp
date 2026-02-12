package com.example.app.domain;

import java.util.List;

import com.example.app.domain.area.GeoPoint;

// テスト用固定データ（削除予定）
public class MockRiskData {

	public static List<RiskArea> getEarthquakeRiskAreas() {
		return List.of(
				new RiskArea(
						List.of(
								new GeoPoint(35.681236, 139.767125),
								new GeoPoint(35.682236, 139.767125),
								new GeoPoint(35.682236, 139.768125),
								new GeoPoint(35.681236, 139.768125)),
						RiskLevel.HIGH),
				new RiskArea(
						List.of(
								new GeoPoint(35.713768, 139.774219),
								new GeoPoint(35.714768, 139.774219),
								new GeoPoint(35.714768, 139.775219),
								new GeoPoint(35.713768, 139.775219)),
						RiskLevel.MEDIUM),
				new RiskArea(
						List.of(
								new GeoPoint(35.693825, 139.703667),
								new GeoPoint(35.694825, 139.703667),
								new GeoPoint(35.694825, 139.704667),
								new GeoPoint(35.693825, 139.704667)),
						RiskLevel.LOW));
	}

}
