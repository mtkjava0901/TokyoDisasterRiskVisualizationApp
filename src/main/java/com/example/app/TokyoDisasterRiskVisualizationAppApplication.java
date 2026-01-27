package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(
		// 強制無効化（暫定）
		exclude = { DataSourceAutoConfiguration.class }
		)
public class TokyoDisasterRiskVisualizationAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TokyoDisasterRiskVisualizationAppApplication.class, args);
	}

}
