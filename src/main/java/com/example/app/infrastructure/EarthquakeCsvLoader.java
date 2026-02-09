package com.example.app.infrastructure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.app.dto.raw.EarthquakeRawDto;

// CSVファイル読み込み⇒リスト形式で返す
// infrastructure ⇒ 基盤・設備等

@Component
public class EarthquakeCsvLoader {

	// CSVパス定義
	private static final String CSV_PATH = "static/csv/earthquake_risk.csv";

	// CSVを読んでDTOのListを返す
	public List<EarthquakeRawDto> load() {
		List<EarthquakeRawDto> list = new ArrayList<>();

		// try&catch 失敗したらRuntimeException
		try (

				// 1行ずつreadLine()で読めるようにする
				BufferedReader br = new BufferedReader(

						// バイト⇒文字に変換
						new InputStreamReader(

								// CSVをバイトストリームとして取得
								new ClassPathResource(CSV_PATH).getInputStream()))) {

			// ヘッダー行スキップ用フラグ 1行目を飛ばす
			String line;
			boolean isHeader = true;

			// CSVの終わりまで繰り返すループ構文
			while ((line = br.readLine()) != null) {

				// ヘッダ行スキップ
				if (isHeader) {
					isHeader = false;
					continue;
				}

				// カンマ区切りで分割
				String[] values = line.split(",");

				// このクラスの核心部分
				// CSV文字列⇒Java型に変換する
				EarthquakeRawDto dto = new EarthquakeRawDto();
				dto.setMeshCode(values[0]);
				dto.setIntensity(Double.parseDouble(values[1]));

				// リストに追加
				list.add(dto);
			}

			// 例外処理
		} catch (Exception e) {
			throw new RuntimeException("地震CSVの読み込みに失敗しました", e);
		}

		return list;
	}

}
