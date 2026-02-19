package com.example.app.dto.layer;

import java.util.List;

import com.example.app.domain.RiskLevel;
import com.example.app.dto.polygon.LatLngDto;

import lombok.Data;

@Data
public class FloodLayerDto {

	/*******************************************
	 * A-02 洪水レイヤーAPI レスポンス用DTO
	 * 地震側の EarthquakeLayerDto に相当
	 ******************************************/

	// A31b_201 浸水深ランク（1〜5）
	private int rank;
	// ENUM : RiskLevel（分位点で判定）
	private RiskLevel riskLevel;
	// GoogleMap Polygon用座標
	private List<LatLngDto> polygon;

}
