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
	
	@Parameter(property="docker.host")
	private String dockerHost;
	
	@Parameter(property="docker.swarm.address")
	private String dockerSwarmAddress;
	
	@Parameter(property="swarm.listenAddr")
	private String listenAddr;
	
	@Parameter(property= "swarm.advertiseAddr")
	private String advertiseAddr;
	
	@Parameter(property="swarm.force.newcluster")
	private Boolean forceNewCluster;
	
	
	@Parameter(property="docker.swarm.retries")
	private int numRetries;
	
	
	 
	 @Parameter(property = "bwdocker.certPath")
	    private String certPath; //THIS IS GETTING DUPLICATED
	 	 
	

	@Override
	protected void executeInternal(ServiceHub serviceHub)
			throws DockerAccessException, ExecException, MojoExecutionException {
		
		Properties dockerProps = Utils.loadDockerProps();
		
		if(dockerHost==null){
			dockerHost = dockerProps.getProperty("bwdocker.host");
		}
		
		
		if(listenAddr==null)
		listenAddr = Utils.loadSwarmPropFromFile("initSwarm", "listenAddress");
		
	
		
		if(advertiseAddr==null){
			advertiseAddr = Utils.loadSwarmPropFromFile("initSwarm", "advertiseAddress");
		}
		
	
		
		if(forceNewCluster==null){
			forceNewCluster= ("true").equalsIgnoreCase(Utils.loadSwarmPropFromFile("initSwarm", "forceNewCluster"))?true:false;
		}
		
		//WILL DOCKER CERT PATH BE SAME AS SWARM CERT PATH??
		if(certPath==null){
			certPath=dockerProps.getProperty("bwdocker.certPath");
		}
		
		numRetries=3; //ADD NUM RETRIES AS UI PROP IN BW6 PLUGIN MAVEN
	
		
		
		DockerAccessContext dockerAccessContext= getDockerAccessContext();
		
		try {
			
			dockerHost= dockerHost.replace("127.0.0.1", "0.0.0.0");
			
			DockerAccessObjectWithHcClientSwarm dockerClient=new DockerAccessObjectWithHcClientSwarm("v1.38", dockerHost,  certPath,
                    dockerAccessContext.getMaxConnections(),
                    dockerAccessContext.getLog() );
			
			if(dockerHost!=null && dockerHost.startsWith("tcp://")){
				dockerHost= dockerHost.replace("tcp://","http://");
			}
			
			if(listenAddr!=null && listenAddr.startsWith("tcp")){
				listenAddr=listenAddr.replace("tcp://", "");
			}
			
			//CONSIDER THE UNIX CASES TOO , IS TCP A RIGHT PREEFIX
			if(advertiseAddr!=null && advertiseAddr.startsWith("tcp")){
				advertiseAddr=advertiseAddr.replace("tcp://", "");
			}
			
		
			String url=	String.format("%s/%s", dockerHost, "swarm/init");
		//	String listenAdr= listenAddr.replace("http:", "");
			Map<String, Object> props= new HashMap<String, Object>();
			props.put("ListenAddr", listenAddr); 
		
			props.put("AdvertiseAddr", advertiseAddr);
			props.put("ForceNewCluster", forceNewCluster); 
			//ADD OTHER FIELDS ACCORDING TO THE Docker engine REST API
			
			
			
			String body =new ObjectMapper().writeValueAsString(props);
			
			
			
			
			dockerClient.communicateWithSwarm(url, body, new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(dockerAccessContext.getLog())), HTTP_OK, 3);
		//WRITE A NEW RESPONSE HANDLER!!	
			
			}
			
		 catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace(); //THIS EXCEPTION COMES WHEN THE NODE TRIES TO JOIN ALREADY JOINED, JUST LOG THE MESSAGE
		}	
	}}
	
	
	
	
	


