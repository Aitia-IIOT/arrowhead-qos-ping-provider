package ai.aitia.qosping.service.model;

import java.util.List;

public class IcmpPingServiceModel {

	//=================================================================================================
	// members
	
	public static final String SERVICE_URI = "/ping-icmp";
	public static final String SERVICE_DEFINITION = "qos-icmp-ping";
	public static final List<String> SECURE_INTERFACE_LIST = List.of("HTTP-SECURE-JSON");
	public static final List<String> INSECURE_INTERFACE_LIST = List.of("HTTP-INSECURE-JSON");
	
	private static boolean registrated = false;	
	
	//=================================================================================================
	// methods

	public static boolean isRegistrated() { return registrated; }
	public static void setRegistrated(boolean registrated) { IcmpPingServiceModel.registrated = registrated; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private IcmpPingServiceModel() {
		throw new UnsupportedOperationException();
	}	
}
