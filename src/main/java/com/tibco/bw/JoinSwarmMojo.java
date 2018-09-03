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


@Mojo(name = "join", defaultPhase = LifecyclePhase.INSTALL)
public class JoinSwarmMojo extends AbstractDockerMojo {
	
	
	
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
	 
	 @Parameter(property = "bwdocker.joinToken")
	 private String token;
	 
	 
		@Parameter(property="swarm.listenAddr")
		private String listenAddr;
		
		@Parameter(property= "swarm.advertiseAddr")
		private String advertiseAddr;
	 

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		Properties dockerProp= Utils.loadDockerProps();
		baseUrl= dockerProp.getProperty("bwdocker.host");
		numRetries=3;
		if(remoteAddr==null)
		remoteAddr = Utils.loadSwarmPropFromFile("joinSwarm", "remoteManagerAddress");	
		
		if(token==null)
		token = Utils.loadSwarmPropFromFile("joinSwarm", "joinToken");	
		
		if(listenAddr==null)
			listenAddr = Utils.loadSwarmPropFromFile("initSwarm", "listenAddress");
		
		if(advertiseAddr==null){
			advertiseAddr = Utils.loadSwarmPropFromFile("initSwarm", "advertiseAddress");
		}
	
		
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
		
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", baseUrl,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			
			if(baseUrl!=null && baseUrl.startsWith("tcp")){
				baseUrl=baseUrl.replace("tcp", "http");
			}
			
		
			String url=	String.format("%s/%s", baseUrl, "swarm/join");
			listenAddr= listenAddr.replace("tcp:", "");
			advertiseAddr= advertiseAddr.replace("tcp:", "");
			Map<String, Object> props= new HashMap<String, Object>();
			props.put("ListenAddr", listenAddr); //ADD PROP OPTION FOR LISTEN ADDR, ONLY THR HOST NEEDED
		
			props.put("AdvertiseAddr", advertiseAddr);
			String[] addr= new String[10];
			addr[0]= remoteAddr;
			props.put("RemoteAddrs", addr); //?
			
			
			props.put("JoinToken", token); // CHANGE LATER
			//ADD OTHER FIELDS ACCORDING TO THE Docker engine REST API
			
			
			
			String body =new ObjectMapper().writeValueAsString(props);
			
			
			
			
			dockerClient.communicateWithSwarm(url, body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
	}}
	