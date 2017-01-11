package hello;

import hello.JobListener.JobCompletionNotificationListener;
import hello.StepListeners.StepListener;

import javax.sql.DataSource;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;


//for tasklet
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.StepContribution;

//for using job operator
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

//to get id for jobOperator to stop execution
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.StepExecution;



@Configuration
@EnableBatchProcessing
public class BatchConfiguration {  


	@Autowired
	public JobRepository jobRepository;

	@Autowired
	public JobRegistry jobRegistry;

	@Autowired
	public JobLauncher jobLauncher;

	@Autowired
	public JobExplorer jobExplorer;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	StepListener steplistener;

	public BatchConfiguration(){
		steplistener=new StepListener();
	}


	@Bean
	public SimpleJobOperator simpleJobOperator() {
		SimpleJobOperator simpleJobOperator = new SimpleJobOperator();
		simpleJobOperator.setJobExplorer(jobExplorer);
		simpleJobOperator.setJobLauncher(jobLauncher);
		simpleJobOperator.setJobRegistry(jobRegistry);
		simpleJobOperator.setJobRepository(jobRepository);
		return simpleJobOperator;
	}

	@Bean
	protected Tasklet tasklet(){
		return new Tasklet(){
			@Override
			public RepeatStatus execute(StepContribution contribution,ChunkContext context) {
					StepContext stepContext = context.getStepContext();
					StepExecution stepExecution = stepContext.getStepExecution();
					JobExecution execution = stepExecution.getJobExecution();
					System.out.println("Tasklet running..."+execution.getStatus());
					return RepeatStatus.FINISHED;
				}
		};
	}

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> reader() {
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
		reader.setLineMapper(new DefaultLineMapper<Person>() {{
		    setLineTokenizer(new DelimitedLineTokenizer() {{
			setNames(new String[] { "firstName", "lastName" });
		    }});
		    setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
			setTargetType(Person.class);
		    }});
		}});
		return reader;
		}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer() {
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		return writer;
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener)throws Exception {
		return jobBuilderFactory.get("importUserJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			//use below code for conditional execution of steps
			//.flow(step1())
			//.on("*").to(step2())
			//.end()
			.start(step1(steplistener)).next(step2())
			.build();
	}

	@Bean
	public Step step1(StepListener listener) {
		return stepBuilderFactory.get("step1")
			.<Person, Person> chunk(10)
			.reader(reader())
			.processor(processor())
			.writer(writer())
			.listener(listener)
			.build();
	}
	// end::jobstep[]


	@Bean
	public Job job() throws Exception {
		return jobBuilderFactory.get("job").start(step2()).build();
	}

	@Bean
	protected Step step2() throws Exception {
		return stepBuilderFactory.get("step2").tasklet(tasklet()).build();
	}

}
