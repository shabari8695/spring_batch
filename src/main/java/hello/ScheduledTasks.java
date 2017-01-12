package hello;

//for scheduling
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//for running job form program
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

//for running using job operator
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.repository.JobRepository;

import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import java.util.List;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@Component
public class ScheduledTasks {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job importUserJob;
	
	@Autowired
	Job job;

	@Autowired
	SimpleJobOperator simpleJobOperator;

	@Autowired
	public JobRegistry jobRegistry;

	@Autowired
	public JobFactory jobFactory;

	@Autowired
        private JobExplorer jobExplorer;
        
	@Autowired
        private JobRepository jobRepository;


	@Scheduled(fixedRate = 10000)
	public void runJob() throws Exception {
		JobParameters param = new JobParametersBuilder().addString("JobID",String.valueOf(System.currentTimeMillis())).toJobParameters();
		/*AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);

        	ScheduledTasks main = context.getBean(ScheduledTasks.class);
		main.jobRegistry.register(main.jobFactory);
        	long executionId = main.simpleJobOperator.start(main.job.getName(), String.valueOf(System.currentTimeMillis()));*/
		jobRegistry.register(jobFactory);
		JobExecution execution = jobLauncher.run(importUserJob, param);

		try{	
			List<JobInstance> instances = jobExplorer.getJobInstances("importUserJob", 0, 10);
            		System.out.println("Explorer Size : " + instances.size());
            		for(JobInstance instance:instances) {
			        List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
			        System.out.println("Executions size : " + executions.size()+"   "+instance.getId());
					if(executions.size() > 0) {
		            			JobExecution jobExecution = executions.get(executions.size() - 1);
						long id=simpleJobOperator.restart(instance.getId());
						System.out.println("restart id: "+id);
					}
			}
		}catch(JobInstanceAlreadyCompleteException e){System.out.println("JobInstanceAlreadyCompleteException");
		}catch(NoSuchJobExecutionException e){System.out.println("NoSuchJobExecutionException");
		}catch(NoSuchJobException e){System.out.println("NoSuchJobException ");
		}catch(JobRestartException e){System.out.println("JobRestartException");
		}catch(JobParametersInvalidException e){System.out.println("JobParametersInvalidException");
		}		
		jobRegistry.unregister(importUserJob.getName());
		System.out.println("Exit status : "+execution.getStatus());
        	//long executionId = simpleJobOperator.start(job, null);
		//System.out.println(executionId);
		//long restartId = simpleJobOperator.restart(execution.getJobId());
	}
}
