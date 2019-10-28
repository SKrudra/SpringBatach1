package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.example.batch.beans.Website;
import com.example.batch.processors.WebsiteItemProcessor;

/**
 * This job reads input from multiple resources(CSVs) and after processing
 * writes to single destination(CSV).
 * 
 */
@Configuration
public class MultiResourceReaderConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Value("input/data-*.csv")
	private Resource[] resources;

	@Bean
	public FlatFileItemReader<Website> csvReader() {
		return new FlatFileItemReaderBuilder<Website>().name("websiteItemReader").delimited()
				.names(new String[] { "id", "url" }).fieldSetMapper(new BeanWrapperFieldSetMapper<Website>() {
					{
						setTargetType(Website.class);
					}
				}).build();
	}

	@Bean
	public MultiResourceItemReader<Website> multiResourceItemReader() {
		return new MultiResourceItemReaderBuilder<Website>().name("multiResourceItemReader").resources(resources)
				.delegate(csvReader()).build();
	}

	@Bean
	public WebsiteItemProcessor websiteProcessor() {
		return new WebsiteItemProcessor();
	}

	@Bean
	public FlatFileItemWriter<Website> csvWriter() {
		return new FlatFileItemWriterBuilder<Website>().name("websiteItemWriter")
				.resource(new ClassPathResource("output/web.csv")).append(true)
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

	@Bean(name = "multiInputReaderJob")
	public Job multiInputReaderJob() {
		return jobBuilderFactory.get("multiInputReaderJob").incrementer(new RunIdIncrementer())
				.flow(multiInputReaderStep()).end().build();
	}

	@Bean
	public Step multiInputReaderStep() {
		return stepBuilderFactory.get("multiInputReaderStep").<Website, Website>chunk(10)
				.reader(multiResourceItemReader()).processor(websiteProcessor()).writer(csvWriter()).build();
	}
}
