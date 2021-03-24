package com.extension.batch.example.service;

import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.extension.batch.CustomBatchBuilder;
import org.camunda.bpm.extension.batch.plugin.CustomBatchHandlerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.extension.batch.example.handler.BatchJobHandler;

@Service
public class CalculateBatchImpl implements CalculateBatch {

	private static final Logger logger = LoggerFactory.getLogger(CalculateBatchImpl.class);

	@Autowired
	private BatchJobHandler batchJobHandler;

	@Autowired
	@Qualifier("batchProcessEngineFactoryBean")
	private ProcessEngineFactoryBean processEngineFactoryBean;

	@Override
	@Transactional
	public void transactionBatch() {

		// Add the new custom batch handler to the process engine plugin
		processEngineFactoryBean.getProcessEngineConfiguration().getProcessEnginePlugins()
				.add(new CustomBatchHandlerPlugin(batchJobHandler));

		// Set the batch handlers to custom processEngineConfiguration
		processEngineFactoryBean.getProcessEngineConfiguration().getBatchHandlers().put(batchJobHandler.getType(),
				batchJobHandler);

		// Set job handlers to custom processEngineConfiguration
		processEngineFactoryBean.getProcessEngineConfiguration().getJobHandlers().put(batchJobHandler.getType(),
				batchJobHandler);

		logger.info("Create new Batch");
		final List<String> simpleStringList = IntStream.range(0, 20)
				.mapToObj(i -> "SomeRandomBatchData_" + UUID.randomUUID()).collect(toList());

		// Execute a custom camunda batch
		Batch batch = CustomBatchBuilder.of(simpleStringList)
				.configuration(processEngineFactoryBean.getProcessEngineConfiguration()).jobHandler(batchJobHandler)
				.create();

		BatchQuery batchQuery = processEngineFactoryBean.getProcessEngineConfiguration().getManagementService()
				.createBatchQuery().batchId(batch.getId());

		// await to know when the Camunda batch has finished
		await().atMost(5, TimeUnit.MINUTES).until(() -> 0 == batchQuery.list().size());

		logger.info("Finish new Batch");

		// Some local database operations coming here
	}

}
