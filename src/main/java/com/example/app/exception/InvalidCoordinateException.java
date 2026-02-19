package com.example.app.exception;

public class InvalidCoordinateException extends RuntimeException {

	/**************************
	 * 
	 * 無効な座標の例外クラス
	 * 
	************************ */
	
	public InvalidCoordinateException(String message) {
		super(message);
	}

}
