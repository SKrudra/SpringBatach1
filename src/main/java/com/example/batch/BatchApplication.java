package com.example.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.batch.config.DemoBatchConfig;
import com.example.batch.config.FileToDBBatchConfig;
import com.example.batch.config.MultiResourceReaderConfig;

@EnableScheduling
@SpringBootApplication
public class BatchApplication {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	DemoBatchConfig demoBatchConfig;

	@Autowired
	FileToDBBatchConfig fileToDBBatchConfig;

	@Autowired
	MultiResourceReaderConfig multiResourceReaderConfig;

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	@Scheduled(cron = "0 */1 * * * ?")
	public void schedule() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobId", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(demoBatchConfig.demoJob(), params);

	}

	@Scheduled(cron = "0 */2 * * * ?")
	public void secondSchedule() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobId", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileToDBBatchConfig.importUserJob(), params);
	}

	@Scheduled(cron = "0 */3 * * * ?")
	public void thirdSchedule() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobId", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(multiResourceReaderConfig.multiInputReaderJob(), params);
	}
}
