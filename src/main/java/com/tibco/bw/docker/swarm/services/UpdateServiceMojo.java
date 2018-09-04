package com.tibco.bw.docker.swarm.services;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import io.fabric8.maven.docker.AbstractDockerMojo;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.ExecException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.service.ServiceHub;
import io.fabric8.maven.docker.service.DockerAccessFactory.DockerAccessContext;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.tibco.bw.DockerAccessObjectWithHcClientSwarm;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;


@Mojo(name="updateservice", defaultPhase=LifecyclePhase.INSTALL)
public class UpdateServiceMojo extends AbstractDockerMojo{
	
	
	@Parameter(property="bwdocker.host")
	private String baseUrl;
	
	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	 @Parameter(property = "bwdocker.certPath")
	 private String certPath;
	
	@Parameter(property= "update.service.id")
	private String serviceId;
	
	@Parameter(property= "update.service.name")
	private String serviceName;
	
	@Parameter(property = "service.update.file")
	private String updateFile;
	
	//FOLLOW THE SAME FILE APPROACH FOR CREATING SERVICE AS OF NOW, IMPROVE ON THAT LATER, TRY DOCKER CREATE ON ALREADY CREATED SERVICE AND SEE IF ERROR

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
		
			if(serviceId!=null) //SERVICE ID HAS TO BE COMPULSORILY PASSED AS PROP
			url=	String.format("%s/%s/%s/%s", baseUrl, "services",serviceId,"update");
			
			//ONLY ID ALLOWED, PROVIDE FUNCTIONALITY LATER TO ALLOW NAME TOO BY QUERYING TO FIND ITS ID, USING THE GETINFO MOJO CALLING IT INTERNALLY
			
			if(updateFile==null){
				updateFile = Utils.loadSwarmPropFromFile("updateService", "serviceUpdateDataLocation");
			}
			
			
			String body= Utils.getFileContents(updateFile);
				
		
			//CHANGE THIS NAME TO add post in method name
			dockerClient.communicateWithSwarm(url,body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
			
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
		
	
	}

}
