package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.domain.FloodMesh;
import com.example.app.domain.MeshCodeCalculator;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.FloodLayerDto;

import lombok.RequiredArgsConstructor;

/****************************************************************
 * 洪水サービス
 *
 * 責務：
 * ・起動時集約済みFloodMeshを取得
 * ・BBoxフィルタ
 * ・DTO変換
 ****************************************************************/

@Service
@RequiredArgsConstructor
public class FloodService {

	private final FloodMeshAggregator floodMeshAggregator;
	private final FloodLayerFactory floodLayerFactory;
	private final MeshCodeCalculator meshCodeCalculator;

	/************************
	 * A-02 洪水レイヤー取得
	 ************************/
	public List<FloodLayerDto> getLayer(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		// 集約済みMesh取得
		List<FloodMesh> meshes = floodMeshAggregator.getMeshesInBounds(
				minLat, maxLat, minLng, maxLng);

		if (meshes.isEmpty()) {
			return List.of();
		}

		// DTO変換（今はBBox無視）
		return floodLayerFactory.createList(meshes);
	}

	/************************
	 * A-04 洪水リスク判定
	 ************************/
	public FloodMesh findFloodArea(GeoPoint point) {

		List<FloodMesh> meshes = floodMeshAggregator.getAllMeshes();

		if (meshes.isEmpty()) {
			return null;
		}

		String meshCode = meshCodeCalculator.toTertiaryMesh(
				point.lat(),
				point.lng());

		return meshes.stream()
				.filter(m -> m.getMeshCode().equals(meshCode))
				.findFirst()
				.orElse(null);
	}
}

//	/************************
//	 * 
//	 * A-02 洪水レイヤー取得
//	 * 
//	 * cachedMesh ⇒ BBox ⇒ DTO 
//	 ************************/
//	public List<FloodLayerDto> getLayer(
//			double minLat,
//			double maxLat,
//			double minLng,
//			double maxLng) {
//
//		// 1.集約済みMesh取得
//		List<FloodArea> meshes = floodMeshAggregator.getAllMeshes();
//
//		if (meshes.isEmpty()) {
//			return List.of();
//		}
//
//		// 2.BBoxフィルタリング(mesh中心で判定、またはmesh boundsで判定)
//		List<FloodMesh> filtered = m.stream()
//				.filter(mesh -> {
//
//					GeoPoint center = mesh.getCenter();
//
//					return center.lat() >= minLat
//							&& center.lat() <= maxLat
//							&& center.lng() >= minLng
//							&& center.lng() <= maxLng;
//				})
//				.collect(Collectors.toList());
//
//		if (filtered.isEmpty()) {
//			return List.of();
//		}
//
//		// 4.Mesh ⇒ FloodLayerDto変換
//		return floodLayerFactory.createList(filtered);
//	}
//
//	/************************
//	 * 
//	 * A-04 洪水リスク判定
//	 * (public)
//	 *  
//	 ************************/
//	public FloodMesh findFloodArea(GeoPoint point) {
//
//		// 1.集約済みMesh取得
//		List<FloodArea> meshes = floodMeshAggregator.getCachedMeshes();
//
//		if (meshes.isEmpty()) {
//			return null;
//		}
//
//		// 3.座標 ⇒ meshCode
//		String meshCode = meshCodeCalculator.toTertiaryMesh(
//				point.lat(),
//				point.lng());
//
//		// 4.mesh検索
//		return meshes.stream()
//				.filter(m -> m.getMeshCode().equals(meshCode))
//				.findFirst()
//				.orElse(null);
//
//	}
//}
