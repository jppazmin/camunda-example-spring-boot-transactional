package com.extension.batch.example.handler;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchJobHandler extends CustomBatchJobHandler<String> {

	private static final Logger logger = LoggerFactory.getLogger(BatchJobHandler.class.getSimpleName());

	public static final String TYPE = "simple-batch-handler";

	@Autowired
	private RuntimeService runtimeService;

	@Override
	public void execute(List<String> data, @SuppressWarnings("unused") CommandContext commandContext) {

		data.forEach(entry -> {

			logger.info("Working on data: {}", entry);

			runtimeService.startProcessInstanceByKey("loanApproval");
		});
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
