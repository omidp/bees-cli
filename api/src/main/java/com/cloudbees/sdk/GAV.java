package com.cloudbees.sdk;

/**
 * GroupId, artifactId, version tuple.
 *
 * @author Kohsuke Kawaguchi
 */
public final class GAV {
    public final String groupId, artifactId, version;

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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
}
