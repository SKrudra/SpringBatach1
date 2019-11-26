package com.example.batch.config;

import java.net.MalformedURLException;
import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.example.batch.beans.Website;
import com.example.batch.partitions.RangePartitioner;

@Configuration
public class PartitionerConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private SimpleAsyncTaskExecutor taskExecutor;

	@Autowired
	private RangePartitioner rangePartitioner;

	@Bean(name = "partitionerJob")
	public Job partitionerJob() throws UnexpectedInputException, MalformedURLException, ParseException {
		return jobBuilderFactory.get("partitioningJob").start(partitionStep()).build();
	}

	/*
	 * @Bean public RangePartitioner partitioner() { RangePartitioner partitioner =
	 * new RangePartitioner(); Resource[] resources; try { resources =
	 * resoursePatternResolver.getResources("file:src/main/resources/input/*.csv");
	 * } catch (IOException e) { throw new
	 * RuntimeException("I/O problems when resolving" + " the input file pattern.",
	 * e); } partitioner.setResources(resources); return partitioner; }
	 */

	@Bean
	public Step partitionStep() throws UnexpectedInputException, MalformedURLException, ParseException {
		return stepBuilderFactory.get("partitionStep").partitioner("slaveStep", rangePartitioner).step(slaveStep())
				.taskExecutor(taskExecutor).build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<Website> partitionerReader() {
		return new FlatFileItemReaderBuilder<Website>().name("websiteItemReader")
				.resource(new ClassPathResource("sample-data.csv")).delimited().names(new String[] { "id", "url" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Website>() {
					{
						setTargetType(Website.class);
					}
				}).build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<Website> partitionerCsvWriter() {
		return new FlatFileItemWriterBuilder<Website>().name("websiteItemWriter")
				.resource(new ClassPathResource("output/" + new Date().toString() + "web.csv")).append(true)
				.lineAggregator(new DelimitedLineAggregator<Website>() {
					{
						setDelimiter(",");
						setFieldExtractor(new BeanWrapperFieldExtractor<Website>() {
							{
								setNames(new String[] { "id", "url" });
							}
						});
					}
				}).build();
	}

	@Bean
	public Step slaveStep() throws UnexpectedInputException, MalformedURLException, ParseException {
		return stepBuilderFactory.get("slaveStep").<Website, Website>chunk(1).reader(partitionerReader())
				.writer(partitionerCsvWriter()).build();
	}
}
