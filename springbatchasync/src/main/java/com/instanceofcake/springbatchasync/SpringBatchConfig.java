package com.instanceofcake.springbatchasync;

import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
//import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.instanceofcake.springbatchasync.entity.Employee;
import com.instanceofcake.springbatchasync.entity.EmployeeResultRowMapper;
import com.instanceofcake.springbatchasync.processor.EmployeeItemProcessor;

@EnableBatchProcessing
@Configuration
public class SpringBatchConfig {
  

  @Autowired
  private DataSource dataSource;


  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public ItemReader<Employee> asyncreader() {
      JdbcPagingItemReaderBuilder jdbcPagingItemReaderBuilder = new JdbcPagingItemReaderBuilder();
      jdbcPagingItemReaderBuilder.name("reader");
      jdbcPagingItemReaderBuilder.dataSource(dataSource);
      jdbcPagingItemReaderBuilder.selectClause("select id, staff_number, name ");
      jdbcPagingItemReaderBuilder.fromClause("FROM student ");
     jdbcPagingItemReaderBuilder.sortKeys(Collections.singletonMap("id", Order.DESCENDING));
      jdbcPagingItemReaderBuilder.rowMapper(new EmployeeResultRowMapper());
      return jdbcPagingItemReaderBuilder.build();
  }

  @Bean
  public ItemProcessor<Employee, Employee> studentItemProcessor() {
      return new EmployeeItemProcessor();
  }

  @Bean
  public ItemProcessor<Employee, Future<Employee>> asyncItemProcessor() {
      AsyncItemProcessor<Employee, Employee> asyncItemProcessor = new AsyncItemProcessor<>();
      asyncItemProcessor.setDelegate(studentItemProcessor());
      asyncItemProcessor.setTaskExecutor(getAsyncExecutor());
      return asyncItemProcessor;
  }

  @Bean
  public ItemWriter<Future<Employee>> asyncItemWriter() {
      AsyncItemWriter<Employee> asyncItemWriter = new AsyncItemWriter<>();
      asyncItemWriter.setDelegate(writer());
      return asyncItemWriter;
  }

  @Bean
  public FlatFileItemWriter<Employee> writer() {
      FlatFileItemWriter<Employee> writer = new FlatFileItemWriter<>();
      writer.setResource(new FileSystemResource("C://data/batch/data.csv"));
      writer.setLineAggregator(getDelimitedLineAggregator());
      return writer;
  }

  private DelimitedLineAggregator<Employee> getDelimitedLineAggregator() {
      BeanWrapperFieldExtractor<Employee> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<Employee>();
      beanWrapperFieldExtractor.setNames(new String[]{"id", "rollNumber", "name"});

      DelimitedLineAggregator<Employee> aggregator = new DelimitedLineAggregator<Employee>();
      aggregator.setDelimiter(",");
      aggregator.setFieldExtractor(beanWrapperFieldExtractor);
      return aggregator;

  }

  @Bean(name = "asyncExecutor")
  public TaskExecutor getAsyncExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(100);
      executor.setMaxPoolSize(100);
      executor.setQueueCapacity(100);
      executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
      executor.setThreadNamePrefix("netsurfingzone 1-");
      return executor;
  }

  @Bean
  public Step asyncStep1() {
      StepBuilder stepBuilder = stepBuilderFactory.get("asyncStep1");
      SimpleStepBuilder<Employee, Future<Employee>> simpleStepBuilder = stepBuilder.chunk(10);
      return simpleStepBuilder.reader(asyncreader()).processor(asyncItemProcessor()).writer(asyncItemWriter()).build();
  }

  @Bean
  public Job asyncJob() {
      JobBuilder jobBuilder = jobBuilderFactory.get("asyncJob");
      jobBuilder.incrementer(new RunIdIncrementer());
      FlowJobBuilder flowJobBuilder = jobBuilder.flow(asyncStep1()).end();
      Job job = flowJobBuilder.build();
      return job;
  }

}
