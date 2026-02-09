package com.example.app.domain;

// lat / lngを明示的に分ける
// x= lng(経度), y= lat(緯度)として扱う

public record GeoPoint (double lat, double lng) {

}
