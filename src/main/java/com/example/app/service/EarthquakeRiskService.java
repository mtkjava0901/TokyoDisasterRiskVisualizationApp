package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.domain.MeshLevel;
import com.example.app.domain.RiskArea;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;

import lombok.RequiredArgsConstructor;

/***********************************************
 * 
 * 複数のRiskAreaをまとめてチェックするロジック
 * 
********************************************* */

@Service
@RequiredArgsConstructor
public class EarthquakeRiskService {

	// private final List<RiskArea> riskAreas;
	private final EarthquakeService earthquakeService;

	public RiskLevel getRiskLevel(GeoPoint point, MeshLevel meshLevel) {

		// meshLevelに応じたRiskArea生成
		List<RiskArea> riskAreas = earthquakeService.getRiskAreasByMeshLevel(meshLevel);

		// デフォルト値
		RiskLevel result = RiskLevel.LOW;

		for (RiskArea area : riskAreas) {
			if (area.contains(point)) {
				if (area.getRiskLevel() == RiskLevel.HIGH)
					return RiskLevel.HIGH;

				if (area.getRiskLevel() == RiskLevel.MEDIUM &&
						result != RiskLevel.HIGH) {
					result = RiskLevel.MEDIUM;
				}
			}
		}

		return result;
	}

}
