package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.batch.tasks.MyTaskOne;
import com.example.batch.tasks.MyTaskTwo;

/**
 * This is an empty demo job with two stops that use tasklets.
 * 
 */
@Configuration
public class DemoBatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Step stepOne() {
		return stepBuilderFactory.get("stepOne").tasklet(new MyTaskOne()).build();
	}

	@Bean
	public Step stepTwo() {
		return stepBuilderFactory.get("stepTwo").tasklet(new MyTaskTwo()).build();
	}

	@Bean(name = "demoJob")
	public Job demoJob() {
		return jobBuilderFactory.get("demoJob").incrementer(new RunIdIncrementer()).start(stepOne()).next(stepTwo())
				.build();
	}

}