package com.extension.batch.example.configuration;

import java.util.Collections;

import javax.sql.DataSource;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.extension.batch.plugin.CustomBatchHandlerPlugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.extension.batch.example.handler.BatchJobHandler;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ConfigureEngine {

	@Value("${camunda.bpm.application.isDeployChangedOnly}")
	private boolean deployChangeOnly;
	@Value("${camunda.bpm.job-execution.enabled}")
	private boolean jobExecution;
	@Value("${camunda.bpm.metrics.enabled}")
	private boolean metricsEnabled;
	@Value("${camunda.bpm.metrics.db-reporter-activate}")
	private boolean metricsDbReporterActive;
	@Value("${camunda.bpm.generic-properties.properties.batchJobsPerSeed}")
	private int batchJobsPerSeed;
	@Value("${camunda.bpm.generic-properties.properties.invocationsPerBatchJob}")
	private int invocationsPerBatchJob;
	@Value("${camunda.bpm.database.type}")
	private String databaseType;
	@Value("${camunda.bpm.database.jdbc-batch-processing}")
	private boolean jdbcBatchProcessing;
	@Value("${camunda.bpm.history-level}")
	private String historyLevel;

	private ApplicationContext applicationContext;

	public ConfigureEngine(ApplicationContext applicationContext) {

		this.applicationContext = applicationContext;
	}

	@Bean(name = "batch-camunda-ds")
	@ConfigurationProperties(prefix = "spring.datasource.primary.hikari")
	public DataSource dataSource() {

		return new HikariDataSource();
	}

	@Bean(name = "batchTransactionManager")
	public PlatformTransactionManager batchTransactionManager(
			@Qualifier("batch-camunda-ds") final DataSource dataSource) {

		return new DataSourceTransactionManager(dataSource);
	}

	@Bean(name = "batchProcessEngineConfiguration")
	public SpringProcessEngineConfiguration processEngineConfiguration(
			@Qualifier("batch-camunda-ds") final DataSource dataSource,
			@Qualifier("batchTransactionManager") final PlatformTransactionManager ptm) {

		SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
		springProcessEngineConfiguration.setApplicationContext(applicationContext);
		springProcessEngineConfiguration.setDataSource(dataSource);
		springProcessEngineConfiguration.setTransactionManager(ptm);

		springProcessEngineConfiguration.setDeployChangedOnly(deployChangeOnly);
		springProcessEngineConfiguration.setJobExecutorActivate(jobExecution);
		springProcessEngineConfiguration.setMetricsEnabled(metricsEnabled);
		springProcessEngineConfiguration.setDbMetricsReporterActivate(metricsDbReporterActive);
		springProcessEngineConfiguration.setBatchJobsPerSeed(batchJobsPerSeed);
		springProcessEngineConfiguration.setInvocationsPerBatchJob(invocationsPerBatchJob);
		springProcessEngineConfiguration.setDatabaseType(databaseType);
		springProcessEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		springProcessEngineConfiguration.setJdbcBatchProcessing(jdbcBatchProcessing);
		springProcessEngineConfiguration.setJdbcMaxActiveConnections(6000);
		springProcessEngineConfiguration.setHistory(historyLevel);

		return springProcessEngineConfiguration;
	}

	@Bean("batchProcessEngineFactoryBean")
	public ProcessEngineFactoryBean processEngineFactoryBean(
			@Qualifier("batchProcessEngineConfiguration") final SpringProcessEngineConfiguration springProcessEngineConfiguration) {

		ProcessEngineFactoryBean pefb = new ProcessEngineFactoryBean();
		pefb.setProcessEngineConfiguration(springProcessEngineConfiguration);

		return pefb;
	}

	@Primary
	@Bean("batchProcessEngine")
	public ProcessEngine batchProcessEngine(
			@Qualifier("batchProcessEngineFactoryBean") final ProcessEngineFactoryBean pefb) {

		return pefb.getProcessEngineConfiguration().buildProcessEngine();
	}

	@Bean("batchRuntimeService")
	public RuntimeService batchRuntimeService(
			@Qualifier("batchProcessEngineFactoryBean") final ProcessEngineFactoryBean pe) {

		return pe.getProcessEngineConfiguration().getRuntimeService();
	}

	@Bean("batchTaskService")
	public TaskService batchTaskService(@Qualifier("batchProcessEngineFactoryBean") final ProcessEngineFactoryBean pe) {

		return pe.getProcessEngineConfiguration().getTaskService();
	}

	@Bean("batchManagementService")
	public ManagementService batchManagementService(
			@Qualifier("batchProcessEngineFactoryBean") final ProcessEngineFactoryBean pe) {

		return pe.getProcessEngineConfiguration().getManagementService();
	}

	@Bean("batchRepositoryService")
	public RepositoryService batchRepositoryService(
			@Qualifier("batchProcessEngineFactoryBean") final ProcessEngineFactoryBean pe) {

		return pe.getProcessEngineConfiguration().getRepositoryService();
	}

	@Bean
	public BatchJobHandler simpleCustomBatchJobHandler() {
		return new BatchJobHandler();
	}

	@Bean
	public ProcessEnginePlugin customBatchHandlerPlugin(BatchJobHandler batchJobHandler) {
		return new CustomBatchHandlerPlugin(Collections.singletonList(batchJobHandler));
	}
}
