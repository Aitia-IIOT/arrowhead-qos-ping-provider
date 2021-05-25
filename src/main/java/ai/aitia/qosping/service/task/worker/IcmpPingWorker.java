package ai.aitia.qosping.service.task.worker;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import ai.aitia.qosping.service.publish.Publisher;
import ai.aitia.qosping.service.task.IcmpPingJob;
import eu.arrowhead.common.dto.shared.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class IcmpPingWorker implements Runnable {
	
	//=================================================================================================
	// members
	
	private final IcmpPingJob job;
	
	@Autowired
	private Publisher publisher;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IcmpPingWorker(final IcmpPingJob job) {
		this.job = job;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		final FinishedMonitoringMeasurementEventDTO result = new FinishedMonitoringMeasurementEventDTO();
		//TODO
		try {
			publisher.publish(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT, result);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
