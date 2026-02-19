package com.example.app.service;

import org.springframework.stereotype.Service;

import com.example.app.domain.NearestBoundaryResult;
import com.example.app.domain.area.GeoPoint;
import com.example.app.domain.area.NearestBoundary;
import com.example.app.domain.area.TokyoBoundary;
import com.example.app.infrastructure.TokyoBoundaryRepository;
import com.example.app.infrastructure.TokyoPolygonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokyoAreaService {

	private final TokyoPolygonRepository polygonRepository;
	private final TokyoBoundaryRepository boundaryRepository;

	/***************************************************************
	 * 
	 * 東京都エリアに関するユースケース(UseCase)調整役
	 * 
	 * ・Controllerから受け取ったプリミティブ値を値オブジェクトに変換
	 * ・Repositoryからドメインを取得 ⇒ ドメインロジックを呼び出す
	 * ・結果をResult/DTOに詰め替えて返す
	 * 
	 **************************************************************/

	// 指定座標が東京都内かどうかを判定
	public boolean isTokyo(double lat, double lng) {
		return polygonRepository.contains(lat, lng);
	}

	// 東京都境界までの最近点を取得
	public NearestBoundaryResult findNearestBoundary(double lat, double lng) {

		// 1.primitive ⇒ 値オブジェクト
		GeoPoint target = new GeoPoint(lat, lng);

		// 2.ドメイン取得
		TokyoBoundary boundary = boundaryRepository.load();

		// 3.ドメインロジック実行
		NearestBoundary nearest = boundary.findNearest(target);

		// 4.ドメイン ⇒ ResultDTOに変換
		return new NearestBoundaryResult(
				nearest.point().lat(),
				nearest.point().lng(),
				nearest.distanceMeter());
	}
}
