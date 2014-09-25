package com.appirio.jp.bulk.property.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.appirio.jp.bulk.property.BulkProperty;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public class BulkPropertyTest {
	
	@Autowired
	BulkProperty prop;

	@Test
	public void testProperty(){
		System.out.println("###### username " + prop.getUsername());
		System.out.println("###### password " + prop.getPassword());
		System.out.println("###### token " + prop.getToken());
		System.out.println("###### end " + prop.getAuthEndpoint());
	}



}
