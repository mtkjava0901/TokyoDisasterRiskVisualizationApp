package com.example.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.app.domain.FloodArea;
import com.example.app.domain.QuantileCalculator;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.dto.layer.FloodLayerDto;
import com.example.app.dto.polygon.LatLngDto;
import com.example.app.infrastructure.FloodGeoJsonLoader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FloodService {

	private final FloodGeoJsonLoader geoJsonLoader;

	/************************
	 * 
	 * 分位点ロジック
	 * （EarthquakeService.applyQuantileRisk と同じ方式）
	 *  
	 ************************/
	private void applyQuantileRisk(List<FloodArea> areas) {
		if (areas.isEmpty())
			return;
		// rankをdouble化してソート
		List<Double> ranks = areas.stream()
				.map(a -> (double) a.getRank())
				.sorted()
				.toList();
		double q33 = QuantileCalculator.percentile(ranks, 0.33);
		double q66 = QuantileCalculator.percentile(ranks, 0.66);
		for (FloodArea area : areas) {
			double rank = area.getRank();
			if (rank >= q66) {
				area.setRiskLevel(RiskLevel.HIGH);
			} else if (rank >= q33) {
				area.setRiskLevel(RiskLevel.MEDIUM);
			} else {
				area.setRiskLevel(RiskLevel.LOW);
			}
		}
	}

	/************************
	 * 
	 * FloodArea ⇒ FloodLayerDto変換
	 * (private)
	 *  
	 ************************/
	private FloodLayerDto toLayerDto(FloodArea area) {
		FloodLayerDto dto = new FloodLayerDto();
		dto.setRank(area.getRank());
		dto.setRiskLevel(area.getRiskLevel());
		// GeoPoint ⇒ LatLngDto変換
		List<LatLngDto> polygon = area.getPolygon().stream()
				.map(p -> new LatLngDto(p.lat(), p.lng()))
				.toList();
		dto.setPolygon(polygon);
		return dto;
	}

	/************************
	 * 
	 * A-02 洪水レイヤー取得
	 * (public)
	 *  
	 ************************/
	public List<FloodLayerDto> getLayer(
			double minLat,
			double maxLat,
			double minLng,
			double maxLng) {
		// 1.GeoJSON読み込み（キャッシュ）
		List<FloodArea> allAreas = geoJsonLoader.load();
		// 2.BBoxフィルタリング
		//   ポリゴンの先頭座標がBBox内にあるかで判定
		List<FloodArea> filtered = allAreas.stream()
				.filter(area -> {
					if (area.getPolygon().isEmpty())
						return false;
					GeoPoint first = area.getPolygon().get(0);
					return first.lat() >= minLat
							&& first.lat() <= maxLat
							&& first.lng() >= minLng
							&& first.lng() <= maxLng;
				})
				.collect(Collectors.toList());
		// 3.分位点でリスク判定
		applyQuantileRisk(filtered);
		// 4.FloodArea ⇒ FloodLayerDto変換
		return filtered.stream()
				.map(this::toLayerDto)
				.toList();
	}

	/************************
	 * 
	 * A-04 洪水リスク判定
	 * (public)
	 *  
	 ************************/
	public FloodArea findFloodArea(GeoPoint point) {
		// 1.全データ読み込み（キャッシュ）
		List<FloodArea> allAreas = geoJsonLoader.load();
		// 2.分位点でリスク判定
		applyQuantileRisk(allAreas);
		// 3.指定座標を含むFloodAreaを検索
		//   複数該当する場合は最もrankが大きい（最深の）ものを返す
		FloodArea selected = null;
		for (FloodArea area : allAreas) {
			if (!area.contains(point))
				continue;
			// 最大rankを優先（最も深い浸水エリア）
			if (selected == null || area.getRank() > selected.getRank()) {
				selected = area;
			}
		}
		return selected;
	}

}
