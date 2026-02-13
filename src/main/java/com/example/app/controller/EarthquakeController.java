package com.example.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.domain.MeshLevel;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.service.EarthquakeRiskService;
import com.example.app.service.EarthquakeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/earthquake")
public class EarthquakeController {

	private final EarthquakeService earthquakeService;
	private final EarthquakeRiskService earthquakeRiskService;

	// A-01 地震レイヤーAPI取得
	@GetMapping("/layer")
	public List<EarthquakeLayerDto> getEarthquakeLayer(
			@RequestParam double minLat,
			@RequestParam double maxLat,
			@RequestParam double minLng,
			@RequestParam double maxLng,
			@RequestParam int meshLevel) {
		return earthquakeService
				.getLayer(minLat, maxLat, minLng, maxLng,
						MeshLevel.fromCodeLength(meshLevel));
	}

	// A-03 地震リスク(1点)判定API取得
	@GetMapping("/risk")
	public Map<String, String> getRiskLevel(
			@RequestParam double lat,
			@RequestParam double lng,
			@RequestParam int meshLevel) {

		RiskLevel level = earthquakeRiskService
				.getRiskLevel(new GeoPoint(lat, lng),
						MeshLevel.fromCodeLength(meshLevel));

		return Map.of("riskLevel", level.name());
	}

}
