package com.tibco.bw.docker.swarm.services;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.util.Properties;

import io.fabric8.maven.docker.AbstractDockerMojo;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.ExecException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.service.ServiceHub;
import io.fabric8.maven.docker.service.DockerAccessFactory.DockerAccessContext;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.tibco.bw.DockerAccessObjectWithHcClientSwarm;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;


@Mojo(name = "serviceinfo", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class GetServiceInfoMojo extends AbstractDockerMojo {
	
	
	@Parameter(property="bwdocker.host")
	private String baseUrl;
	
	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	 @Parameter(property = "bwdocker.certPath")
	 private String certPath;
	
	@Parameter(property= "getinfo.service.id")
	private String serviceId;
	
	@Parameter(property= "getinfo.service.name")
	private String serviceName;
	
	
	

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		Properties dockerProp= Utils.loadDockerProps();
		baseUrl= dockerProp.getProperty("bwdocker.host");
		numRetries=3; //ADD A RETRY HANDLER IN THIS PLACE
		
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
		
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", baseUrl,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			
			if(baseUrl!=null && baseUrl.startsWith("tcp")){
				baseUrl=baseUrl.replace("tcp", "http");
			}
			
			
			String url=new String();
		
			if(serviceId!=null)
			url=	String.format("%s/%s/%s", baseUrl, "services",serviceId);
			
			
			else{
				url= String.format("%s/%s/%s", baseUrl,"services",serviceName);
			}
			
			
		
			
			dockerClient.communicateWithSwarmGet(url, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 1);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
		
	}

}
