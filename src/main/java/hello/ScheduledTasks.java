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



@Component
public class ScheduledTasks {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job importUserJob;

	@Autowired
	SimpleJobOperator simpleJobOperator;

	@Scheduled(fixedRate = 3000)
	public void runJob() throws Exception {
		JobParameters param = new JobParametersBuilder().addString("JobID",String.valueOf(System.currentTimeMillis())).toJobParameters();
		JobExecution execution = jobLauncher.run(importUserJob, param);
		System.out.println("Exit status : "+execution.getStatus());
	}
}
