package com.cloudbees.sdk.commands.config.model;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @Author: Fabian Donze
 */
public class ParameterSettings {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String value;

    public ParameterSettings(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ParameterSettings{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterSettings that = (ParameterSettings) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
