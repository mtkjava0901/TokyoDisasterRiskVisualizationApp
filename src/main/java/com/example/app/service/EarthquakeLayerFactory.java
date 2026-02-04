package com.example.app.service;

import org.springframework.stereotype.Component;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.dto.layer.EarthquakeLayerDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EarthquakeLayerFactory {

	// LayerDto専用Factory
	private final MeshPolygonFactory meshPolygonFactory;

	public EarthquakeLayerDto create(EarthquakeMesh mesh) {
		EarthquakeLayerDto dto = new EarthquakeLayerDto();
		dto.setMeshCode(mesh.getMeshCode());
		dto.setRiskLevel(mesh.getRiskLevel().name());
		dto.setPolygon(meshPolygonFactory.create(mesh.getMeshCode()));
		return dto;
	}

}
