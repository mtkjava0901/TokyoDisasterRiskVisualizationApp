package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	
	/***********************
	 * 
	 * エラーレスポンスDTO
	 * 
	 **********************/
	
	private String code;
  private String message;

}
