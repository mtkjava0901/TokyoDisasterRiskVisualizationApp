package com.example.app.dto.layer;

import lombok.Data;

// APIレスポンス用DTO
// 内部処理用DTOのデータを整形し、MAP描画できる形にする

@Data
public class EarthquakeLayerDto {
	
	private String meshCode;
	
	private String riskLevel; // フロント用文字列

}
