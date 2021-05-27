package ai.aitia.qosping.service.publish;

import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

@Service
public class Publisher {

	//=================================================================================================
	// members
	
	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String clientSystemName;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String clientSystemAddress;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int clientSystemPort;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	private SystemRequestDTO sourceSystem;
	
	private final Logger logger = LogManager.getLogger(Publisher.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void publish(final EventPublishRequestDTO event) {
		Assert.notNull(event, "event is null");
		event.setSource(getSource());		
		
		try {
			arrowheadService.publishToEventHandler(event);			
		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			logger.debug(ex);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getSource() {		
		if (sourceSystem != null) {
			return sourceSystem;
		} else {			
			sourceSystem = new SystemRequestDTO();
			sourceSystem.setSystemName(clientSystemName);
			sourceSystem.setAddress(clientSystemAddress);
			sourceSystem.setPort(clientSystemPort);
			if (sslEnabled) {
				sourceSystem.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			}
			return sourceSystem;
		}		
	}
}
