package com.example.app.domain;

import java.util.Arrays;

import com.example.app.exception.UnsupportedMeshCodeException;

public enum MeshLevel {
	// Polygon 計算･地理ロジック専用

	PRIMARY(4), // 一次メッシュ
	SECONDARY(6), // 二次メッシュ
	TERTIARY(8); // 三次メッシュ

	private final int codeLength;

	MeshLevel(int codeLength) {
		this.codeLength = codeLength;
	}

	public int getCodeLength() {
		return codeLength;
	}

	/*******************************************
	 * 
	 * meshCode(例:"533946")からMeshLevelを判定
	 * 
	 ******************************************/
	public static MeshLevel from(String meshCode) {
		return Arrays.stream(values())
				.filter(l -> l.codeLength == meshCode.length())
				.findFirst()
				.orElseThrow(() -> new UnsupportedMeshCodeException(meshCode));
	}

	/*******************************************
	 * 
	 * APIなどで渡されるmeshLevel(4/6/8)から判定
	 * 
	 ******************************************/
	public static MeshLevel fromCodeLength(int codeLength) {
		return Arrays.stream(values())
				.filter(l -> l.codeLength == codeLength)
				.findFirst()
				.orElseThrow(() -> new UnsupportedMeshCodeException(codeLength));
	}
}
