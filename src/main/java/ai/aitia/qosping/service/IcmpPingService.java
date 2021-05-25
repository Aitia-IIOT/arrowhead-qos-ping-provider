package ai.aitia.qosping.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ai.aitia.qosping.service.publish.Publisher;
import ai.aitia.qosping.service.task.IcmpPingJob;
import ai.aitia.qosping.service.task.queue.IcmpPingJobQueue;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.ReceivedMonitoringRequestEventDTO;

@Service
public class IcmpPingService {
	
	//=================================================================================================
	// methods
	
	@Autowired
	private IcmpPingJobQueue jobQueue;
	
	@Autowired
	private Publisher publisher;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK enrollRequest(final IcmpPingRequestDTO request) {
		Assert.notNull(request, "request is null");
		Assert.isTrue(!Utilities.isEmpty(request.getHost()), "request.host is empty");
		Assert.notNull(request.getTtl(), "request.ttl is null");
		Assert.isTrue(request.getTtl() >= 0, "request.ttl is negative");
		Assert.notNull(request.getPacketSize(), "request.packetSize is null");
		Assert.isTrue(request.getPacketSize() >= 0, "request.packetSize is negative");
		Assert.notNull(request.getTimeout(), "request.timeout is null");
		Assert.isTrue(request.getTimeout() >= 0, "request.timeout is negative");
		Assert.notNull(request.getTimeToRepeat(), "request.timeToRepeat is null");
		Assert.isTrue(request.getTimeToRepeat() >= 0, "request.timeToRepeat is negative");
		
		final IcmpPingJob job = new IcmpPingJob(UUID.randomUUID(), request.getHost(), request.getTtl(), request.getPacketSize(), request.getTimeout(), request.getTimeToRepeat());
		jobQueue.put(job);
		
		final ReceivedMonitoringRequestEventDTO event = new ReceivedMonitoringRequestEventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST);
		event.setPayload(job.getJobId().toString());
		event.setTimeStamp(ZonedDateTime.now());
		publisher.publish(event.getEventType(), event);
		
		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(job.getJobId());
		return ack;
	}
}
