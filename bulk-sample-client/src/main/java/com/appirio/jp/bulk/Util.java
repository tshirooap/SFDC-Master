package com.appirio.jp.bulk;

import java.io.UnsupportedEncodingException;

public class Util {

	private Util(){}
	
	public static byte[] convertToBytes(String covertee) throws UnsupportedEncodingException { 
		return convertToBytes(covertee, "UTF-8");
	}
	
	public static byte[] convertToBytes(String convertee, String encoding) throws UnsupportedEncodingException { 
		if(isNullOrEmpty(convertee) || isNullOrEmpty(encoding)) { 
			return null;
		}
		
		byte[] bytes = convertee.getBytes(encoding);
		return bytes;
	}
	
	public static boolean isNullOrEmpty(String str) { 
		return str == null || str.isEmpty();
	}
	
}
