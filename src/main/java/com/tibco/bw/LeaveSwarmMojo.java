package com.tibco.bw;


import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;

import io.fabric8.maven.docker.AbstractDockerMojo;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.ExecException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.service.ServiceHub;
import io.fabric8.maven.docker.service.DockerAccessFactory.DockerAccessContext;


@Mojo(name = "leave", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class LeaveSwarmMojo extends AbstractDockerMojo {
	
	
	
	@Parameter(property="docker.swarm.address")
	private String dockerSwarmAddress;
	
	@Parameter(property="bwdocker.host")
	private String baseUrl;
	
	@Parameter(property="docker.swarm.force.newcluster")
	private Boolean forceNewCluster;
	
	
	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	
	 
	 @Parameter(property = "bwdocker.certPath")
	    private String certPath;
	 
	 
	 @Parameter(property= "bwdocker.remoteAddr")
	 private String remoteAddr;
	 
	 @Parameter(property = "bwdocker.forceLeave")
	 private boolean forceLeave; //FIND A WAY TO OVERRIDE THIS USING A STRING PROPERTY LATER
	 

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		Properties dockerProp= Utils.loadDockerProps();
		baseUrl= dockerProp.getProperty("bwdocker.host");
		numRetries=3; //READ THIS FROM UI
		
		
		forceLeave = ("true").equalsIgnoreCase(Utils.loadSwarmPropFromFile("leaveSwarm", "forceLeave"));
		
		
	
		
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
		
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", baseUrl,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			
			if(baseUrl!=null && baseUrl.startsWith("tcp")){
				baseUrl=baseUrl.replace("tcp", "http");
			}
			
		
			String url=	String.format("%s/%s=%s", baseUrl, "swarm/leave?force=", forceLeave); //CHANGE THIS LATER
			
			
			
			
			dockerClient.communicateWithSwarm(url, null, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
	}}
	
