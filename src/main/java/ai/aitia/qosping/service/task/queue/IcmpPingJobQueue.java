package ai.aitia.qosping.service.task.queue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ai.aitia.qosping.service.task.IcmpPingJob;

@Component
public class IcmpPingJobQueue {

	//=================================================================================================
	// members
	
	private final BlockingQueue<IcmpPingJob> queue = new LinkedBlockingQueue<>();
	private final Set<UUID> set = Collections.synchronizedSet(new HashSet<>());
	private final Object lock = new Object();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void put(final IcmpPingJob job) {
		synchronized (lock) {
			Assert.notNull(job, "Null job is not allowed to put into IcmpPingJobQueue");
			if (!set.contains(job.getJobId())) {
				set.add(job.getJobId());
				queue.add(job);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public IcmpPingJob take() throws InterruptedException {
		final IcmpPingJob job = queue.take();
		set.remove(job.getJobId());
		
		return job;	
	}
}
