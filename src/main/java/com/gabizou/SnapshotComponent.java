package com.gabizou;

import java.util.List;
import java.util.StringJoiner;

public final class SnapshotComponent implements MavenComponent {

    public final String version;
    public final List<VersionedArtifact> snapshots;

    public SnapshotComponent(String version, List<VersionedArtifact> snapshots) {
        this.version = version;
        this.snapshots = snapshots;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnapshotComponent.class.getSimpleName() + "[", "]")
            .add("version='" + version + "'")
            .add("snapshots=" + snapshots)
            .toString();
    }
}
