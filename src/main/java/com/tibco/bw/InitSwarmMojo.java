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
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;

import io.fabric8.maven.docker.AbstractDockerMojo;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.ExecException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.service.DockerAccessFactory;
import io.fabric8.maven.docker.service.DockerAccessFactory.DockerAccessContext;
import io.fabric8.maven.docker.service.ServiceHub;


@Mojo(name = "init", defaultPhase = LifecyclePhase.INSTALL)
public class InitSwarmMojo extends AbstractDockerMojo{
	
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
	 
	 
	 /*2 appraoches for getting these props:
	1. define in xml tags similar to docker maven plugin : see how its done in code (preferred) - explore how the props turn up in xml tags
	2. see the application project, read in docker-dev props, and take - only for now
	*/
	 
	 
	

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		
		Properties dockerProp= Utils.loadDockerProps();
		baseUrl= dockerProp.getProperty("bwdocker.host");
		numRetries=3;
	
		
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
		
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", baseUrl,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			
			if(baseUrl!=null && baseUrl.startsWith("tcp")){
				baseUrl=baseUrl.replace("tcp", "http");
			}
			
		
			String url=	String.format("%s/%s", baseUrl, "swarm/init");
			String listenAdr= baseUrl.replace("http:", "");
			Map<String, Object> props= new HashMap<String, Object>();
			props.put("ListenAddr", listenAdr);
		//	props.put("AdvertiseAddr", dockerSwarmAddress);
			props.put("AdvertiseAddr", "192.168.0.103");
			props.put("ForceNewCluster", false); // CHANGE LATER
			//ADD OTHER FIELDS ACCORDING TO THE Docker engine REST API
			
			
			
			String body =new ObjectMapper().writeValueAsString(props);
			
			
			
			
			dockerClient.doInitSwarm(url, body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}}
	
	
	
	
	


