package com.staxnet.ant;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.ServletException;

import com.cloudbees.sdk.utils.Helper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.staxnet.appserver.StaxSdkAppServer;

public class RunWarTask extends Task {
    private File baseDir;
    private File serverConfigFile;
    private File workingDir;
    private File webappDir;
    private int port = 8080;
    private String environment;
    
    @Override
    public void execute() throws BuildException {
        try {
            if(baseDir == null)
                baseDir = getProject().getBaseDir();

            checkRequiredAttribute("workingDir", workingDir);
            checkRequiredAttribute("webappDir", webappDir);
            run(workingDir, port, getEnvironments());
        }
        catch (IllegalArgumentException e) {
            throw new BuildException(e.getMessage());
        }
        catch (ServletException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
    
    private void run(File workingDir, int port, String[] environments) throws ServletException
    {
        ArrayList<String> args = new ArrayList<String>();
        args.add("-dir");
        args.add(workingDir.getAbsolutePath());
        args.add("-web");
        args.add(webappDir.getAbsolutePath());
        args.add("-port");
        args.add(((Integer)port).toString());
        if(serverConfigFile != null && serverConfigFile.exists()){
            args.add("-config");
            args.add(serverConfigFile.getAbsolutePath());
        }
        if(environments != null && environments.length > 0)
        {
            args.add("-env");
            args.add(StringHelper.join(environments, ","));
            log("application environment: " + StringHelper.join(environments, ","));
        }
        
        StaxSdkAppServer.main(args.toArray(new String[0]));
    }
    
    private void checkRequiredAttribute(String attrName, Object value)
    {
        if(value == null)
            throw new BuildException("missing required attribute: " + attrName);
    }
    
    private String emptyToNull(String v)
    {
        if(v == null || v.trim().equals(""))
            return null;
        else
            return v;
    }
    
    private String getAttributeValue(String attrName, String value, boolean required)
    {
        if(value == null)
        {
            value = System.getProperty("bees." + attrName);
            if (value == null) value = System.getProperty("stax." + attrName);
            if(required && value == null)
                throw new BuildException("missing required attribute: " + attrName);
        }
        return value;
    }
    
    public void setWorkingdir(File workingDir) {
        this.workingDir = workingDir;
    }

    public void setWebappdir(File webappDir) {
        this.webappDir = webappDir;
    }
    
    public void setServerconfig(File servrConfigFile) {
        this.serverConfigFile = servrConfigFile;
    }
    
    public void setBaseDir(File webappDir) {
        this.webappDir = webappDir;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setEnvironment(String environments)
    {
        this.environment = environments;
    }
    
    private String[] getEnvironments()
    {
        String[] environment = Helper.getEnvironmentList(this.environment, "run");
        return environment;
    }
}
