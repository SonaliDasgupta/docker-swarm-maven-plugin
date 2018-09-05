package com.tibco.bw.docker.swarm.services;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
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
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;

//PROVIDE OPTION WITH DOCKER IMAGE TOO , FOR CREATE SERVICE SEE IF CHECK FOR IF BUILT To REBUILD AGAIN
@Mojo(name="updateservice", defaultPhase=LifecyclePhase.INSTALL)
public class UpdateServiceMojo extends AbstractDockerMojo{
	
	
	@Parameter(property="bwdocker.host")
	private String baseUrl;
	
	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	 @Parameter(property = "bwdocker.certPath")
	 private String certPath;
	 
	 
	
	@Parameter(property= "update.service.id") //SERVICE ID : USE GET TO TAKE IT 
	private String serviceId;
	
	
	@Parameter(property = "swarm.serviceupdate.useDockerimage")
	private boolean useDockerImage;
	
	@Parameter(property="swarm.serviceupdate.dockerimage")
	private String dockerImage; //SHOULD WE REBUILD THE DOCKER IMAGE BEFORE UPDATE , OR DO IT FIRST USING THE DOCKER:BUILD MAVEN GOAL ?
	
	
	
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
			
		
			String body= "";
			String url=new String();
		
			if(serviceId!=null) //SERVICE ID HAS TO BE COMPULSORILY PASSED AS PROP
			url=	String.format("%s/%s/%s/%s", baseUrl, "services",serviceId,"update");
			
			//ONLY ID ALLOWED, PROVIDE FUNCTIONALITY LATER TO ALLOW NAME TOO BY QUERYING TO FIND ITS ID, USING THE GETINFO MOJO CALLING IT INTERNALLY
			
			useDockerImage =("true").equalsIgnoreCase(Utils.loadSwarmPropFromFile("updateService", "usedockerimage"));
			if(useDockerImage){
			
			
			dockerImage= Utils.loadDockerProps().getProperty("docker.image");
			if(dockerImage!=null){
				
			
				Map<String, String> imageInfo = new HashMap<String, String>();
				imageInfo.put("Image", dockerImage);
				Map<String, Object> containerInfo = new HashMap<String, Object>();
				containerInfo.put("ContainerSpec",imageInfo);
				Map<String, Object> taskInfo = new HashMap<String, Object>();
				taskInfo.put("TaskTemplate",containerInfo);
				
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(new File("temp.json"),taskInfo);
				body = IOUtils.toString(new FileInputStream("temp.json"),"UTF-8");
				
				
				
				
			}
				
			
			}
			
			
			else if(updateFile==null){
				updateFile = Utils.loadSwarmPropFromFile("updateService", "serviceUpdateDataLocation");
			
			
			
			 body= Utils.getFileContents(updateFile);
			}	
		
			//CHANGE THIS NAME TO add post in method name
			dockerClient.communicateWithSwarm(url,body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
			
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
			
			//EXCEPTION WHEN WE USE IMAGE , LOOK INTO IT , ALSO DELETE temp.json later
		}	
		
	
	}

}
