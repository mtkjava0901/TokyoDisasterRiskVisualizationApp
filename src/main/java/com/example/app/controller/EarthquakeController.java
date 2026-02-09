package com.example.app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.domain.MeshLevel;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.service.EarthquakeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/earthquake")
public class EarthquakeController {

	private final EarthquakeService earthquakeService;

	// 地震レイヤーAPI取得(A-01)
	@GetMapping("/layer")
	public List<EarthquakeLayerDto> getEarthquakeLayer(
			@RequestParam double minLat,
			@RequestParam double maxLat,
			@RequestParam double minLng,
			@RequestParam double maxLng,
			@RequestParam int meshLevel) {
		return earthquakeService.getLayer(minLat, maxLat, minLng, maxLng, MeshLevel.fromCodeLength(meshLevel));
	}

}
