package com.gabizou;

import java.util.List;
import java.util.StringJoiner;

public final class VersionedArtifact {

    public final String version;
    public final Artifact pom;
    public final List<Artifact> artifacts;

    public VersionedArtifact(String version, Artifact pom, List<Artifact> artifacts) {
        this.version = version;
        this.pom = pom;
        this.artifacts = artifacts;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VersionedArtifact.class.getSimpleName() + "[", "]")
            .add("version='" + version + "'")
            .add("pom=" + pom)
            .add("artifacts=" + artifacts)
            .toString();
    }
}
