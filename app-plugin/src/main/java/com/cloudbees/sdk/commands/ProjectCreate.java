package com.cloudbees.sdk.commands;

import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.repository.LocalRepository;
import net.stax.appgen.AppGenerator;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: Fabian Donze
 */
@CommandGroup("Project")
@CLICommand("app:create")
public class ProjectCreate extends Command {
    /**
     * The id of the stax application.
     */
    private String appid;

    /**
     */
    private String packageOption;

    private String applicationDirectory;

    private String template;

    public ProjectCreate() {
        setAddDefaultOptions(false);
        setArgumentExpected(1);
    }

    @Override
    protected String getUsageMessage() {
        return "APP_DIR";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption("a", "appid", true, "CloudBees application Id (default is APP_DIR)");
        addOption("p", "package", true, "Source code java package");
        addOption("t", "template", true, "(Deprecated option) Application template ID\n" +
                "  basic  - basic J2EE web (WAR-based) \n", true);
/*
                "  basic  - basic J2EE web (WAR-based) \n" +
                "  simple - basic J2EE web (EAR-based)\n" +
                "  struts - Apache Struts\n" +
                "  wicket - Apache Wicket\n" +
                "  gwt    - Google Web Toolkit\n" +
                "  coldfusion-core - ColdFusion 8 (minimal)\n" +
                "  lift   - Lift Web Framework (Scala)", true);
*/

        return true;
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setApplicationDirectory(getParameters().get(0));
            return true;
        }
        return false;
    }

    public String getApplicationDirectory() {
        return applicationDirectory;
    }

    public void setApplicationDirectory(String applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid == null ? getApplicationDirectory() : appid;
    }

    public void setPackageOption(String packageOption) {
        this.packageOption = packageOption;
    }

    public String getTemplate() {
        return template == null ? "basic" : template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    protected boolean execute() throws Exception {
        String beesTemplates = new File(getLocalRepository(), "templates").getCanonicalPath();
        if(!new File(beesTemplates).exists())
            new File(beesTemplates).mkdirs();

        File templateFile = null;
        String templateId = getTemplate();
        if (templateId.indexOf('/') == -1 && templateId.indexOf('\\') == -1) {
            System.out.println("loading remote template: " + templateId);
            LocalRepository repo = new LocalRepository();
            templateFile = repo.getApplicationTemplateZip(templateId);
        } else {
            System.out.println("loading local template: " + new File(templateId).getAbsolutePath());
            templateFile = new File(templateId);
        }

        if (templateFile == null || !templateFile.exists()) {
            System.err.println("No such template: " + templateId);
            return false;
        }

        System.out.println("Installing from template:  " + templateFile);
        Properties props = new Properties();
        props.setProperty("appid", getAppid());
        if (packageOption != null)
            props.setProperty("package", packageOption);
        createApplication(templateFile.getAbsolutePath(), new File(getApplicationDirectory()).getAbsolutePath(), isVerbose(), props);

        return true;
    }

    private void createApplication(String templateDir, String outputDir, boolean verbose, Properties props) {
        File appgenPropFile = new File(templateDir, "appgen.properties");
        Properties properties = new Properties();
        Helper.loadProperties(appgenPropFile, properties);
        for (Map.Entry<Object, Object> property: props.entrySet()) {
            properties.setProperty((String)property.getKey(), (String)property.getValue());
        }
        AppGenerator appgen = new AppGenerator(new File(templateDir, "appgen.xml"), properties);
        appgen.setVerbose(verbose);
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdirs();
        appgen.generate(outputDirFile);
    }

}
