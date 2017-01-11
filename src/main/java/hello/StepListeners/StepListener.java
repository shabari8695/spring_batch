package hello.StepListeners;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.ExitStatus;

@Component
public class StepListener extends StepExecutionListenerSupport{

	

	@Override
	public void beforeStep(StepExecution stepExecution){
		
	
		System.out.println("Step starting.....");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution){

		
		System.out.println("Step ended.....");
		return null;
	}
}
