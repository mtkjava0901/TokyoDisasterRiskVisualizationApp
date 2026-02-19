package com.example.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.dto.area.TokyoAreaResponse;
import com.example.app.dto.area.TokyoContainsResponse;
import com.example.app.service.TokyoAreaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area/tokyo")
public class TokyoAreaController {

	private final TokyoAreaService service;

	// 東京都内判定API (A-05)
	@GetMapping("/contains")
	public TokyoContainsResponse contains(
			@RequestParam double lat,
			@RequestParam double lng) {

		boolean isTokyo = service.isTokyo(lat, lng);
		return new TokyoContainsResponse(isTokyo);
	}

	// 最近接東京都境界取得 (A-06)
	@GetMapping("/nearest-boundary")
	public TokyoAreaResponse checkArea(
			@RequestParam double lat,
			@RequestParam double lng) {

		boolean isTokyo = service.isTokyo(lat, lng);
		var nearest = service.findNearestBoundary(lat, lng);

		return new TokyoAreaResponse(isTokyo, nearest);
	}

	// Controllerの戻り値を明示的にする（必要？）
	//	public record TokyoContainsResponse(boolean isTokyo) {
	//		public static TokyoContainsResponse of(boolean isTokyo) {
	//			return new TokyoContainsResponse(isTokyo);
	//		}
	//	}

}
