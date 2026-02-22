package com.example.app.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.domain.MeshLevel;
import com.example.app.domain.RiskArea;
import com.example.app.domain.area.GeoPoint;
import com.example.app.service.EarthquakeRiskService;
import com.example.app.service.EarthquakeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/earthquake")
public class EarthquakeController {

	private final EarthquakeService earthquakeService;
	private final EarthquakeRiskService earthquakeRiskService;

	// データ更新年月(CSV更新時に変更)
	private static final String DATA_UPDATED_AT = "2024年6月";

	// A-01 地震レイヤーAPI取得
	@GetMapping("/layer")
	public Object getEarthquakeLayer(
			@RequestParam double minLat,
			@RequestParam double maxLat,
			@RequestParam double minLng,
			@RequestParam double maxLng,
			@RequestParam int meshLevel) {

		return earthquakeService.getLayer(
				minLat, maxLat, minLng, maxLng,
				MeshLevel.fromCodeLength(meshLevel));
	}

	// A-03 地震リスク(1点)判定API取得
	@GetMapping("/risk")
	public Map<String, Object> getRiskLevel(
			@RequestParam double lat,
			@RequestParam double lng) {

		GeoPoint point = new GeoPoint(lat, lng);

		// 常に3次メッシュ
		RiskArea area = earthquakeRiskService.findRiskArea(
				point,
				MeshLevel.TERTIARY);

		// メッシュが存在しない場合
		if (area == null) {
			return Map.of(
					"riskLevel", "UNKNOWN",
					"intensity", null,
					"dataUpdatedAt", DATA_UPDATED_AT);
		}

		// RiskLevel level = mesh.getRiskLevel();

		// 正常レスポンス
		return Map.of(
				"riskLevel", area.getRiskLevel().name(),
				"intensity", area.getIntensity(),
				"dataUpdatedAt", DATA_UPDATED_AT);
	}

}
