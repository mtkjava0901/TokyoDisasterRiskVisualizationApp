package com.example.app.service;

import org.springframework.stereotype.Service;

import com.example.app.infrastructure.TokyoPolygonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokyoAreaService {

	private final TokyoPolygonRepository repository;

	/*****************************************
	 * 
	 * 指定座標が東京都内かどうかを判定
	 * 
	 ****************************************/
	public boolean isTokyo(double lat, double lng) {
		return repository.contains(lat, lng);
	}

}
