package com.example.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.domain.MeshBounds;
import com.example.app.domain.MeshLevel;
import com.example.app.domain.QuantileCalculator;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.polygon.LatLngDto;
import com.example.app.dto.raw.EarthquakeRawDto;
import com.example.app.infrastructure.EarthquakeCsvLoader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/****************************************************************
 * EarthquakeRaw → 各MeshLevel集約（起動時1回だけ）
 *
 * 責務：
 * ・CSV全件読み込み
 * ・各MeshLevel（1次, 2次, 3次）へ集約
 * ・最大intensity計算
 * ・riskLevel（分位点）計算
 * ・結果をキャッシュ
 ****************************************************************/

@Slf4j
@Component
@RequiredArgsConstructor
public class EarthquakeMeshAggregator {

	private final EarthquakeCsvLoader csvLoader;
	private final MeshPolygonFactory meshPolygonFactory;

	// MeshLevelごとのキャッシュ
	private Map<MeshLevel, List<EarthquakeMesh>> cachedMeshesMap = new HashMap<>();

	@PostConstruct
	public void init() {
		log.info("EarthquakeMeshAggregator 初期化開始");

		List<EarthquakeRawDto> raws = csvLoader.load();

		if (raws.isEmpty()) {
			log.warn("Earthquakeデータが0件");
			for (MeshLevel level : MeshLevel.values()) {
				cachedMeshesMap.put(level, List.of());
			}
			return;
		}

		// 各レベルについて集約してキャッシュ化
		for (MeshLevel level : MeshLevel.values()) {
			List<EarthquakeMesh> meshes = aggregateByLevel(raws, level);
			cachedMeshesMap.put(level, meshes);
			log.info("EarthquakeMeshAggregator {} キャッシュ完了: {} 件", level, meshes.size());
		}

		log.info("EarthquakeMeshAggregator 初期化完了");
	}

	private List<EarthquakeMesh> aggregateByLevel(List<EarthquakeRawDto> raws, MeshLevel level) {
		// 1. Raw ⇒ 対象レベル以上の長さをフィルタし、指定レベル文字列に丸める
		// 2. meshCode単位で集約し、最大のintensityを残す
		Map<String, EarthquakeMesh> meshMap = raws.stream()
				.filter(r -> r.getMeshCode() != null && r.getMeshCode().length() >= level.getCodeLength())
				.collect(Collectors.toMap(
						r -> r.getMeshCode().substring(0, level.getCodeLength()),
						r -> {
							EarthquakeMesh m = new EarthquakeMesh();
							m.setMeshCode(r.getMeshCode().substring(0, level.getCodeLength()));
							m.setIntensity(r.getIntensity());
							return m;
						},
						(a, b) -> {
							if (a.getIntensity() >= b.getIntensity()) {
								return a;
							} else {
								// 最大値を保持する（オブジェクトの破棄を減らすため値を上書き）
								b.setMeshCode(a.getMeshCode());
								return b;
							}
						}));

		List<EarthquakeMesh> meshes = List.copyOf(meshMap.values());

		// 分位点ロジックでRiskLevelセット
		applyQuantileRisk(meshes);

		// Bounds(境界)セット
		for (EarthquakeMesh m : meshes) {
			m.setBounds(createBounds(m.getMeshCode()));
		}

		return meshes;
	}

	private void applyQuantileRisk(List<EarthquakeMesh> meshes) {
		if (meshes.isEmpty())
			return;

		List<Double> intensities = meshes.stream()
				.map(EarthquakeMesh::getIntensity)
				.sorted()
				.toList();

		double q33 = QuantileCalculator.percentile(intensities, 0.33);
		double q66 = QuantileCalculator.percentile(intensities, 0.66);

		for (EarthquakeMesh mesh : meshes) {
			double intensity = mesh.getIntensity();
			if (intensity >= q66) {
				mesh.setRiskLevel(RiskLevel.HIGH);
			} else if (intensity >= q33) {
				mesh.setRiskLevel(RiskLevel.MEDIUM);
			} else {
				mesh.setRiskLevel(RiskLevel.LOW);
			}
		}
	}

	private MeshBounds createBounds(String meshCode) {
		List<LatLngDto> polygon = meshPolygonFactory.create(meshCode);
		double minLat = Double.MAX_VALUE;
		double minLng = Double.MAX_VALUE;
		double maxLat = -Double.MAX_VALUE;
		double maxLng = -Double.MAX_VALUE;

		for (LatLngDto p : polygon) {
			if (p.getLat() < minLat) minLat = p.getLat();
			if (p.getLat() > maxLat) maxLat = p.getLat();
			if (p.getLng() < minLng) minLng = p.getLng();
			if (p.getLng() > maxLng) maxLng = p.getLng();
		}

		return new MeshBounds(new GeoPoint(minLat, minLng), new GeoPoint(maxLat, maxLng));
	}

	/*************************************************************
	 * 指定レベルの全mesh取得 (risk判定用)
	 *************************************************************/
	public List<EarthquakeMesh> getAllMeshesByLevel(MeshLevel level) {
		return cachedMeshesMap.getOrDefault(level, List.of());
	}

	/*************************************************************
	 * BBox内mesh取得（レイヤー表示用）
	 *************************************************************/
	public List<EarthquakeMesh> getMeshesInBounds(
			MeshLevel level,
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		return cachedMeshesMap.getOrDefault(level, List.of()).stream()
				.filter(mesh -> intersects(mesh, minLat, maxLat, minLng, maxLng))
				.toList();
	}

	private boolean intersects(
			EarthquakeMesh mesh,
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {

		if (mesh.getBounds() == null)
			return false;

		return mesh.getBounds().intersects(minLat, maxLat, minLng, maxLng);
	}

}
