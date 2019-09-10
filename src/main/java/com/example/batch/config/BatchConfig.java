package com.example.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
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

import com.example.batch.beans.Person;
import com.example.batch.beans.Website;
import com.example.batch.listeners.JobCompletionNotificationListener;
import com.example.batch.processors.PersonItemProcessor;
import com.example.batch.tasks.MyTaskOne;
import com.example.batch.tasks.MyTaskTwo;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Value("input/data-*.csv")
	private Resource[] resources;

	// demo empty job begins
	@Bean
	public Step stepOne() {
		return stepBuilderFactory.get("stepOne").tasklet(new MyTaskOne()).build();
	}

	@Bean
	public Step stepTwo() {
		return stepBuilderFactory.get("stepTwo").tasklet(new MyTaskTwo()).build();
	}

	@Bean
	public Job demoJob() {
		return jobBuilderFactory.get("demoJob").incrementer(new RunIdIncrementer()).start(stepOne()).next(stepTwo())
				.build();
	}
	// demo empty job ends

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>().name("personItemReader")
				.resource(new ClassPathResource("sample-data.csv")).delimited()
				.names(new String[] { "firstName", "lastName" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				}).build();
	}

	// tag::intermediate process
	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)").dataSource(dataSource)
				.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener).flow(step1)
				.end().build();
	}

	@Bean
	public Step step1(JdbcBatchItemWriter<Person> writer) {
		return stepBuilderFactory.get("step1").<Person, Person>chunk(10).reader(reader()).processor(processor())
				.writer(writer).build();
	}
	// end::jobstep[]

	/* Reading input for multiple resources(CSVs) Job starts */
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

	@Bean
	public Job multiInputReaderJob() {
		return jobBuilderFactory.get("multiInputReaderJob").incrementer(new RunIdIncrementer())
				.flow(multiInputReaderStep()).end().build();
	}

	@Bean
	public Step multiInputReaderStep() {
		return stepBuilderFactory.get("multiInputReaderStep").<Website, Website>chunk(10)
				.reader(multiResourceItemReader()).writer(csvWriter()).build();
	}
	/* Reading input for multiple resources(CSVs) Job ends */
}