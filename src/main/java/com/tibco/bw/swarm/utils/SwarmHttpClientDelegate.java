package com.tibco.bw.swarm.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

/*import io.fabric8.maven.docker.access.hc.ApacheHttpClientDelegate;
import io.fabric8.maven.docker.access.hc.ApacheHttpClientDelegate.StatusCodeCheckerResponseHandler;
import io.fabric8.maven.docker.access.hc.util.ClientBuilder;

public class SwarmHttpClientDelegate extends ApacheHttpClientDelegate{

	public SwarmHttpClientDelegate(ClientBuilder clientBuilder, boolean pooled)
			throws IOException {
		super(clientBuilder, pooled);
		// TODO Auto-generated constructor stub
	}
	
	public SwarmHttpClientDelegate(ClientBuilder clientBuilder)
			throws IOException {
		super(clientBuilder, true);
		// TODO Auto-generated constructor stub
	}
	
	 public <T> T post(String url, Object body, Map<String, String> headers,
             ResponseHandler<T> responseHandler, int... statusCodes) throws IOException {
		 	HttpUriRequest request = newPost(url, body);
		 	for (Entry<String, String> entry : headers.entrySet()) {
		 			request.addHeader(entry.getKey(), entry.getValue());
		 	}

		 	return httpClient.execute(request, new StatusCodeCheckerResponseHandler<>(responseHandler, statusCodes));
	 }
	

}*/
