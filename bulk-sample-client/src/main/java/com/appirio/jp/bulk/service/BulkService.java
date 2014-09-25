package com.appirio.jp.bulk.service;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;

public interface BulkService {

	public void setConnection(BulkConnection connection);
	public BulkConnection getConnection();

	public JobInfo createJob() throws AsyncApiException;
	
	public BatchInfo createBatchInfo() throws AsyncApiException;
	public BatchInfo createBatchInfo(String queryString) throws AsyncApiException;
	
	public String getSObjectType();
	
	public JobInfo getJobInfo();
}
