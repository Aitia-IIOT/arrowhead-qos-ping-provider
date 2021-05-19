package eu.arrowhead.client.skeleton.provider.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ai.aitia.qosping.service.model.IcmpPingServiceModel;
import eu.arrowhead.common.CommonConstants;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members

	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = IcmpPingServiceModel.SERVICE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object pingMeasurement(@RequestBody final Object request) { // TODO: response: IcmpPingRequestACK, request: IcmpPingRequest
		return null;
	}
}
