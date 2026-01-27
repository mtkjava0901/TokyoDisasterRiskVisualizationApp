package com.example.app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.service.EarthquakeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/earthquake")
public class EarthquakeController {

	private final EarthquakeService earthquakeService;

	// 地震レイヤー取得API(A-01)
	@GetMapping("/layer")
	public List<EarthquakeLayerDto> getEarthquakeLayer(
			@RequestParam double minLat,
			@RequestParam double maxLat,
			@RequestParam double minLng,
			@RequestParam double maxLng,
			@RequestParam int zoom) {
		return earthquakeService.getLayer(minLat, maxLat, minLng, maxLng, zoom);
	}

}
