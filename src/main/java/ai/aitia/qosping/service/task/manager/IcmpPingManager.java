package ai.aitia.qosping.service.task.manager;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ai.aitia.qosping.service.publish.Publisher;
import ai.aitia.qosping.service.task.IcmpPingJob;
import ai.aitia.qosping.service.task.queue.IcmpPingJobQueue;
import ai.aitia.qosping.service.task.worker.IcmpPingWorker;
import eu.arrowhead.client.skeleton.provider.Constant;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.StartedMonitoringMeasurementEventDTO;

@Component
public class IcmpPingManager extends Thread {

	//=================================================================================================
	// members
	
	@Autowired
	private Publisher publisher;
	
	@Autowired
	private IcmpPingJobQueue jobQueue;
	
	@Autowired
	private Function<IcmpPingJob,IcmpPingWorker> icmpPingWorkerFactory;
	
	@Value(Constant.$THREAD_NUM_ICMP_PING_WORKER_WD)
	private int threadNum;
	
	private ThreadPoolExecutor threadPool;
	
	private boolean doWork = false;
	
	private final Logger logger = LogManager.getLogger(IcmpPingManager.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		logger.trace("IcmpPingManager.run started...");
		
		if (doWork) {
			throw new UnsupportedOperationException("IcmpPingManager is already started");
		}
		setName(IcmpPingManager.class.getSimpleName());
		
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum);
		doWork = true;
		
		while (doWork) {
			try {
				final IcmpPingJob job = jobQueue.take();
				threadPool.execute(icmpPingWorkerFactory.apply(job));
				
				final StartedMonitoringMeasurementEventDTO event = new StartedMonitoringMeasurementEventDTO();
				event.setEventType(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT);
				event.setMetaData(Map.of(Constant.PROCESS_ID, job.getJobId().toString()));
				event.setTimeStamp(ZonedDateTime.now());
				publisher.publish(event.getEventType(), event);
				
			} catch (final InterruptedException ex) {
				logger.error(ex);
				logger.debug(ex.getMessage(), ex);
				interrupt();
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		doWork = false;
		super.interrupt();
	}
}
