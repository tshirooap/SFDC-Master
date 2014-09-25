package com.appirio.jp.bulk.service.impl;

import com.appirio.jp.bulk.service.BulkService;
import com.sforce.async.BulkConnection;
import com.sforce.async.OperationEnum;

public class BulkServiceFactory {

	private BulkServiceFactory(){
		
	}
	
	public static BulkService createBulkService(OperationEnum operation, BulkConnection connection, String sObjectType) { 
		BulkService service = null;
		if(operation != null) { 
			switch (operation) { 
			case query:
				service = new QueryBulkServiceImpl(sObjectType, connection);
				break;
			case delete:
				break;
			case insert:
				break;
			case update:
				break;
			case upsert:
				break;
			case hardDelete:
				break;
			default:
				break;
			}
		}
		return service;
	}
}
