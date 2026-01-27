package com.example.app.domain;

import lombok.Data;

// 意味変換 ⇒ 内部処理用DTO
// 「このアプリとしてどう扱うか」を表現
// ※後にロジックを持たせる場合@Dataを外す

@Data
public class EarthquakeMesh {

	private String meshCode;

	private double intensity;

	private RiskLevel riskLevel; // 仮ENUM

}
