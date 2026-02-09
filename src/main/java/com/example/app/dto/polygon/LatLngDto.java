package com.example.app.dto.polygon;

import lombok.AllArgsConstructor;
import lombok.Data;

// GoogleMap Polygon用座標指定
@Data
@AllArgsConstructor
public class LatLngDto {

	private double lat;

	private double lng;

}
