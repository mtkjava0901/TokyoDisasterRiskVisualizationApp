package com.example.app.infrastructure;

import java.io.InputStream;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.app.domain.GeoPoint;
import com.example.app.domain.PointInPolygon;

@Component
public class TokyoPolygonRepository {

	/*************************************************
	 * 
	 * メモリ常駐リポジトリ
	 * ・起動時に東京都ポリゴンを読み込み (⇒GeoJsonLoader)
	 * ・「この座標東京都か？」を高速判定
	 * 
	 ************************************************/

	// List<List<GeoPoint>> = 東京都の全ポリゴン
	private List<List<GeoPoint>> polygons;

	private static final Logger log = LoggerFactory.getLogger(TokyoPolygonRepository.class);

	// ここでGeoJSONデータの読み込み（起動時1回のみ）
	@PostConstruct
	public void load() {
		try {
			// GeoJSONの読み込み処理
			ClassPathResource resource = new ClassPathResource("static/geo/tokyo_mainland.geojson");

			try (InputStream is = resource.getInputStream()) {
				this.polygons = GeoJsonLoader.loadPolygons(is);
			}

			// 動作確認
			log.info("Tokyo polygons loaded: {}", polygons.size());
			log.debug("Points in first polygon: {}", polygons.get(0).size());

			// 読み込みエラー時
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to load tokyo_mainland.geojson", e);
		}
	}

	// APIの本体　現在の緯度経度の東京都判定
	public boolean contains(double lat, double lng) {

		// 判定対象の点を作る
		GeoPoint point = new GeoPoint(lat, lng);

		// 東京都を構成する全ポリゴンを順番にチェック
		for (List<GeoPoint> polygon : polygons) {
			if (PointInPolygon.contains(polygon, point)) {
				// 一つでも含まれていればtrue
				return true;
			}
		}

		// 一つもなければfalse
		return false;
	}

}
