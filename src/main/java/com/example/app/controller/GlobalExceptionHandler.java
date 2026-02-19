package com.example.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.app.dto.response.ErrorResponse;
import com.example.app.exception.BoundaryDataNotAvailableException;
import com.example.app.exception.InvalidCoordinateException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**************************
	 * 
	 * 例外をまとめて処理
	 * 
	 *************************/

	@ExceptionHandler(InvalidCoordinateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidCoordinate(InvalidCoordinateException ex) {
		return new ErrorResponse(
				"INVALID_COORDINATE",
				ex.getMessage());
	}

	@ExceptionHandler(BoundaryDataNotAvailableException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ErrorResponse handleBoundaryUnavailable(BoundaryDataNotAvailableException ex) {
		return new ErrorResponse(
				"BOUNDARY_DATA_NOT_AVAILABLE",
				"Boundary data is not available");
	}

}
