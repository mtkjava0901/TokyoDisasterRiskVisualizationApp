package com.example.app.dto.raw;

import lombok.Data;

// CSVに書いてあること（50mメッシュコード,計測震度）をそのまま伝える
// ＝ 意味の変換

@Data
public class EarthquakeRawDto {
	
	private String meshCode; // 50mメッシュコード
	
	private double intensity; // 震度

}
