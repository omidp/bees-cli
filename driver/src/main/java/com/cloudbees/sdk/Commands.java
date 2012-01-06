package com.cloudbees.sdk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@XStreamAlias("commands")
public class Commands {

    @XStreamImplicit(itemFieldName="command")
    List<CommandProperties> properties;

    public Commands() {
        properties = new ArrayList<CommandProperties>();
    }

    public List<CommandProperties> getProperties() {
        return properties;
    }

    public void setProperties(List<CommandProperties> properties) {
        this.properties = properties;
    }
}
