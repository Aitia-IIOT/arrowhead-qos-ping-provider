package ai.aitia.qosping.service.task.worker;

import java.util.function.Function;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ai.aitia.qosping.service.task.IcmpPingJob;

@Configuration
public class WorkerFactory {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public IcmpPingWorker createIcmpPingWorker(final IcmpPingJob job) {
		return new IcmpPingWorker(job);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	public Function<IcmpPingJob,IcmpPingWorker> icmpPingWorkerFactory() {
		return job -> createIcmpPingWorker(job);
	}
}
