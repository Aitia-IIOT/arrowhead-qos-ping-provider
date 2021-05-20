package eu.arrowhead.client.skeleton.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ai.aitia.qosping.service.model.IcmpPingService;
import ai.aitia.qosping.service.model.IcmpPingServiceModel;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members

	@Autowired
	private IcmpPingService icmpPingService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = IcmpPingServiceModel.SERVICE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public IcmpPingRequestACK pingIcmp(@RequestBody final IcmpPingRequestDTO request) {
		//TODO validate		
		return icmpPingService.enrollRequest(request);
	}
}
