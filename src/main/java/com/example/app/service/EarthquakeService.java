package com.example.app.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.app.domain.EarthquakeMesh;
import com.example.app.domain.MeshLevel;
import com.example.app.domain.QuantileCalculator;
import com.example.app.domain.RiskArea;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.EarthquakeLayerDto;
import com.example.app.dto.polygon.LatLngDto;
import com.example.app.dto.raw.EarthquakeRawDto;
import com.example.app.exception.UnsupportedMeshCodeException;
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
		return mesh;
	}

	// List変換メソッド
	public List<EarthquakeMesh> convertList(List<EarthquakeRawDto> raws) {
		return raws.stream()
				.map(this::convert)
				.collect(Collectors.toList());
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

	// 分位点ロジック
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

	// Service内部用変換ルール（外部利用しないのでprivate）
	private EarthquakeLayerDto toLayerDto(EarthquakeMesh mesh) {
		// API返却用DTOを新規作成
		EarthquakeLayerDto dto = new EarthquakeLayerDto();
		dto.setMeshCode(mesh.getMeshCode());
		dto.setRiskLevel(mesh.getRiskLevel());
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

		// 1.CSV読み込み
		List<EarthquakeRawDto> raws = csvLoader.load();

		// 2.Raw ⇒ Mesh ⇒ メッシュ単位で集約
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

		// 3.分位点の計算
		applyQuantileRisk(meshes);

		// 4.Mesh⇒LayerDto(Polygon生成)
		return meshes.stream()
				.map(layerFactory::create)
				.toList();

	}

	/************************
	 * 
	 * 既存privateメソッド ⇒ 地震リスク判定API用に加工
	 * (public)
	 *  
	************************/

	public List<RiskArea> getAllRiskAreas() {
		// 1.全データ読み込み
		List<EarthquakeRawDto> raws = csvLoader.load();

		// 2.Meshに変換（A-01 convertList流用）
		List<EarthquakeMesh> meshes = convertList(raws);

		// 3.リスク判定（既存applyQuantileRiskを利用）
		applyQuantileRisk(meshes);

		// 4.RiskArea（判定用ドメイン）に変換
		// 基準とするメッシュレベルを定義(3次メッシュ)
		MeshLevel targetLevel = MeshLevel.TERTIARY;

		return meshes.stream()
				.map(m -> {
					String code = m.getMeshCode();

					// MeshLevelの定義に基づいて切り詰め
					if (code != null && code.length() > targetLevel.getCodeLength()) {
						code = code.substring(0, targetLevel.getCodeLength());
					}

					// 切り詰めた結果、そのコードが MeshLevel の定義（4, 6, 8桁）の
					// いずれかに一致するかチェックしてから Factory に渡す
					try {
						// MeshLevel.from(code) が通るか確認（想定外の桁数ならここで例外が出る）
						MeshLevel.from(code);

						List<LatLngDto> dtos = meshPolygonFactory.create(code);
						List<GeoPoint> geoPoints = dtos.stream()
								.map(dto -> new GeoPoint(dto.getLat(), dto.getLng()))
								.toList();

						return new RiskArea(geoPoints, m.getRiskLevel());
					} catch (UnsupportedMeshCodeException e) {
						// もし切り詰めてもなお不正な桁数（例：3桁など）の場合はスキップ
						return null;
					}
				})
				.filter(java.util.Objects::nonNull) // null（スキップ分）を除外
				.toList();
	}

	/************************
	 * 
	 * A03にmeshLevelの対応を追加
	 * (public)
	 *  
	************************/

	public List<RiskArea> getRiskAreasByMeshLevel(MeshLevel meshLevel) {

		List<EarthquakeRawDto> raws = csvLoader.load();

		Map<String, EarthquakeMesh> meshMap = raws.stream()
				.map(this::convert)
				.filter(m -> m.getMeshCode().length() >= meshLevel.getCodeLength())
				.map(m -> {
					m.setMeshCode(
							m.getMeshCode().substring(0, meshLevel.getCodeLength()));
					return m;
				})
				.collect(Collectors.toMap(
						EarthquakeMesh::getMeshCode,
						m -> m,
						(a, b) -> a.getIntensity() >= b.getIntensity() ? a : b));

		List<EarthquakeMesh> meshes = List.copyOf(meshMap.values());

		applyQuantileRisk(meshes);

		return meshes.stream()
				.map(m -> {
					List<LatLngDto> dtos = meshPolygonFactory.create(m.getMeshCode());

					List<GeoPoint> geoPoints = dtos.stream()
							.map(dto -> new GeoPoint(dto.getLat(), dto.getLng()))
							.toList();

					return new RiskArea(geoPoints, m.getRiskLevel());
				})
				.toList();
	}

}
