package com.extension.batch.example.application;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.extension.batch.example.service.CalculateBatch;

@SpringBootApplication
@EnableProcessApplication
@ComponentScan("com.extension.batch.example.*")
public class CalculateBatchApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(CalculateBatchApplication.class);

	@Autowired
	private CalculateBatch calculateBatch;

	public static void main(final String... args) {

		SpringApplication.run(CalculateBatchApplication.class, args);
	}

	@Override
	public void run(@SuppressWarnings("unused") String... args) throws Exception {

		logger.info("Initializing transactional Batch");

		calculateBatch.transactionBatch();

		logger.info("Finishing transactional Batch");
	}

}
