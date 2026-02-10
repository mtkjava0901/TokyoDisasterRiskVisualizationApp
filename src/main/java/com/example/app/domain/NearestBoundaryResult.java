package com.example.app.domain;

import lombok.Data;

@Data
public class NearestBoundaryResult {
	
	/***************************************
	 * 
	 * 最近接境界算出用戻り値
	 * 緯度・経度・距離計(distanceMeter)
	 * 
	 **************************************/
	
	private final double lat;
	
	private final double lng;
	
	private final double distanceMeter;

}
