package ai.aitia.qosping.service.task.worker;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.springframework.beans.factory.annotation.Autowired;

import ai.aitia.qosping.service.publish.Publisher;
import ai.aitia.qosping.service.task.IcmpPingJob;
import ai.aitia.qosping.service.task.manager.IcmpPingManager;
import eu.arrowhead.common.dto.shared.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.dto.shared.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class IcmpPingWorker implements Runnable {
	
	//=================================================================================================
	// members
	
	private final IcmpPingJob job;
	
	@Autowired
	private Publisher publisher;
	
	private static final int PING_REST = 1000;
	
	private final Logger logger = LogManager.getLogger(IcmpPingManager.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IcmpPingWorker(final IcmpPingJob job) {
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
		final List<IcmpPingResponseDTO> responseList = new ArrayList<>(job.getTimeToRepeat());
		
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
			request.setHost(job.getHost());
			request.setTimeout(job.getTimeout());
			request.setPacketSize(job.getPacketSize());
			request.setTtl(job.getTtl());
			
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
				dto.setThrowable(response.getThrowable() == null ? null : response.getThrowable().getClass().getSimpleName());
				dto.setHost(response.getHost());
				dto.setSize(response.getSize());
				dto.setRtt(response.getRtt());
				dto.setTtl(response.getTtl());
				dto.setDuration(response.getDuration());

				responseList.add(dto);
				Thread.sleep(PING_REST);
			}
			
		} catch (final InterruptedException | IllegalArgumentException ex) {
			final InterruptedMonitoringMeasurementEventDTO event = new InterruptedMonitoringMeasurementEventDTO();
			event.setEventType(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT);
			event.setPayload(job.getJobId().toString());
			event.setTimeStamp(ZonedDateTime.now());
			publisher.publish(event.getEventType(), event);
			return;
		}

		final FinishedMonitoringMeasurementEventDTO event = new FinishedMonitoringMeasurementEventDTO();
		event.setEventType(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT);
		event.setPayload(responseList);
		event.setTimeStamp(ZonedDateTime.now());
		publisher.publish(event.getEventType(), event);
	}
}
