package com.gabizou;

import java.util.List;
import java.util.StringJoiner;

public final class ReleaseComponent implements MavenComponent {

    public final String version;
    public final Artifact pom;
    public final List<Artifact> artifacts;

    public ReleaseComponent(String version, Artifact pom, List<Artifact> artifacts) {
        this.version = version;
        this.pom = pom;
        this.artifacts = artifacts;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReleaseComponent.class.getSimpleName() + "[", "]")
            .add("version='" + version + "'")
            .add("pom=" + pom)
            .add("artifacts=" + artifacts)
            .toString();
    }
}
