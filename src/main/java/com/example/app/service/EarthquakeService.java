package com.example.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.domain.RiskLevel;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.dto.raw.EarthquakeRawDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EarthquakeService {

	/************************
	 * 
	 * RawDto ⇒ Mesh
	 *  
	************************/

	// 1件変換メソッド
	public EarthquakeMesh convert(EarthquakeRawDto raw) {
		EarthquakeMesh mesh = new EarthquakeMesh();
		// 値のコピー
		mesh.setMeshCode(raw.getMeshCode());
		mesh.setIntensity(raw.getIntensity());
		// リスク分類
		mesh.setRiskLevel(classifyRisk(raw.getIntensity()));
		return mesh;
	}

	// List変換メソッド
	public List<EarthquakeMesh> convertList(List<EarthquakeRawDto> raws) {
		return raws.stream()
				.map(this::convert)
				.collect(Collectors.toList());
	}

	// リスク分類メソッド
	public RiskLevel classifyRisk(double intensity) {
		// 計測震度4.7以上：HIGH
		if (intensity >= 4.7) {
			return RiskLevel.HIGH;
		}
		// 計測震度4.6以上：MEDIUM
		if (intensity >= 4.6) {
			return RiskLevel.MEDIUM;
		}
		// 計測震度上記未満：LOW
		return RiskLevel.LOW;
	}

	/************************
	 * 
	 * Mesh ⇒ LayerDto
	 *  
	************************/

	// リスト変換
	public List<EarthquakeLayerDto> toLayerDtos(List<EarthquakeMesh> meshes) {
		// List<EarthquakeLayerDto>をStreamに変換(1件ずつ処理可能にする)
		return meshes.stream()
				.map(this::toLayerDto)
				.toList();
	}

	// Service内部用変換ルール（外部利用しないのでprivate）
	private EarthquakeLayerDto toLayerDto(EarthquakeMesh mesh) {
		// API返却用DTOを新規作成
		EarthquakeLayerDto dto = new EarthquakeLayerDto();
		dto.setMeshCode(mesh.getMeshCode());
		dto.setRiskLevel(mesh.getRiskLevel().name());

		return dto;
	}

	// 地震レイヤーの取得
	public List<EarthquakeLayerDto> getLayer(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng,
			int zoom) {
		// TODO: 後で実装
		return List.of();
	}

}
