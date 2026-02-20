package com.example.app.domain;

import com.example.app.domain.area.GeoPoint;

import lombok.Data;

/****************************************************************
 * 洪水メッシュドメイン
 *
 * FloodArea(複雑ポリゴン) → メッシュ単位へ集約した結果
 *
 * 責務：
 * ・1つの3次メッシュの洪水情報を保持
 * ・最大浸水ランクを保持
 * ・代表riskLevelを保持
 ****************************************************************/

@Data
public class FloodMesh {

	/** 3次メッシュコード（8桁） */
	private String meshCode;

	/** メッシュ内最大浸水ランク（1〜5） */
	private int maxRank;

	/** メッシュ代表リスク */
	private RiskLevel riskLevel;

	/** メッシュ境界 */
	private MeshBounds bounds;

	/************************************************************
	 * コンストラクタ
	 ************************************************************/
	public FloodMesh(String meshCode) {
		this.meshCode = meshCode;
		this.maxRank = 0;
	}

	/************************************************************
	 * FloodAreaのrankを集約（最大値を採用）
	 *
	 * aggregatorから呼ばれる
	 ************************************************************/
	public void updateRank(int rank) {
		if (rank > this.maxRank) {
			this.maxRank = rank;
		}
	}

	/************************************************************
	 * 中心取得ショートカット
	 ************************************************************/
	public GeoPoint getCenter() {
		return bounds.getCenter();
	}

	/************************************************************
	 * 境界取得
	 ************************************************************/
	public MeshBounds getBounds() {
		return bounds;
	}

	/************************************************************
	 * 境界セット
	 ************************************************************/
	public void setBounds(MeshBounds bounds) {
		this.bounds = bounds;
	}

	/************************************************************
	 * rank → riskLevel変換
	 *
	 * ※後でFloodMeshAggregatorから呼ぶ
	 ************************************************************/
	public void applyRiskLevel(double q33, double q66) {
		if (maxRank >= q66) {
			this.riskLevel = RiskLevel.HIGH;
		} else if (maxRank >= q33) {
			this.riskLevel = RiskLevel.MEDIUM;
		} else {
			this.riskLevel = RiskLevel.LOW;
		}
	}

	/************************************************************
	 * 深度説明を取得
	 ************************************************************/
	public static String getDepthDescription(int rank) {
		return switch (rank) {
		case 5 -> "5m以上";
		case 4 -> "3〜5m";
		case 3 -> "1〜3m";
		case 2 -> "0.5〜1m";
		case 1 -> "0〜0.5m";
		default -> "不明";
		};
	}

}
