package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.app.domain.FloodMesh;
import com.example.app.dto.layer.FloodLayerDto;
import com.example.app.dto.polygon.LatLngDto;

/****************************************************************
 * FloodMesh → FloodLayerDto 変換クラス
 *
 * 役割：
 * ・meshCode → 矩形Polygon生成
 * ・FloodMesh → DTO変換
 *
 * Earthquake側の LayerFactory と同じ責務
 ****************************************************************/

@Component
public class FloodLayerFactory {

	private final MeshPolygonFactory meshPolygonFactory;

	public FloodLayerFactory(MeshPolygonFactory meshPolygonFactory) {
		this.meshPolygonFactory = meshPolygonFactory;
	}

	/*************************************************************
	 * FloodMesh → FloodLayerDto
	 *************************************************************/
	public FloodLayerDto create(FloodMesh mesh) {

		FloodLayerDto dto = new FloodLayerDto();

		dto.setRank(mesh.getMaxRank());
		dto.setRiskLevel(mesh.getRiskLevel());

		// meshCode → 矩形Polygon生成
		List<LatLngDto> polygon = meshPolygonFactory.create(mesh.getMeshCode());

		dto.setPolygon(polygon);

		return dto;
	}

	/*************************************************************
	 * List<FloodMesh> → List<FloodLayerDto>
	 *************************************************************/
	public List<FloodLayerDto> createList(List<FloodMesh> meshes) {
		return meshes.stream()
				.map(this::create)
				.toList();
	}

}
