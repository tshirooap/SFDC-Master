package com.appirio.jp.bulk.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BulkProperty {

	@Value("${sfdc.username}")
	private String username;
	
	@Value("${sfdc.password}")
	private String password;
	
	@Value("${sfdc.token}")
	private String token;
	
	@Value("${sfdc.authEndpoint}")
	private String authEndpoint;
	
	public String getUsername() { 
		return this.username;
	}
	
	public String getPassword() { 
		return this.password;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public String getAuthEndpoint(){
		return this.authEndpoint;
	}
}
