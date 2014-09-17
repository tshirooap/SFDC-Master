package com.appirio.jp.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SampleBulkClient {

	private final static Logger logger = LoggerFactory.getLogger(SampleBulkClient.class);
	public static void main(String[] args) throws AsyncApiException, ConnectionException, IOException{ 
		
		
		String sobjectType = "Account";
		String username = "tshiroo+mun1@appirio.com";
		String password = "appirio123!" + "l1XCJDopFW6XEVPr3UJohVAu";
		String sampleFileName="src/main/resources/Account.csv";

		SampleBulkClient client = new SampleBulkClient();
		client.runSample(sobjectType, username, password, sampleFileName);
	}
	
	public void runSample(String sobjectType, String username, String password, String sampleFileName) 
		throws AsyncApiException, ConnectionException, IOException{ 
		
		BulkConnection connection = getBulkConnection(username, password);
		
		JobInfo job = createJob(sobjectType, connection);
		List<BatchInfo> batchiInfoList = createBatchesFromCSVFile(connection, job, sampleFileName);
		closeJob(connection, job.getId());
		
		awaitCompletion(connection, job, batchiInfoList);
		
		checkResults(connection, job, batchiInfoList);
	}
	
	private BulkConnection getBulkConnection(String username, String password) 
		throws AsyncApiException, ConnectionException{
		
		ConnectorConfig partnerConfig = new ConnectorConfig();
		partnerConfig.setUsername(username);
		partnerConfig.setPassword(password);
		partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/31.0");
		
		// Creating the connection automatically handles login and stores
		// the session in partnerConfig
		new PartnerConnection(partnerConfig);
		// When PartnerConnection is instantiated, a login is implicitly
		// executed and, if successful,
		// a valid session is stored in the ConnectorConfig instance.
		// Use this key to initialize a BulkConnection:
		ConnectorConfig config = new ConnectorConfig();
		config.setSessionId(partnerConfig.getSessionId());
		// The endpoint for the Bulk API service is the same as for the normal
		// SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
		String soapEndpoint = partnerConfig.getServiceEndpoint();
		String apiVersion = "31.0";
		String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/"))
		+ "async/" + apiVersion;
		config.setRestEndpoint(restEndpoint);
		// This should only be false when doing debugging.
		config.setCompression(true);
		// Set this to true to see HTTP requests and responses on stdout
		config.setTraceMessage(false);
		BulkConnection connection = new BulkConnection(config);
		return connection;
	}
	
	private JobInfo createJob(String sobjectType, BulkConnection connection) 
		throws AsyncApiException{ 
		
		JobInfo job = new JobInfo();
		job.setObject(sobjectType);
		//job.setOperation(OperationEnum.insert);
		job.setOperation(OperationEnum.upsert);
		job.setExternalIdFieldName("AccountNumber__c");
		job.setContentType(ContentType.CSV);
		job = connection.createJob(job);
		
		logger.info(job.toString());
		
		return job;
	}
	
	private List<BatchInfo> createBatchesFromCSVFile(BulkConnection connection, JobInfo job, String csvFileName) 
		throws IOException, AsyncApiException{ 
		List<BatchInfo> batchInfos = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFileName)));
		
		byte[] headerBytes = (br.readLine() + "\n").getBytes("UTF-8");
		int headerByteLength = headerBytes.length;
		File tmpFile = File.createTempFile("bulkAPIInsert", ".csv");
		
		try { 
			FileOutputStream tmpOut = new FileOutputStream(tmpFile);
			int maxBytesPerBatch = 10000000;
			int maxRowsPerBatch = 10000;
			int currentBytes = 0;
			int currentLines = 0;
			String nextLine;
			while((nextLine = br.readLine()) != null) { 
				byte[] bytes = (nextLine + "\n").getBytes("UTF-8");
				if(currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) { 
					createBatch(tmpOut, tmpFile, batchInfos, connection, job);
					currentBytes = 0;
					currentLines = 0;
				}
				
				if(currentBytes == 0) { 
					tmpOut = new FileOutputStream(tmpFile);
					tmpOut.write(headerBytes);
					currentBytes = headerByteLength;
					currentLines = 1;
				}
				tmpOut.write(bytes);
				currentBytes += bytes.length;
				currentLines++;
			}
			if(currentLines > 1) { 
				createBatch(tmpOut, tmpFile, batchInfos, connection, job);
			}
			
		} finally { 
			tmpFile.delete();
		}
		return batchInfos;
	}
	
	private void createBatch(FileOutputStream tmpOut, File tmpFile, List<BatchInfo> batchInfos, BulkConnection connection, JobInfo job) 
		throws IOException, AsyncApiException{ 
		tmpOut.flush();
		tmpOut.close();
		
		FileInputStream fis = new FileInputStream(tmpFile);
		try{ 
			BatchInfo info = connection.createBatchFromStream(job, fis);
			logger.info(info.toString());
			batchInfos.add(info);
		} finally { 
			fis.close();
		}
	}
	
	private void closeJob(BulkConnection connection, String jobId) throws AsyncApiException { 
		JobInfo job = new JobInfo();
		job.setId(jobId);
		job.setState(JobStateEnum.Closed);
		connection.updateJob(job);
	}
	
	private void awaitCompletion(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList)
		throws AsyncApiException { 
		long sleepTime = 0L;
		Set<String> incomplete = new HashSet<>();
		for(BatchInfo b : batchInfoList) { 
			incomplete.add(b.getId());
		}
		
		while(!incomplete.isEmpty()) { 
			try { 
				Thread.sleep(sleepTime);
			} catch(InterruptedException e) { 
				
			}
			logger.info("Awaiting results.... " +  incomplete.size());
			sleepTime = 10000L;
			BatchInfo[] statusList = connection.getBatchInfoList(job.getId()).getBatchInfo();
			for(BatchInfo b : statusList) { 
				if(b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) { 
					if(incomplete.remove(b.getId())) { 
						logger.info("BATCH STATUS:\n " + b);
					}
				}
			}
		}
	}
	
	private void checkResults(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList) 
		throws AsyncApiException, IOException	{ 
		
		for(BatchInfo b: batchInfoList) { 
			CSVReader r = new CSVReader(connection.getBatchRequestInputStream(job.getId(), b.getId()));
			List<String> resultHeader = r.nextRecord();
			int resultCols = resultHeader.size();
			List<String> row;
			while((row = r.nextRecord()) != null) { 
				Map<String, String> resultInfo = new HashMap<>();
				for(int i = 0; i < resultCols; i++) { 
					resultInfo.put(resultHeader.get(i), row.get(i));
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
