/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 */
@XStreamAlias("command")
public class CommandProperties implements Comparable<CommandProperties> {
    @XStreamAsAttribute
    String group;

    @XStreamAsAttribute
    String name;

    @XStreamAsAttribute
    String pattern;

    @XStreamAsAttribute
    String description;

    @XStreamAsAttribute
    String className;

    @XStreamAsAttribute
    Boolean experimental;

    @XStreamAsAttribute
    int priority = 1;

    public CommandProperties() {
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getPattern() {
        if (pattern == null)
            return name;
        return pattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getExperimental() {
        return experimental;
    }

    public boolean isExperimental() {
        return experimental != null && experimental;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    @Override
    public String toString() {
        return "CommandProperties{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", pattern='" + pattern + '\'' +
                ", description='" + description + '\'' +
                ", className='" + className + '\'' +
                ", experimental=" + experimental +
                ", priority=" + priority +
                '}';
    }

    @Override
    public int compareTo(CommandProperties o) {
        return name.compareTo(o.getName());
    }
}
