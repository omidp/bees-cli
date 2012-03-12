/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.sdk.commands.config.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabian Donze
 */
@XStreamAlias("env")
public class Environment {
    @XStreamAsAttribute
    private String name;

    @XStreamImplicit(itemFieldName = "resource")
    private List<ResourceSettings> resources;

    @XStreamImplicit(itemFieldName="param")
    private List<ParameterSettings> parameters;

    public Environment() {
    }

    public Environment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResourceSettings> getResources() {
        if (resources == null)
            resources = new ArrayList<ResourceSettings>();
        return resources;
    }

    public void setResources(List<ResourceSettings> resources) {
        this.resources = resources;
    }
    public void setResource(ResourceSettings resource) {
        deleteResource(resource.getName());
        getResources().add(resource);
    }

    public ResourceSettings getResource(String name) {
        for (ResourceSettings resource : getResources()) {
            if (resource.getName().equals(name))
                return resource;
        }
        return null;
    }
    public void deleteResource(String name) {
        Iterator<ResourceSettings> it = getResources().iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) {
                it.remove();
            }
        }
    }

    public List<ParameterSettings> getParameters() {
        if (parameters == null)
            parameters = new ArrayList<ParameterSettings>();
        return parameters;
    }

    public void setParameters(List<ParameterSettings> parameters) {
        this.parameters = parameters;
    }

    public void deleteParameter(String name) {
        Iterator<ParameterSettings> it = getParameters().iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) {
                it.remove();
            }
        }
    }
    public void setParameter(ParameterSettings parameter) {
        deleteParameter(parameter.getName());
        getParameters().add(parameter);
    }

    public ParameterSettings getParameter(String name) {
        for (ParameterSettings resource : getParameters()) {
            if (resource.getName().equals(name))
                return resource;
        }
        return null;
    }

}
