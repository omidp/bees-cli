package com.cloudbees.sdk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@XStreamAlias("plugin")
public class Plugin {
    @XStreamAsAttribute
    String artifact;

    @XStreamImplicit(itemFieldName="command")
    List<CommandProperties> properties;

    @XStreamImplicit(itemFieldName="jar")
    List<String> jars;

    public Plugin() {
        properties = new ArrayList<CommandProperties>();
        jars = new ArrayList<String>();
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public List<CommandProperties> getProperties() {
        return properties;
    }

    public void setProperties(List<CommandProperties> properties) {
        this.properties = properties;
    }

    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }
}
