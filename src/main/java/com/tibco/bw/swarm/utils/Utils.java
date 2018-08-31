package com.tibco.bw.swarm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
	
	public static String getWorkspacePath(){
		String wsPath = System.getProperty("user.dir");
		
		if(wsPath.indexOf(".parent")!=-1){
			String path= wsPath.substring(0, wsPath.lastIndexOf(".parent"));
			return path;
		}
		return wsPath;
	}
	
	
	public static Properties loadDockerProps(){
		String file= (getWorkspacePath()+File.separator+"docker-dev.properties"); //MAKE THE DEV/PROD PROFILE CHANGES ON THIS
		File propFile= new File(file);
		Properties prop=new Properties();
		if(!propFile.exists()){ //IT IS INSIDE APP MODULE, NAVIGATE TO APPLICATION
		file= (getWorkspacePath()+".application"+File.separator+"docker-dev.properties");
		propFile=new File(file);
		
		if(!propFile.exists()){
			try {
				throw new IOException("File not found");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}}
		
		InputStream input=null;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			if(input!=null)
			prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return prop;
		
	}

}
