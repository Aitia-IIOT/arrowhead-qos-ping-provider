package eu.arrowhead.client.skeleton.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ai.aitia.qosping.service.IcmpPingService;
import ai.aitia.qosping.service.model.IcmpPingServiceModel;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;

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
		validateIcmpPingRequestDTO(request);		
		return icmpPingService.enrollRequest(request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateIcmpPingRequestDTO(final IcmpPingRequestDTO dto) {
		if (dto == null) {
			throw new BadPayloadException("IcmpPingRequestDTO is null", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		
		if (Utilities.isEmpty(dto.getHost())) {
			throw new BadPayloadException("IcmpPingRequestDTO.host is empty", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		
		if (dto.getTtl() != null && dto.getTtl() <= 0) {
			throw new BadPayloadException("IcmpPingRequestDTO.ttl is negative or zero", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		
		if (dto.getPacketSize() != null && dto.getPacketSize() <= 0) {
			throw new BadPayloadException("IcmpPingRequestDTO.packetSize is negative or zero", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		
		if (dto.getTimeout() != null && dto.getTimeout() <= 0) {
			throw new BadPayloadException("IcmpPingRequestDTO.timeout is negative or zero", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		
		if (dto.getTimeToRepeat() == null) {
			throw new BadPayloadException("IcmpPingRequestDTO.timeToRepeat is null", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
		if (dto.getTimeToRepeat() <= 0) {
			throw new BadPayloadException("IcmpPingRequestDTO.timeToRepeat is negative or zero", HttpStatus.BAD_REQUEST.value(), IcmpPingServiceModel.SERVICE_URI);
		}
	}
}
