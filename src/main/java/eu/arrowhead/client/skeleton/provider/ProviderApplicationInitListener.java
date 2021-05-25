package eu.arrowhead.client.skeleton.provider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import ai.aitia.qosping.service.model.IcmpPingServiceModel;
import ai.aitia.qosping.service.task.manager.IcmpPingManager;
import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.config.ApplicationInitListener;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.client.skeleton.provider.security.ProviderSecurityConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class ProviderApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	private ProviderSecurityConfig providerSecurityConfig;

	@Autowired
	private IcmpPingManager icmpPingManager;
	
	@Value(ClientCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String mySystemName;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String mySystemAddress;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int mySystemPort;
	
	private final Logger logger = LogManager.getLogger(ProviderApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {

		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICEREGISTRY);
		if (sslEnabled && tokenSecurityFilterEnabled) {
			checkCoreSystemReachability(CoreSystem.AUTHORIZATION);			

			//Initialize Arrowhead Context
			arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);			
		
			setTokenSecurityFilter();
		
		}else {
			logger.info("TokenSecurityFilter in not active");
		}		
		
		boolean serviceReady = false;
		try {
			icmpPingManager.start();
			serviceReady = true;
		} catch (final Exception ex) {
			logger.error("IcmpPingManager could not start");
			logger.debug(ex);
			customDestroy();
		}
		
		if (serviceReady) {
			if (!registerQoSIcmpPingService()) {
				customDestroy();
			}			
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void customDestroy() {
		unregisterQoSIcmpPingService();
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void setTokenSecurityFilter() {
		final PublicKey authorizationPublicKey = arrowheadService.queryAuthorizationPublicKey();
		if (authorizationPublicKey == null) {
			throw new ArrowheadException("Authorization public key is null");
		}
		
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
			keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
			throw new ArrowheadException(ex.getMessage());
		}			
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());
		
		providerSecurityConfig.getTokenSecurityFilter().setAuthorizationPublicKey(authorizationPublicKey);
		providerSecurityConfig.getTokenSecurityFilter().setMyPrivateKey(providerPrivateKey);

	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean registerQoSIcmpPingService() {
		final SystemRequestDTO systemRequest = new SystemRequestDTO();
		systemRequest.setSystemName(mySystemName);
		systemRequest.setAddress(mySystemAddress);
		systemRequest.setPort(mySystemPort);
		
		final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
		serviceRegistryRequest.setServiceDefinition(IcmpPingServiceModel.SERVICE_DEFINITION);
		serviceRegistryRequest.setServiceUri(IcmpPingServiceModel.SERVICE_URI);
		serviceRegistryRequest.setProviderSystem(systemRequest);
		if (tokenSecurityFilterEnabled) {
			systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			serviceRegistryRequest.setSecure(ServiceSecurityType.TOKEN.name());
			serviceRegistryRequest.setInterfaces(IcmpPingServiceModel.SECURE_INTERFACE_LIST);
		} else if (sslEnabled) {
			systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			serviceRegistryRequest.setSecure(ServiceSecurityType.CERTIFICATE.name());
			serviceRegistryRequest.setInterfaces(IcmpPingServiceModel.SECURE_INTERFACE_LIST);
		} else {
			serviceRegistryRequest.setSecure(ServiceSecurityType.NOT_SECURE.name());
			serviceRegistryRequest.setInterfaces(IcmpPingServiceModel.INSECURE_INTERFACE_LIST);
		}
		
		try {
			arrowheadService.forceRegisterServiceToServiceRegistry(serviceRegistryRequest);
			IcmpPingServiceModel.setRegistrated(true);
		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			IcmpPingServiceModel.setRegistrated(false);
		}

		return IcmpPingServiceModel.isRegistrated();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void unregisterQoSIcmpPingService() {
		if (IcmpPingServiceModel.isRegistrated()) {
			arrowheadService.unregisterServiceFromServiceRegistry(IcmpPingServiceModel.SERVICE_DEFINITION, IcmpPingServiceModel.SERVICE_URI);
		}
	}
}
