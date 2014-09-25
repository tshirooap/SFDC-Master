package com.appirio.jp.bulk;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SFDCService {
	
	private final static Logger logger = LoggerFactory.getLogger(SFDCService.class);
	private final static String AUTH_ENDPOINT_PRODUCTION = "https://login.salesforce.com/services/Soap/u/31.0";
	private final static String AUTH_ENDPOINT_SANDBOX = "https://test.salesforce.com/services/Soap/u/31.0";
	
	private String username;
	private String password;
	private String token;
	private String authEndpoint;
	private BulkConnection connection;
	
	
	private static SFDCService service = null;

	/**
	 * Constructor
	 */
	private SFDCService() { 
	}
	public static SFDCService createSFDCService(String username, String password, boolean isProdcution) { 
		return createSFDCService(username, password, isProdcution ? AUTH_ENDPOINT_PRODUCTION : AUTH_ENDPOINT_SANDBOX);
	}
	public static SFDCService createSFDCService(String username, String password) { 
		return createSFDCService(username, password, AUTH_ENDPOINT_PRODUCTION);
	}
	public static SFDCService createSFDCService(String username, String password, String authEndpoint) { 
		return createSFDCService(username, password, "", authEndpoint);
	}
	public static SFDCService createSFDCService(String username, String password, String token, String authEndpoint) { 
		if(service == null) { 
			service = new SFDCService();
			service.username = username;
			service.password = password;
			service.token = token;
			service.authEndpoint = authEndpoint;
		}
		return service;
	}
	
	public void login() { 
		try{
			ConnectorConfig partnerConfig = new ConnectorConfig();
			partnerConfig.setUsername(username);
			partnerConfig.setPassword(password + (Util.isNullOrEmpty(token) ? "" : token));
			partnerConfig.setAuthEndpoint(authEndpoint);
			//Session id and service end point are stored once ParnterConnection instance is created.
			PartnerConnection partnerCon = new PartnerConnection(partnerConfig);

			ConnectorConfig config = new ConnectorConfig();
			config.setSessionId(partnerConfig.getSessionId());
			String soapEndpoint = partnerConfig.getServiceEndpoint();
			String apiVersion = "31.0";
			String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
			config.setRestEndpoint(restEndpoint);
			config.setCompression(true);
			config.setTraceMessage(false);
			
			connection = new BulkConnection(config);

		} catch(ConnectionException ce) {
			 logger.error("Failed to login : reason = " + ce.getMessage());
			 logger.error("Starck Trace:", ce);	
			 connection = null;
		} catch(AsyncApiException ae ) { 
			 logger.error("Failed to login : reason = " + ae.getMessage());
			 logger.error("Starck Trace:", ae);	
			 connection = null;
		}
	}

	public BulkConnection getConnection() { 
		return this.connection;
	}
	
}
