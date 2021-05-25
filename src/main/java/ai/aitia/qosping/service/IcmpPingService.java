package ai.aitia.qosping.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.aitia.qosping.service.task.IcmpPingJob;
import ai.aitia.qosping.service.task.queue.IcmpPingJobQueue;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;

@Service
public class IcmpPingService {
	
	//=================================================================================================
	// methods
	
	@Autowired
	private IcmpPingJobQueue jobQueue;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK enrollRequest(final IcmpPingRequestDTO request) {
		//TODO validate
		
		final IcmpPingJob job = new IcmpPingJob(UUID.randomUUID(), request.getHost(), request.getTtl(), request.getPacketSize(), request.getTimeout(), request.getTimeToRepeat());
		jobQueue.put(job);
		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(job.getJobId());
		return ack;
	}
}
