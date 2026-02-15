// package com.example.app.config;

/**********************************
 * 
 * 地震リスクデータ供給用コンフィグ
 * 
 * ※一旦無使用予定
 * 
 *********************************/

/*
@Configuration
@RequiredArgsConstructor
public class EarthquakeRiskDataConfig {

	// private final EarthquakeCsvLoader csvLoader;
	private final EarthquakeService earthquakeService;

	//	public EarthquakeRiskDataConfig(EarthquakeCsvLoader csvLoader) {
	//		this.csvLoader = csvLoader;
	//	}

	// このメソッドの戻り値を「アプリ共有のデータ」として登録する
	@Bean
	public List<RiskArea> earthquakeRiskAreas() {
		// 1.CsvLoader使ってRawデータを取得
		List<RiskArea> masterData = earthquakeService.getAllRiskAreas();

		System.out.println("Config: 地震リスクデータをロードしました。件数: " + masterData.size());
		return masterData;
*/

// 2.RawデータをRiskArea(内部ドメイン)に変換
//		return rawData.stream()
//				.map(raw -> {
//					// ここでメッシュコードからポリゴンを作成
//					List<GeoPoint> polygon = convertMeshToPolygon(raw.getMeshCode());
//					// ここで震度からRiskLevelを判定
//					RiskLevel level = calculateRiskLevel(raw.getIntensity());
//
//					return new RiskArea(polygon, level);
//				})
//				.collect(Collectors.toList());
//}
//}
