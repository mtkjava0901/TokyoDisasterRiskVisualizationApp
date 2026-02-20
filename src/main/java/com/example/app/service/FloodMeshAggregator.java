package com.example.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.example.app.domain.FloodArea;
import com.example.app.domain.FloodMesh;
import com.example.app.domain.MeshCodeCalculator;
import com.example.app.domain.QuantileCalculator;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.infrastructure.FloodGeoJsonLoader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/****************************************************************
 * FloodArea → FloodMesh集約（起動時1回だけ）
 *
 * 責務：
 * ・GeoJSON全件読み込み
 * ・3次meshへ集約
 * ・maxRank計算
 * ・riskLevel計算
 * ・結果をキャッシュ
 ****************************************************************/

@Slf4j
@Component
@RequiredArgsConstructor
public class FloodMeshAggregator {

	private final FloodGeoJsonLoader geoJsonLoader;
	private final MeshCodeCalculator meshCodeCalculator;
	private List<FloodMesh> cachedMeshes = List.of();

	/*************************************************************
	 * アプリ起動時に一度だけ実行
	 *************************************************************/
	@PostConstruct
	public void init() {
		log.info("FloodMeshAggregator 初期化開始");

		List<FloodArea> areas = geoJsonLoader.load();

		if (areas.isEmpty()) {
			log.warn("FloodAreaが0件");
			cachedMeshes = List.of();
			return;
		}

		// 全件を一度だけ集約しキャッシュ
		cachedMeshes = aggregateInternal(areas);

		log.info("FloodMeshAggregator 初期化完了 mesh数={}", cachedMeshes.size());
	}

	/*************************************************************
	 * FloodArea一覧 → FloodMesh一覧 (内部専用)
	 *************************************************************/
	private List<FloodMesh> aggregateInternal(List<FloodArea> areas) {

		// meshCode → FloodMesh
		Map<String, FloodMesh> meshMap = new HashMap<>();

		for (FloodArea area : areas) {

			if (area.getPolygon().isEmpty())
				continue;

			// ① ポリゴン中心を取得（簡易：平均値）
			GeoPoint centroid = calculateCentroid(area.getPolygon());

			// ② メッシュコード算出
			String meshCode = meshCodeCalculator.toTertiaryMesh(
					centroid.lat(),
					centroid.lng());

			// ③ メッシュ単位で集約
			FloodMesh mesh = meshMap.computeIfAbsent(
					meshCode,
					code -> {
						FloodMesh m = new FloodMesh(code);
						m.setBounds(meshCodeCalculator.toBounds(code)); // ⭐追加
						return m;
					});

			// 最大rank更新
			mesh.updateRank(area.getRank());
		}

		List<FloodMesh> meshes = List.copyOf(meshMap.values());

		// riskLevelを計算
		applyQuantileRisk(meshes);

		return meshes;
	}

	/*************************************************************
	 * 分位点リスク計算
	 *************************************************************/
	private void applyQuantileRisk(List<FloodMesh> meshes) {

		if (meshes.isEmpty())
			return;

		// rankをdoubleにしてソート
		List<Double> ranks = meshes.stream()
				.map(m -> (double) m.getMaxRank())
				.sorted()
				.toList();

		double q33 = QuantileCalculator.percentile(ranks, 0.33);
		double q66 = QuantileCalculator.percentile(ranks, 0.66);

		for (FloodMesh mesh : meshes) {

			double rank = mesh.getMaxRank();

			if (rank >= q66) {
				mesh.setRiskLevel(RiskLevel.HIGH);
			} else if (rank >= q33) {
				mesh.setRiskLevel(RiskLevel.MEDIUM);
			} else {
				mesh.setRiskLevel(RiskLevel.LOW);
			}
		}
	}

	/*************************************************************
	 * 全mesh取得 (risk判定等で使用)
	 *************************************************************/
	public List<FloodMesh> getAllMeshes() {
		return cachedMeshes;
	}

	/*************************************************************
	 * BBox内mesh取得（レイヤー表示用）
	 * 
	 * ・bounds交差判定入り
	 *************************************************************/
	public List<FloodMesh> getMeshesInBounds(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		// 全件返す
		return cachedMeshes.stream()
				.filter(mesh -> intersects(mesh, minLat, maxLat, minLng, maxLng))
				.toList();
	}

	/*************************************************************
	 * mesh bounds と BBox の交差判定
	 *
	 * 交差条件：
	 * ・緯度が重なる
	 * ・経度が重なる
	 *************************************************************/
	private boolean intersects(
			FloodMesh mesh,
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		if (mesh.getBounds() == null)
			return false;

		var bounds = mesh.getBounds();

		// mesh bounds
		GeoPoint sw = bounds.getSouthWest();
		GeoPoint ne = bounds.getNorthEast();

		double meshMinLat = sw.lat();
		double meshMaxLat = ne.lat();
		double meshMinLng = sw.lng();
		double meshMaxLng = ne.lng();

		// 緯度が重なるか
		boolean latOverlap = meshMaxLat >= minLat &&
				meshMinLat <= maxLat;

		// 経度が重なるか
		boolean lngOverlap = meshMaxLng >= minLng &&
				meshMinLng <= maxLng;

		return latOverlap && lngOverlap;
	}

	/*************************************************************
	 * ポリゴン中心（簡易平均）
	 *
	 * ※将来改善可能（重心計算）
	 *************************************************************/
	private GeoPoint calculateCentroid(List<GeoPoint> polygon) {

		double latSum = 0;
		double lngSum = 0;

		for (GeoPoint p : polygon) {
			latSum += p.lat();
			lngSum += p.lng();
		}

		return new GeoPoint(
				latSum / polygon.size(),
				lngSum / polygon.size());
	}

}
