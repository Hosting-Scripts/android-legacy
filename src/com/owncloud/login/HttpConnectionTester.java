package com.owncloud.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This Object tests for the possibility to encrypt a
 * {@link HttpConnection} in a separate Thread.
 * 
 * @author Lennart Rosam
 */
public class HttpConnectionTester extends Thread {
	public static final String MSG_KEY_URL = "host";
	public static final String MSG_KEY_CONNECTED = "testResult";
	public static final String MSG_KEY_SSL_SUCCESS = "ssl";
	public static final String MSG_KEY_URL_VALID = "urlValid";
	
	private String host;
	private String url;
	private String protocol;
	private int port;
	private boolean connectionTested = true;
	private Bundle testResultData = null;
	
	private Handler parentHandler;
	private Handler myThreadHandler = new Handler(){
		// Receives Host, Protocol and Port from main Thread
		
		@Override
		public void handleMessage(Message msg) {
			url = msg.getData().getString(MSG_KEY_URL);
			
			// Prepare test result
			testResultData = new Bundle();
			
			// Validate URL
			String urlRegex = "^(http|https)?(://)?.*(:[0-9]*)?";
			if(url.matches(urlRegex)){
				testResultData.putBoolean(MSG_KEY_URL_VALID, true);
			} else {
				testResultData.putBoolean(MSG_KEY_URL_VALID, false);
				connectionTested = false;
				return;
			}
			
			host = urlToHostname(url);
			protocol = getProtocol(url);
			port = getPort(url);
			
			connectionTested = false;
		}
		
	};
	
	/**
	 * Creates a new Instance of {@link HttpConnectionTester}
	 * @param parentHandler The handler on the UI thread to handle events
	 */
	public HttpConnectionTester(Handler parentHandler){
		this.parentHandler = parentHandler;
	}
	
	/**
	 * Use this handler to pass in a Port, a host and a URL
	 * to make this {@link HttpConnectionTester} test a connection.
	 * @return The handler for communication
	 */
	public Handler getHandler(){
		return myThreadHandler;
	}
	
	
	
	@Override
	public void run() {
		while(true){
			if(connectionTested == false){
				// Signal UI that we are starting the test
				parentHandler.sendEmptyMessage(0);
				if(testResultData.getBoolean(MSG_KEY_URL_VALID)){
					boolean connectedSucessfully = testHttpConnection(host, port, protocol);
					
					// On failure -> retry HTTP
					if(!connectedSucessfully){
						connectedSucessfully = testHttpConnection(host, 80, "http");
						if(connectedSucessfully){
							// HTTP Fallback OK
							testResultData.putBoolean(MSG_KEY_CONNECTED, true);
							testResultData.putBoolean(MSG_KEY_SSL_SUCCESS, false);
						} else {
							// HTTP fallback failed
							testResultData.putBoolean(MSG_KEY_CONNECTED, false);
							testResultData.putBoolean(MSG_KEY_SSL_SUCCESS, false);
						}
					} else {
						// First attempt OK, but double check -> Protocol could be explicitly HTTP
						testResultData.putBoolean(MSG_KEY_CONNECTED, true);
						if(protocol.equals("http")){
							testResultData.putBoolean(MSG_KEY_SSL_SUCCESS, false);
						} else {
							testResultData.putBoolean(MSG_KEY_SSL_SUCCESS, true);
						}
					}
				} else {
					// URL was invalid
					testResultData.putBoolean(MSG_KEY_URL_VALID, true);
				}
				
				// Report result back to the UI
				Message message = new Message();
				message.setData(testResultData);
				message.what = 1;
				parentHandler.sendMessage(message);
				
				connectionTested = true;
			}
			try {
				sleep(500);
			} catch (InterruptedException e) {
				Log.e("HTTPConnectionTester", "Interrupted in main loop! -> " + e);
			}
		}
	}

	/**
	 * Tests if a connection to a given host can be established
	 * @param host The host to test
	 * @param port The port to connect to
	 * @param protocol The protocol to use
	 * @return True on success, otherwise false
	 */
	private boolean testHttpConnection(String host, int port, String protocol){
		// Enterprise class names ftw! o.O
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HostConfiguration config = new HostConfiguration();
		HttpConnection connection = null;
		
		boolean connectedSucessfully = true;
		
		if(protocol == null || protocol.equals("https")){
			protocol = "https"; // Make sure its not null
			@SuppressWarnings("deprecation")
			Protocol lEasyHttps = new Protocol(protocol,
					new EasySslProtocolSocketFactory(), port);
			Protocol.registerProtocol(protocol, lEasyHttps);
		}
		
		config.setHost(host, port, protocol);
		
		try{
			connection = manager.getConnectionWithTimeout(config, 5000);
			connection.open();
		} catch (Exception e){
			connectedSucessfully = false;
		} finally{
			if(connection != null && connection.isOpen()){
				connection.close();
				connection.releaseConnection();
			}
		}
		
		return connectedSucessfully;
	}
	
	/**
	 * Returns the protocol of the URL
	 * @param url URL to check
	 * @return HTTPs - unless "http://" was exlicitly given on the beginning
	 */
	private String getProtocol(String url){
		String protocol = "https";
		if(url.startsWith("http://")){
			protocol = "http";
		}
		return protocol;
	}
	
	/**
	 * Returns the port number from the URL
	 * @param url to check
	 * @return The port number of the URL
	 */
	private int getPort(String url){
		int port = 443;
		String portRegex = ".*(:[0-9]*).*";
		
		// If protocol is HTTP, assume port 80
		if(url.startsWith("http://")){
			port = 80;
		}
		
		// If there is an explicit port number given -> Use it
		Pattern portPattern = Pattern.compile(portRegex);
		Matcher portMatcher = portPattern.matcher(url.replace("http://", "").replace("https://", ""));
		if(portMatcher.find()){
			port = Integer.parseInt(portMatcher.group(1).replace(":", ""));
		}
		
		return port;
	}
	
	/**
	 * Removes the Hostname from the URL
	 * @param url to clean
	 * @return The domain part of the URL
	 */
	private String urlToHostname(String url){
		String hostname = new String(url);
		
		// Remove protocol prefix
		hostname = hostname.replace("https://", "");
		hostname = hostname.replace("http://", "");
		
		// Remove port
		String portRegex = ".*(:[0-9]*).*";
		Pattern portPattern = Pattern.compile(portRegex);
		Matcher portMatcher = portPattern.matcher(hostname);
		if(portMatcher.find()){
			hostname = hostname.replace(portMatcher.group(1), "");
		}
		
		// Remove e.g. the "/owncloud" from "cloud.example.com/owncloud"
		if(hostname.contains("/")){
			hostname = hostname.substring(0, hostname.indexOf("/"));
		}
		
		return hostname;
	}
}
