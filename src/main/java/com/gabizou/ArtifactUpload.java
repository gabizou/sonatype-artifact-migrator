package com.gabizou;

import java.util.List;
import java.util.StringJoiner;

public final class ArtifactUpload {

    public final String artifactId;
    public final List<MavenComponent> versions;

    public ArtifactUpload(String artifactId, List<MavenComponent> versions) {
        this.artifactId = artifactId;
        this.versions = versions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArtifactUpload.class.getSimpleName() + "[", "]")
            .add("artifactId='" + artifactId + "'")
            .add("versions=" + versions)
            .toString();
    }
}
