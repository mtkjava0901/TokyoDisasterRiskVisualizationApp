package com.example.app.dto.layer;

import java.util.List;

import com.example.app.domain.RiskLevel;
import com.example.app.dto.polygon.LatLngDto;

import lombok.Data;

// APIレスポンス用DTO
// 内部処理用DTOのデータを整形し、DTOをそのままどう描くかを決める

@Data
public class EarthquakeLayerDto {
	
	private String meshCode;
	
	// 計測震度
	private double intensity;
	
	// ENUM : RiskLevel
	private RiskLevel riskLevel;
	
	// GoogleMap Polygon用
	private List<LatLngDto> polygon;

}
