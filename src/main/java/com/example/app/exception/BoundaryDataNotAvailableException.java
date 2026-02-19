package com.example.app.exception;

public class BoundaryDataNotAvailableException extends RuntimeException {
	
	/*********************************
	 * 
	 * 境界データの利用不可例外クラス
	 * 
	 ********************************/

	public BoundaryDataNotAvailableException() {
		super("Boundary data is not available");
	}

}
