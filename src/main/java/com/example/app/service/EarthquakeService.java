package com.example.app.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.domain.MeshLevel;
import com.example.app.domain.RiskLevel;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.dto.raw.EarthquakeRawDto;
import com.example.app.infrastructure.EarthquakeCsvLoader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EarthquakeService {

	private final EarthquakeCsvLoader csvLoader;
	private final MeshPolygonFactory meshPolygonFactory;
	private final EarthquakeLayerFactory layerFactory;

	/************************
	 * 
	 * RawDto ⇒ Mesh
	 * (public)
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
	 * (private)
	 *  
	************************/

	// リスト変換
	private List<EarthquakeLayerDto> toLayerDtos(List<EarthquakeMesh> meshes) {
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
		dto.setPolygon(meshPolygonFactory.create(mesh.getMeshCode()));

		return dto;
	}

	/************************
	 * 
	 * Mesh ⇒ LayerDto
	 * (public)
	 *  
	************************/

	// 地震レイヤーAPI取得
	public List<EarthquakeLayerDto> getLayer(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng,
			MeshLevel meshLevel) {

		// ①CSV読み込み
		List<EarthquakeRawDto> raws = csvLoader.load();

		// ②Raw⇒Mesh(リスクの分類)
		Map<String, EarthquakeMesh> meshMap = raws.stream()
				.map(this::convert)
				// ｢以上｣で通す
				.filter(m -> m.getMeshCode().length() >= meshLevel.getCodeLength())
				// 上位メッシュに丸める
				.map(m -> {
					m.setMeshCode(
							m.getMeshCode().substring(0, meshLevel.getCodeLength()));
					return m;
				})
				// meshCode単位で集約
				.collect(Collectors.toMap(
						EarthquakeMesh::getMeshCode,
						m -> m,
						// 同一メッシュは｢震度が大きい方｣を残す
						(a, b) -> a.getIntensity() >= b.getIntensity() ? a : b));

		List<EarthquakeMesh> meshes = List.copyOf(meshMap.values());

		// デバッグ用
		System.out.println(
				"[Service] meshes after filter size=" + meshes.size());

		// ③Mesh⇒LayerDto(Polygon生成)
		return meshes.stream()
				.map(layerFactory::create)
				.toList();

	}

}
