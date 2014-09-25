package com.appirio.jp.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.appirio.jp.bulk.property.BulkProperty;
import com.appirio.jp.bulk.service.BulkService;
import com.appirio.jp.bulk.service.impl.BulkServiceFactory;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectionException;

public class ObjectHardDeleter {

	private final static Logger logger = LoggerFactory.getLogger(ObjectHardDeleter.class);
	
	private String sObjectType = "Account";
	
	//private String username = "tshiroo+mun1@appirio.com";
	//private String password = "appirio123!" + "l1XCJDopFW6XEVPr3UJohVAu";	//mun1

	private BulkProperty props;

	public ObjectHardDeleter(){
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		props = ctx.getBean(BulkProperty.class);
	}

	public static void main(String[] args) throws ConnectionException, AsyncApiException, IOException {
		
		ObjectHardDeleter deleter = new ObjectHardDeleter();
		deleter.execute();
	}
	public void execute() throws ConnectionException, AsyncApiException, IOException { 
		BulkConnection connection = getBulkConnection();
		JobInfo queryJob = createQueryJob(connection);
		BatchInfo queryBatchInfo = createQueryBatch();
		closeJob(connection, queryJob.getId());
		awaitQueryCompletion(connection, queryJob);
		//JobInfo job = createJob(connection);

		checkResults(connection, queryJob, queryBatchInfo);
		
		//Hard Delete
		JobInfo deleteJob = createDeleteJob(connection);
		List<BatchInfo> deleteBatchInfos = createBatchesFromIDList(connection, deleteJob);
		closeJob(connection, deleteJob.getId());
		awaitDeleteCompletion(connection, deleteJob, deleteBatchInfos);
		checkResults(connection, deleteJob, deleteBatchInfos);
		
		
	}
	
	private BulkConnection getBulkConnection () throws ConnectionException, AsyncApiException {
		SFDCService service = SFDCService.createSFDCService(props.getUsername(), props.getPassword(), props.getToken(), props.getAuthEndpoint());
		service.login();
		return service.getConnection();
	}
	
	private JobInfo createDeleteJob(BulkConnection connection) throws AsyncApiException { 
		JobInfo job = new JobInfo();
		job.setObject(sObjectType);
		job.setOperation(OperationEnum.hardDelete);
		job.setConcurrencyMode(ConcurrencyMode.Parallel);
		job.setContentType(ContentType.CSV);
		job = connection.createJob(job);

		return job;
	}

	private BulkService queryBulkService;
	private BulkService createQueryBulkService(BulkConnection connection){
		if(queryBulkService == null) { 
			 queryBulkService = BulkServiceFactory.createBulkService(OperationEnum.query, connection, sObjectType);
		}
		return queryBulkService;
	}
	private JobInfo createQueryJob(BulkConnection connection) throws AsyncApiException {
		createQueryBulkService(connection);
		JobInfo queryJob = queryBulkService.createJob();

		return queryJob;
	}
	
	private BatchInfo createQueryBatch() throws AsyncApiException, IOException{
		String query = "Select Id From "  +sObjectType;
		BatchInfo queryBatchInfo = queryBulkService.createBatchInfo(query);
		return queryBatchInfo;
	}
	

	private List<BatchInfo> createBatchesFromIDList(BulkConnection connection, JobInfo deleteJob) throws IOException, AsyncApiException { 
		List<BatchInfo> batchInfos = new ArrayList<>();
		File tempFile = File.createTempFile("hardDetele", ".csv");
		final byte[] headerBytes = ("Id" + "\n").getBytes("UTF-8");
		int headerByteLength = headerBytes.length;
		
		final int maxBytesPerBatch = 10000000;
		final int maxRowsPerBatch = 10000;
		int currentBytes = 0;
		int currentLines = 0;

		try{
			FileOutputStream fos = new FileOutputStream(tempFile);
			for(String id : idList) { 
				logger.info("########### delete id = " + id);
				byte[] bytes = (id + "\n").getBytes("UTF-8");
				if(currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) { 
					createDeleteBatch(fos, tempFile, batchInfos, connection, deleteJob);
					currentBytes = 0;
					currentLines = 0;
				}
				if(currentBytes == 0) { 
					fos = new FileOutputStream(tempFile);
					fos.write(headerBytes);
					currentBytes = headerByteLength;
					currentLines = 1;
				}
				fos.write(bytes);
				currentBytes += bytes.length;
				currentLines++;
			}
			if(currentLines > 1) { 
				createDeleteBatch(fos, tempFile, batchInfos, connection, deleteJob);
			}
				
		} finally { 
			tempFile.delete();
		}
		return batchInfos;
	}
	
