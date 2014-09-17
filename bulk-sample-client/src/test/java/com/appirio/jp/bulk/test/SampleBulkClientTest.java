package com.appirio.jp.bulk.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.appirio.jp.bulk.SampleBulkClient;

public class SampleBulkClientTest {

	@Test
	public void test() {
		String sobjectType = "Account";
		String username = "tshiroo+mun1@appirio.com";
		String password = "appirio123!" + "l1XCJDopFW6XEVPr3UJohVAu";
		String sampleFileName="src/main/resources/Account.csv";

		SampleBulkClient client = new SampleBulkClient();
		try{
			client.runSample(sobjectType, username, password, sampleFileName);
		} catch(Exception e) { 
			e.printStackTrace();
		}
	}

}
