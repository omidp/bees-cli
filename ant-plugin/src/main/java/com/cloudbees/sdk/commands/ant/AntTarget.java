package com.cloudbees.sdk.commands.ant;

import com.cloudbees.sdk.commands.Command;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Fabian Donze
 */
public class AntTarget extends Command {
    private String target;
    private Properties antProperties = new Properties();
    protected String baseDir;

    public AntTarget(String target) {
        this.target = target;
        setAddDefaultOptions(false);
    }

    protected void addAntProperty(String name, String value) {
        antProperties.setProperty(name, value);
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir == null ? "." : baseDir;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption("b", "baseDir", true, "Base directory (default: '.')");
        return true;
    }

    @Override
    protected boolean execute() {
        // call the application to the server
        File buildFile = new File(baseDir, "build.xml");
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());

        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);

        p.init();

        // Add bees.config properties
        Properties configProperties = getConfigProperties();
        Enumeration e = configProperties.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = configProperties.getProperty(key);

            p.setUserProperty("beesConfig." + key, value);
        }

        e = antProperties.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = antProperties.getProperty(key);

            p.setUserProperty(key, value);
        }

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.setCoreLoader(this.getClass().getClassLoader());
        p.executeTarget(target);

        return true;
    }
}
