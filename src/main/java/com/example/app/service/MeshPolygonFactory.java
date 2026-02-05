package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.app.domain.MeshLevel;
import com.example.app.dto.polygon.LatLngDto;

// Polygon(緯度経度4点)を返す責務だけを持つ計算クラス
// ⇒地理計算だけに集中する

@Component
public class MeshPolygonFactory {

	/****************************************************************
	 * meshCode ⇒ create
	 * (共通メッシュ/ここから1～3次メッシュへ分岐) 
	 ***************************************************************/
	public List<LatLngDto> create(String meshCode) {

		MeshLevel level = MeshLevel.from(meshCode);

		return switch (level) {
		case PRIMARY -> createPrimary(meshCode);
		case SECONDARY -> createSecondary(meshCode);
		case TERTIARY -> createTertiary(meshCode);
		};
	}

	/*************************************
	 * meshCode ： 
	 * create ⇒ createPrimary(1次メッシュ) 
	 ************************************/
	private List<LatLngDto> createPrimary(String meshCode) {

		int latCode = Integer.parseInt(meshCode.substring(0, 2));
		int lngCode = Integer.parseInt(meshCode.substring(2, 4));

		// 南西端
		double southLat = latCode * 2.0 / 3.0;
		double westLng = lngCode + 100.0;

		// 北東端
		double northLat = southLat + (2.0 / 3.0);
		double eastLng = westLng + 1.0;

		return rectangle(southLat, westLng, northLat, eastLng);
	}

	/*************************************
	 * meshCode ： 
	 * create ⇒ createSecondary(2次メッシュ) 
	 * 1次メッシュを8×8に分割
	 ************************************/
	private List<LatLngDto> createSecondary(String meshCode) {

		String primaryCode = meshCode.substring(0, 4);
		List<LatLngDto> base = createPrimary(primaryCode);

		int latIndex = Character.getNumericValue(meshCode.charAt(4));
		int lngIndex = Character.getNumericValue(meshCode.charAt(5));

		double southLat = base.get(0).getLat();
		double westLng = base.get(0).getLng();

		double latSize = (2.0 / 3.0) / 8.0;
		double lngSize = 1.0 / 8.0;

		double swLat = southLat + latSize * latIndex;
		double swLng = westLng + lngSize * lngIndex;

		return rectangle(swLat, swLng, swLat + latSize, swLng + lngSize);
	}

	/*************************************
	 * meshCode ： 
	 * create ⇒ createTertiary(3次メッシュ) 
	 * 2次メッシュを10×10に分割
	 ************************************/
	private List<LatLngDto> createTertiary(String meshCode) {

		// 2次メッシュを基準にする
		String secondaryCode = meshCode.substring(0, 6);
		List<LatLngDto> base = createSecondary(secondaryCode);

		int latIndex = Character.getNumericValue(meshCode.charAt(6));
		int lngIndex = Character.getNumericValue(meshCode.charAt(7));

		double southLat = base.get(0).getLat();
		double westLng = base.get(0).getLng();

		double latSize = ((2.0 / 3.0) / 8.0) / 10.0;
		double lngSize = ((1.0) / 8.0) / 10.0;

		double swLat = southLat + latSize * latIndex;
		double swLng = westLng + lngSize * lngIndex;

		return rectangle(
				swLat,
				swLng,
				swLat + latSize,
				swLng + lngSize);
	}

	/*************************************
	 * 
	 *  共通：矩形生成
	 *  
	 ************************************/

	// rectangle -> 概念をコード化
	private List<LatLngDto> rectangle(
			double southLat,
			double westLng,
			double northLat,
			double eastLng) {

		return List.of(
				new LatLngDto(southLat, westLng), // SW
				new LatLngDto(northLat, westLng), // NW
				new LatLngDto(northLat, eastLng), // NE
				new LatLngDto(southLat, eastLng) // SE

		);
	}

}
