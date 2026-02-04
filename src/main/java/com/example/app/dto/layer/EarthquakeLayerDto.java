package com.example.app.dto.layer;

import java.util.List;

import com.example.app.dto.polygon.LatLngDto;

import lombok.Data;

// APIレスポンス用DTO
// 内部処理用DTOのデータを整形し、DTOをそのままどう描くかを決める

@Data
public class EarthquakeLayerDto {
	
	private String meshCode;
	
	private String riskLevel; // フロント用文字列
	
	// GoogleMap Polygon用
	private List<LatLngDto> polygon;

}
