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

/**
 * GroupId, artifactId, version tuple.
 *
 * @author Kohsuke Kawaguchi
 */
// it's in the wrong package for historical reasons
public final class GAV {
    public final String groupId, artifactId, version;

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        if (groupId==null || artifactId==null ||version==null)
            throw new IllegalArgumentException();
    }
    
    public GAV(String id) {
        String[] tokens = id.split(":");
        if (tokens.length == 4) {
            this.version    = tokens[3];
        } else if (tokens.length == 3) {
            this.version    = tokens[2];
        } else if (tokens.length == 2) {
            this.version = "RELEASE";
        } else throw new IllegalArgumentException("Expected GROUPID:ARTIFACTID:VERSION but got '"+id+"'");

        this.groupId    = tokens[0];
        this.artifactId = tokens[1];
    }

    /**
     * Returns "groupId:artifactId"
     */
    public String ga() {
        return groupId+':'+artifactId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GAV gav = (GAV) o;

        return artifactId.equals(gav.artifactId) && groupId.equals(gav.groupId) && version.equals(gav.version);

    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s",groupId,artifactId,version);
    }
}
