package com.example.app.domain;

import java.util.List;

public class QuantileCalculator {

	/**************************************
	 * 
	 * QuantileCalculator = 分位数計算器
	 * 分位点(HIGH/MEDIUM/LOW)の計算用ユーティリティ
	 * 
	************************************ */

	public static double percentile(List<Double> sortedValues, double quantile) {
		if (sortedValues.isEmpty()) {
			throw new IllegalArgumentException("values is empty");
		}

		int n = sortedValues.size();
		double index = quantile * (n - 1);
		int lower = (int) Math.floor(index);
		int upper = (int) Math.ceil(index);

		if (lower == upper) {
			return sortedValues.get(lower);
		}

		double weight = index - lower;
		return sortedValues.get(lower) * (1 - weight)
				+ sortedValues.get(upper) * weight;
	}

}
