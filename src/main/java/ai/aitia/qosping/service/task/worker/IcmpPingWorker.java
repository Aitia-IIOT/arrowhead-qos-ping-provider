package ai.aitia.qosping.service.task.worker;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.aitia.qosping.service.publish.Publisher;
import ai.aitia.qosping.service.task.IcmpPingJob;
import ai.aitia.qosping.service.task.manager.IcmpPingManager;
import eu.arrowhead.client.skeleton.provider.Constant;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class IcmpPingWorker implements Runnable {
	
	//=================================================================================================
	// members
	
	private final IcmpPingJob job;
	
	@Autowired
	private Publisher publisher;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static final int PING_REST = 1000;
	
	private final Logger logger = LogManager.getLogger(IcmpPingManager.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IcmpPingWorker(final IcmpPingJob job) {
		Assert.notNull(job, "job is null");
		this.job = job;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {			
		pingAndPublish();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void pingAndPublish() {
		publishStarted();
		final List<IcmpPingResponseDTO> responseList = new ArrayList<>(job.getTimeToRepeat());
		
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
			request.setHost(job.getHost());
			if (job.getTimeout() != null) {
				request.setTimeout(job.getTimeout());				
			}
			if (job.getPacketSize() != null) {
				request.setPacketSize(job.getPacketSize());				
			}
			if (job.getTtl() != null) {
				request.setTtl(job.getTtl());				
			}
			
			for (int count = 0; count < job.getTimeToRepeat(); count ++) {
				IcmpPingResponse response;
				try {
					response = IcmpPingUtil.executePingRequest(request);
					final String formattedResponse = IcmpPingUtil.formatResponse(response);
					logger.debug(formattedResponse);
					
				} catch (final Exception ex) {
					response = new IcmpPingResponse();
					response.setErrorMessage(ex.getMessage());
					response.setSuccessFlag(false);
					response.setThrowable(ex);
				}
				
				final IcmpPingResponseDTO dto = new IcmpPingResponseDTO();
				dto.setSuccessFlag(response.getSuccessFlag());
				dto.setTimeoutFlag(response.getTimeoutFlag());
				dto.setErrorMessage(response.getErrorMessage());
				dto.setThrowable(response.getThrowable() == null ? null : response.getThrowable().getClass().getName());
				dto.setHost(response.getHost());
				dto.setSize(response.getSize());
				dto.setRtt(response.getRtt());
				dto.setTtl(response.getTtl());
				dto.setDuration(response.getDuration());

				responseList.add(dto);
				Thread.sleep(PING_REST);
			}
			
		} catch (final InterruptedException | IllegalArgumentException ex) {
			publishInterrupted(ex);
			return;
		}
		
		String payloadStr = "";
		try {
			payloadStr = mapper.writeValueAsString(responseList);
		} catch (final JsonProcessingException ex) {
			publishInterrupted(ex);
		}

		publishFinished(payloadStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void publishStarted() {
		final EventPublishRequestDTO event = new EventPublishRequestDTO();
		event.setEventType(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name());
		event.setMetaData(Map.of(Constant.PROCESS_ID, job.getJobId().toString()));
		event.setPayload(Constant.EMPTY_ARRAY_STR);
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		publisher.publish(event);
	}

	//-------------------------------------------------------------------------------------------------
	private void publishFinished(final String payloadStr) {
		final EventPublishRequestDTO event = new EventPublishRequestDTO();
		event.setEventType(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name());
		event.setMetaData(Map.of(Constant.PROCESS_ID, job.getJobId().toString()));
		event.setPayload(payloadStr);
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		publisher.publish(event);
	}

	//-------------------------------------------------------------------------------------------------
	private void publishInterrupted(final Exception ex) {
		final EventPublishRequestDTO event = new EventPublishRequestDTO();
		event.setEventType(QosMonitorEventType.INTERRUPTED_MONITORING_MEASUREMENT.name());
		event.setMetaData(Map.of(Constant.PROCESS_ID, job.getJobId().toString(), Constant.EXCEPTION, ex.getMessage()));
		event.setPayload(Constant.EMPTY_ARRAY_STR);
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		publisher.publish(event);
	}
}
