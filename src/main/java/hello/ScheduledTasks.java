package hello;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.context.annotation.Import;



@Component
public class ScheduledTasks {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job importUserJob;

    @Scheduled(fixedRate = 3000)
    public void reportCurrentTime() throws Exception {
	JobParameters param = new JobParametersBuilder().addString("JobID",String.valueOf(System.currentTimeMillis())).toJobParameters();
	JobExecution execution = jobLauncher.run(importUserJob, param);
        System.out.println("The time is now............");
    }
}
