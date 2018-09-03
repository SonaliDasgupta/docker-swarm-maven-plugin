package com.tibco.bw.swarm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {

	public static String getWorkspacePath(){
		String wsPath = System.getProperty("user.dir");

		if(wsPath.indexOf(".parent")!=-1){
			String path= wsPath.substring(0, wsPath.lastIndexOf(".parent"));
			return path;
		}
		return wsPath; 

	}

	public static String getFile(String fileName){

		String file= (getWorkspacePath()+File.separator+fileName);
		File propFile= new File(file);

		if(!propFile.exists()){ //IT IS INSIDE APP MODULE, NAVIGATE TO APPLICATION , //CHECK IF THIS LOGIN IS CORRECT , IT MIGHT NOT BE ? SINCE APP MODULE MIGHT END WITH .module
			file= (getWorkspacePath()+".application"+File.separator+"docker-dev.properties");
			propFile=new File(file);

			if(!propFile.exists()){
				try {
					throw new IOException("File not found");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


		return file;

	}


	public static Properties loadDockerProps(){
		InputStream input=null;
		try { //CHECK THE PROFILE LATER
			input = new FileInputStream(getFile("docker-dev.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		Properties prop = new Properties();
		try {
			if(input!=null)
				prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prop;

	}
	
	public static String loadSwarmProp(String propFile, String propName){
		InputStream input=null;
		try { //OPTIMIZE FOR PROFILES LATER
			input = new FileInputStream(getFile(propFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		Properties prop = new Properties();
		String propVal="";
		try {
			if(input!=null)
				prop.load(input);
			if(prop!=null) //NEED TO RETURN JUST THE TAG VALUE INSTEAD
			{
				propName=propName.replaceAll("[{$}]+","");
				propVal=prop.getProperty(propName);
				
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return propVal;

	}
	
	public static String findSwarmPropFile(Document doc){
		NodeList propNodeList = doc.getElementsByTagName("properties");
		if(propNodeList!=null && propNodeList.getLength()>0){
			Node propNode= propNodeList.item(0);
			if(propNode.getNodeType()== Node.ELEMENT_NODE){
				Element propEl= (Element) propNode;
				return getTagValue("swarm.property.file", propEl);
				
			}
			
		}
		
		return "";
		
	}


	public static String loadSwarmPropFromFile(String parent, String child){
		
		
		File pomFile= new File(getFile("pom.xml"));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc= dBuilder.parse(pomFile);
			doc.getDocumentElement().normalize();
			String propFile = findSwarmPropFile(doc);
			NodeList initNodeList = doc.getElementsByTagName(parent);
			if(initNodeList!=null && initNodeList.getLength()>0){
				Node initNode= initNodeList.item(0);
				if(initNode!=null && initNode.getNodeType() == Node.ELEMENT_NODE){
					Element initElement = (Element) initNode;
					String propVal = getTagValue(child, initElement);
				if(propVal!=null && !propVal.isEmpty())
					return loadSwarmProp(propFile, propVal); //IS THIS THE RIGHT APPROACH, LOOK AT DOCKER MAVEN PLUGIN?
				
					
					
				}
			
			
			
		}

		return null;
	}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getTagValue(String tag, Element element){
		NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodeList.item(0);
		return node.getNodeValue();
	}
		

}
