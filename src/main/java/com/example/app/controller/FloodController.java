package com.example.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.domain.FloodArea;
import com.example.app.domain.FloodMesh;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.FloodLayerDto;
import com.example.app.service.FloodService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flood")
public class FloodController {

	private final FloodService floodService;
	// データ更新年月（GeoJSON更新時に変更）
	private static final String DATA_UPDATED_AT = "2025年5月";

	// A-02 洪水レイヤーAPI取得
	@GetMapping("/layer")
	public List<FloodLayerDto> getFloodLayer(
			@RequestParam double minLat,
			@RequestParam double maxLat,
			@RequestParam double minLng,
			@RequestParam double maxLng) {
		return floodService.getLayer(
				minLat, maxLat, minLng, maxLng);
	}

	// A-04 洪水リスク(1点)判定API取得
	@GetMapping("/risk")
	public Map<String, Object> getFloodRisk(
			@RequestParam double lat,
			@RequestParam double lng) {

		GeoPoint point = new GeoPoint(lat, lng);

		FloodMesh mesh = floodService.findFloodArea(point);

		// 該当エリアが存在しない場合
		if (mesh == null) {
			Map<String, Object> result = new HashMap<>();
			result.put("riskLevel", "UNKNOWN");
			result.put("rank", null);
			result.put("depthDescription", null);
			result.put("dataUpdatedAt", DATA_UPDATED_AT);
			return result;
		}

		// 正常レスポンス
		return Map.of(
				"riskLevel", mesh.getRiskLevel().name(),
				"rank", mesh.getMaxRank(),
				"depthDescription",
				FloodArea.getDepthDescription(mesh.getMaxRank()),
				"dataUpdatedAt", DATA_UPDATED_AT);
	}

}
