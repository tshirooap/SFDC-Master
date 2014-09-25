package com.appirio.jp.bulk.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.appirio.jp.bulk.SFDCService;

public class SFDCServiceTest {

	@Test
	public void test() {

		SFDCService service = SFDCService.createSFDCService("tshiroo+mun1@appirio.com", "appirio123!", "l1XCJDopFW6XEVPr3UJohVAu", "https://login.salesforce.com/services/Soap/u/31.0");
		service.login();
		
	
	}

}
