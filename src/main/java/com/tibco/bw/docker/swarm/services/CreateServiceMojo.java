package com.tibco.bw.docker.swarm.services;

import static java.net.HttpURLConnection.HTTP_OK;
import io.fabric8.maven.docker.AbstractDockerMojo;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.ExecException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.config.BuildImageConfiguration;
import io.fabric8.maven.docker.config.ImageConfiguration;
import io.fabric8.maven.docker.service.BuildService;
import io.fabric8.maven.docker.service.ImagePullManager;
import io.fabric8.maven.docker.service.ServiceHub;
import io.fabric8.maven.docker.service.DockerAccessFactory.DockerAccessContext;
import io.fabric8.maven.docker.util.EnvUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.tibco.bw.DockerAccessObjectWithHcClientSwarm;
import com.tibco.bw.DockerAccessObjectWithHcClientSwarm.HcChunkedResponseHandlerWrapper;
import com.tibco.bw.swarm.utils.Utils;

@Mojo(name = "createservice", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class CreateServiceMojo extends AbstractDockerMojo {
	
	public static final String DMP_PLUGIN_DESCRIPTOR = "META-INF/maven/io.fabric8/dmp-plugin";
    public static final String DOCKER_EXTRA_DIR = "docker-extra";
	
	@Parameter(property="docker.swarm.address")
	private String dockerSwarmAddress;
	
	@Parameter(property="bwdocker.host")
	private String baseUrl;
	
	
	

	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	
	 
	 @Parameter(property = "bwdocker.certPath")
	    private String certPath;
	 
	 
	 @Parameter(property= "bwdocker.remoteAddr")
	 private String remoteAddr;
	 
	 @Parameter(property = "bwdocker.forceLeave")
	 private boolean forceLeave;
	 
	 @Parameter(property = "swarm.servicefile")
	 private String swarmServiceFile;
	 
	 @Parameter(property="swarm.service.buildImage", defaultValue="true")
	 private String allowImageBuild; //PUT A FILE IN SERVICE SECTION OF BW MAVEN PLUGIN
	 
	 //FOLLOW THE FABRIC8 JSON APPROACH OF EITHER CREATING A MINIMAL JSON OBJECT FOR SERVICE OR PROVIDING A JSON FILE FOR CONFIG
	 
	
	 

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		Properties dockerProp= Utils.loadDockerProps();
		baseUrl= dockerProp.getProperty("bwdocker.host"); //ADD CHECKS TO SEE IF SERVICE IS ALREADY CREATED
		numRetries=3; //ADD A RETRY HANDLER IN THIS PLACE
		//REMEMBER DOCKER LOGIN BEFORE PULLING THE IMAGE TO CREATE SERVICE
		
		/*HERE PROVIDE A FUNCTIONALITY TO BUILD AN IMAGE FROM THE MAVEN PROJECT IF THE IMAGE IS NOT FOUND
		 * 
		 * FOR THAT , CALL THE DOCKER MAVEN PLUGIN DOCKER BUILD METHOD FROM HERE INTERNALLY
		 */
		
		if(swarmServiceFile==null){
			swarmServiceFile = Utils.loadSwarmPropFromFile("createService", "serviceDataLocation");
		}
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
		
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", baseUrl,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			
			if(baseUrl!=null && baseUrl.startsWith("tcp")){
				baseUrl=baseUrl.replace("tcp", "http");
			}
			
		
			allowImageBuild = Utils.loadSwarmPropFromFile("createService", "serviceImageBuild");
			
			if(System.getProperty("service.imageBuild")!=null)
				allowImageBuild = System.getProperty("service.imageBuild");
			
			if (("true").equalsIgnoreCase(allowImageBuild)) {
				InvocationRequest request = new DefaultInvocationRequest();
				request.setPomFile(new File(Utils.getPomFile()));
				request.setGoals(Arrays.asList("clean","package","initialize docker:build"));
				request.setBaseDirectory(new File(Utils.getWorkspacePath()));
				request.setBatchMode(true);
				
				
				
				Invoker invoker = new DefaultInvoker();
				String mavenHome= Utils.loadSwarmPropFromFile("createService", "mavenHome");
				if(mavenHome!=null && !mavenHome.isEmpty())
				invoker.setMavenHome(new File(mavenHome));
				try {
					invoker.execute(request);
				} catch (MavenInvocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
	       
	        }
		
			
		
			String url=	String.format("%s/%s", baseUrl, "services/create"); //CHANGE THIS LATER
			
			
			//READ THE BODY FROM FILE AS OF NOW, PROVIDE A MINIMAL CREATION WITH PROPERTIES LATER
			
			String body= Utils.getFileContents(swarmServiceFile);
			
				
			dockerClient.communicateWithSwarm(url, body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
	}}
	