	private void createDeleteBatch(FileOutputStream fos, File tempFile, List<BatchInfo> batchInfos, BulkConnection connection, JobInfo deleteJob) throws IOException, AsyncApiException { 
		fos.flush();
		fos.close();
		
		FileInputStream fis = new FileInputStream(tempFile);
		try{
			BatchInfo info = connection.createBatchFromStream(deleteJob, fis);
			batchInfos.add(info); 
		} finally { 
			fis.close();
		}
	}
	List<String> idList = new ArrayList<>();
	private void awaitQueryCompletion(BulkConnection connection, JobInfo job) throws AsyncApiException, IOException { 
		long sleepTime = 0L;
		String[] queryResults = null;
		while(true) { 
			try{
				Thread.sleep(sleepTime);
			} catch(InterruptedException e){
			}
			logger.info("Awaiting query result...");
			sleepTime = 10000L;

			BatchInfo[] statusList = connection.getBatchInfoList(job.getId()).getBatchInfo();
			int completeCount = 0;
			for(BatchInfo b : statusList) { 
				logger.info("########## Status is " + b.getState() + " for " + b.getId());
				if(b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
					logger.info("######## Job status is " + b.getState() + " . " + b.getStateMessage());
					completeCount++;
					QueryResultList list = connection.getQueryResultList(job.getId(), b.getId());
				    queryResults = list.getResult();

				     if (queryResults != null) {
				         for (String resultId : queryResults) {
				           InputStream is = connection.getQueryResultStream(job.getId(), b.getId(), resultId);
				           InputStreamReader isr = new InputStreamReader(is);
				           BufferedReader br = new BufferedReader(isr);
				           String line = null;
				           while((line = br.readLine()) != null) { 
				        	   logger.info("############# LINE = " + line);
				        	   if(!line.toUpperCase().contains("ID")) { 
					        	   idList.add(line);
				        	   }
				           }
				           br.close();
				         }
				       }
				}
			}
			logger.info("######## idList = " + idList);
			if(completeCount == statusList.length) { 
				break;
			}
		}
		
  	}
	
	private void awaitDeleteCompletion(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList) throws AsyncApiException { 
		long sleepTime = 0L;
		Set<String> incomplete = new HashSet<>();
		for(BatchInfo b : batchInfoList) { 
			incomplete.add(b.getId());
		}
		
		while(!incomplete.isEmpty()) { 
			try { 
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) { 
				
			}
			sleepTime = 10000L;
			BatchInfo[] statusList = connection.getBatchInfoList(job.getId()).getBatchInfo();
			for(BatchInfo b : statusList) { 
				if(b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) { 
					if(incomplete.remove(b.getId())) { 
						logger.info("####### Delete status = " + b.getState() + (b.getStateMessage() == null ? "" : b.getStateMessage()));
					}
				}
			}
			
		}
	}
	private void closeJob(BulkConnection connection, String jobId) throws AsyncApiException { 
		JobInfo job = new JobInfo();
		job.setId(jobId);
		job.setState(JobStateEnum.Closed);
		connection.updateJob(job);
	}
	
	private void checkResults(BulkConnection connection, JobInfo job, BatchInfo batchInfo) 
			throws AsyncApiException, IOException	{ 
			
			CSVReader r = new CSVReader(connection.getBatchResultStream(job.getId(), batchInfo.getId()));
/*
			BatchResult batchResult = connection.getBatchResult(job.getId(), batchInfo.getId());
			Result[] results = batchResult.getResult();
			logger.info("######### " + results.length);
			for(Result result : results) {
				String erm  = result.getId();
				for(Error e : result.getErrors()) { 
					erm = erm + e.getMessage() + " ::: ";
				}
				logger.info("######## " + erm);
			}
			
			
			
			
			
			List<String> resultHeader = r.nextRecord();
			int resultCols = resultHeader.size();
			List<String> row;
			while((row = r.nextRecord()) != null) { 
				Map<String, String> resultInfo = new HashMap<>();
				for(int i = 0; i < resultCols; i++) { 
					resultInfo.put(resultHeader.get(i), row.get(i));
				}
				logger.info("####### Result Info " + resultInfo);
				boolean success = Boolean.valueOf(resultInfo.get("Success"));
				boolean created = Boolean.valueOf(resultInfo.get("Created"));
				String id = resultInfo.get("Id");
				String error = resultInfo.get("Error");
				if (success && created) {
					logger.info("Created row with id " + id);
				} else if (!success) {
					logger.info("Failed with error: " + error);
				}
			}
*/			
			
		}

	private void checkResults(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList) 
			throws AsyncApiException, IOException	{ 
			
		for(BatchInfo b: batchInfoList) { 
			CSVReader r = new CSVReader(connection.getBatchResultStream(job.getId(), b.getId()));
			
			
			
			
			List<String> resultHeader = r.nextRecord();
			int resultCols = resultHeader.size();
			List<String> row;
			while((row = r.nextRecord()) != null) { 
				Map<String, String> resultInfo = new HashMap<>();
				for(int i = 0; i < resultCols; i++) { 
					resultInfo.put(resultHeader.get(i), row.get(i));
				}

				for(Map.Entry<String, String> entry : resultInfo.entrySet()) {
					logger.info("Key = " + entry.getKey() + " : Value = " + entry.getValue());
				}
				boolean success = Boolean.valueOf(resultInfo.get("Success"));
				boolean created = Boolean.valueOf(resultInfo.get("Created"));
				String id = resultInfo.get("Id");
				String error = resultInfo.get("Error");
				if (success && created) {
					logger.info("Created row with id " + id);
				} else if (!success) {
					logger.info("Failed with error: " + error);
				}
			}
		}
	}
}
