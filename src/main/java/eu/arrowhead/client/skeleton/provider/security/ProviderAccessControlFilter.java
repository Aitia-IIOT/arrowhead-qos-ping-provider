package eu.arrowhead.client.skeleton.provider.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import ai.aitia.arrowhead.application.library.util.ApplicationCommonConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.AccessControlFilter;

@Component
@ConditionalOnExpression(CommonConstants.$SERVER_SSL_ENABLED_WD + " and !" + ApplicationCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
public class ProviderAccessControlFilter extends AccessControlFilter {
	
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String allowedClient = CoreSystem.QOSMONITOR.name().toLowerCase() + "." + getServerCloudCN();
		if (!clientCN.equalsIgnoreCase(allowedClient)) {
			log.debug("Only QoS Monitor core system is allowed to use this provider");
			throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
	}
}
