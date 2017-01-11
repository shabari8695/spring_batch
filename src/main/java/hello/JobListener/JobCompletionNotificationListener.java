package hello.JobListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;

import org.springframework.batch.core.JobInstance;

import hello.Person;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	@Autowired
        private JobExplorer jobExplorer;
        @Autowired
        private JobRepository jobRepository;

	@Autowired
	SimpleJobOperator simpleJobOperator;

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void beforeJob(JobExecution jobExecution){

		//could be used to start timmer...cpu usage level		
	
		System.out.println("Job starting.....");
	}

	@Override
	public void afterJob(JobExecution jobExecution) {

		//could be used to get 			

		String arr[]=jobExecution.getJobParameters().toString().split("\\W");
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			System.out.println("Job finished.....Checking file content  "+jobExecution.getJobInstance().getId().toString());
			List<Person> results = jdbcTemplate.query("SELECT first_name, last_name FROM people", new RowMapper<Person>() {
				@Override
				public Person mapRow(ResultSet rs, int row) throws SQLException {
					return new Person(rs.getString(1), rs.getString(2));
				}
			});

			for (Person person : results) {
				System.out.println("Found <" + person + "> in the database.");
			}

			

		try{	
			List<JobInstance> instances = jobExplorer.getJobInstances("importUserJob", 0, 10);
            		System.out.println("Explorer Size : " + instances.size());
            		for(JobInstance instance:instances) {
			        List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
			        System.out.println("Executions size : " + executions.size()+"   "+instance.getId());
					if(executions.size() > 0) {
		            			jobExecution = executions.get(executions.size() - 1);
						long id=simpleJobOperator.restart(instance.getId());
					}
			}
		}catch(JobInstanceAlreadyCompleteException e){System.out.println("JobInstanceAlreadyCompleteException");
		}catch(NoSuchJobExecutionException e){System.out.println("NoSuchJobExecutionException");
		}catch(NoSuchJobException e){System.out.println("NoSuchJobException ");
		}catch(JobRestartException e){System.out.println("JobRestartException");
		}catch(JobParametersInvalidException e){System.out.println("JobParametersInvalidException");
		}
		}
	}
}
