package com.appirio.jp.bulk.service.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.jp.bulk.Util;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.OperationEnum;

public class QueryBulkServiceImpl extends AbstractBulkService{

	private final static Logger logger = LoggerFactory.getLogger(QueryBulkServiceImpl.class);


	public QueryBulkServiceImpl(String sObjectType, BulkConnection connection){
		super(sObjectType, connection);
	}

	@Override
	public JobInfo createJob() throws AsyncApiException{
		job.setObject(getSObjectType());
		job.setOperation(OperationEnum.query);
		job.setConcurrencyMode(ConcurrencyMode.Parallel);
		//Specify CSV; otherwise XML will be returned
		job.setContentType(ContentType.CSV);
		job = getConnection().createJob(job);

		logger.debug("#### Create new job " + job);
		return job;
	}

	@Override
	public BatchInfo createBatchInfo(String query) { 
		BatchInfo queryBatchInfo = null;
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(Util.convertToBytes(query));
			queryBatchInfo = connection.createBatchFromStream(getJobInfo(), bais);

		} catch(UnsupportedEncodingException e) {
			logger.error("Failed to create BatchInfo for query job " + job, e);
			queryBatchInfo = null;
		} catch (AsyncApiException e) {
			logger.error("Failed to create BatchInfo for query job " + job, e);
			queryBatchInfo = null;
		}
		return queryBatchInfo;
	}
}
