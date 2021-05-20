package ai.aitia.qosping.service.task;

import java.util.UUID;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class IcmpPingJob {
	
	//=================================================================================================
	// members

	private final  UUID jobId;
	private final String host;
	private final int ttl;
	private final int packetSize;
	private final long timeout;
	private final int timeToRepeat;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public IcmpPingJob(final UUID jobId, final String host, final int ttl, final int packetSize, final long timeout, final int timeToRepeat) {
		Assert.notNull(jobId, "jobId is null.");
		Assert.isTrue(!Utilities.isEmpty(host), "host is empty");
		
		this.jobId = jobId;
		this.host = host;
		this.ttl = ttl;
		this.packetSize = packetSize;
		this.timeout = timeout;
		this.timeToRepeat = timeToRepeat;
	}

	//-------------------------------------------------------------------------------------------------	
	public UUID getJobId() { return jobId; }
	public String getHost() { return host; }
	public int getTtl() { return ttl; }
	public int getPacketSize() { return packetSize; }
	public long getTimeout() { return timeout; }
	public int getTimeToRepeat() { return timeToRepeat; }
}
