package com.example.app.dto.area;

import com.example.app.domain.NearestBoundaryResult;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokyoAreaResponse {

	/**********************
	 * 
	 * レスポンスDTO
	 * 
	 * ⇒Serviceが返すDTOをそのまま再利用
	 * 
	 *********************/

	private boolean isTokyo;

	private NearestBoundaryResult nearestBoundary;
}
