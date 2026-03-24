package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.domain.MeshLevel;
import com.example.app.domain.RiskArea;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.dto.polygon.LatLngDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EarthquakeService {

	private final EarthquakeMeshAggregator earthquakeMeshAggregator;
	private final MeshPolygonFactory meshPolygonFactory;
	private final EarthquakeLayerFactory layerFactory;

	/************************
	 * 
	 * 地震レイヤーAPI取得
	 *  
	 ************************/

	public List<EarthquakeLayerDto> getLayer(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng,
			MeshLevel meshLevel) {

		// 1. 集約済みMeshをキャッシュから取得 (BBoxフィルタ付き)
		List<EarthquakeMesh> meshes = earthquakeMeshAggregator.getMeshesInBounds(
				meshLevel, minLat, maxLat, minLng, maxLng);

		if (meshes.isEmpty()) {
			return List.of();
		}

		// 2. Mesh⇒LayerDto(Polygon生成)
		return meshes.stream()
				.map(layerFactory::create)
				.toList();
	}

	/************************
	 * 
	 * 地震リスク判定API用
	 *  
	 ************************/

	public List<RiskArea> getAllRiskAreas() {
		// 基準とするメッシュレベルを定義(3次メッシュ)
		MeshLevel targetLevel = MeshLevel.TERTIARY;

		List<EarthquakeMesh> meshes = earthquakeMeshAggregator.getAllMeshesByLevel(targetLevel);

		return meshes.stream()
				.map(m -> {
					List<LatLngDto> dtos = meshPolygonFactory.create(m.getMeshCode());
					List<GeoPoint> geoPoints = dtos.stream()
							.map(dto -> new GeoPoint(dto.getLat(), dto.getLng()))
							.toList();

					return new RiskArea(
							geoPoints,
							m.getRiskLevel(),
							m.getIntensity());
				})
				.toList();
	}

	/************************
	 * 
	 * meshLevel別リスクエリア取得
	 *  
	 ************************/

	public List<RiskArea> getRiskAreasByMeshLevel(MeshLevel meshLevel) {

		List<EarthquakeMesh> meshes = earthquakeMeshAggregator.getAllMeshesByLevel(meshLevel);

		return meshes.stream()
				.map(m -> {
					List<LatLngDto> dtos = meshPolygonFactory.create(m.getMeshCode());

					List<GeoPoint> geoPoints = dtos.stream()
							.map(dto -> new GeoPoint(dto.getLat(), dto.getLng()))
							.toList();

					return new RiskArea(
							geoPoints,
							m.getRiskLevel(),
							m.getIntensity());
				})
				.toList();
	}

}
