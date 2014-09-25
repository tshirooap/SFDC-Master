package com.appirio.jp.bulk.service.impl;

import com.appirio.jp.bulk.Util;
import com.appirio.jp.bulk.service.BulkService;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;

public abstract class AbstractBulkService implements BulkService {

	protected BulkConnection connection;
	protected String sObjectType;
	protected JobInfo job = new JobInfo();

	public AbstractBulkService(String sObjectType, BulkConnection connection) {
		if(Util.isNullOrEmpty(sObjectType)) { 
			throw new IllegalArgumentException("sObjectType cannot be empty");
		}
		this.sObjectType = sObjectType;
		this.connection = connection;
	}
	
	@Override
	public void setConnection(BulkConnection connection) { 
		this.connection = connection;
	}
	
	@Override
	public BulkConnection getConnection() { 
		return this.connection;
	}
	
	@Override
	public String getSObjectType() { 
		return this.sObjectType;
	}
	
	@Override
	public JobInfo getJobInfo(){
		return this.job;
	}

	@Override
	public BatchInfo createBatchInfo() {
		return null;
	}

	@Override
	public BatchInfo createBatchInfo(String query) {
		return null;
	}

	
}
