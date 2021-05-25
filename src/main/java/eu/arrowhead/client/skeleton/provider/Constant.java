package eu.arrowhead.client.skeleton.provider;

public class Constant {
	
	//=================================================================================================
	// members

	public static final String THREAD_NUM_ICMP_PING_WORKER = "thread.num.icmp-ping-worker";
	public static final String $THREAD_NUM_ICMP_PING_WORKER_WD = "${" + THREAD_NUM_ICMP_PING_WORKER + ":" + 1 + "}";
	
    //=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Constant() {
		throw new UnsupportedOperationException();
	}

}
