package com.gabizou;

import okhttp3.HttpUrl;
import okhttp3.Request;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public final class SnapshotComponent implements MavenComponent {

    public final String artifactId;
    public final String version;
    public final List<VersionedArtifact> snapshots;

    public SnapshotComponent(String artifactId, String version, List<VersionedArtifact> snapshots) {
        this.artifactId = artifactId;
        this.version = version;
        this.snapshots = snapshots;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String prettyPrint() {
        final StringJoiner artifactPrinter = new StringJoiner(",\n", "{", "}");

        this.snapshots.stream()
            .map(VersionedArtifact::toString)
            .forEach(artifactPrinter::add);
        return new StringJoiner(",\n", "[", "]")
            .add(version)
            .add(artifactPrinter.toString())
            .toString();
    }

    @Override
    public List<Request> buildRequests(
        HttpUrl.Builder builder, String creds, String snapshotRepo, String releaseRepo
    ) {
//        final MediaType JAR_TYPE = MediaType.parse("application/java-archive");
//        builder.addQueryParameter("repository", snapshotRepo);
//        return this.snapshots.stream().sorted()
//            .map(versionedArtifact -> {
//                final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//
//                bodyBuilder.addFormDataPart("maven2.generate-pom", "false");
//                bodyBuilder.addFormDataPart("maven2.groupId", "org.spongepowered");
//                bodyBuilder.addFormDataPart("maven2.artifactId", this.artifactId);
//                bodyBuilder.addFormDataPart("maven2.version", this.version);
//
//                final File pomFile = new File(versionedArtifact.pom.fullQualifedFile);
//
//                bodyBuilder.addFormDataPart("maven2.asset1", versionedArtifact.pom.fileName, RequestBody.create(JAR_TYPE, pomFile));
//                bodyBuilder.addFormDataPart("maven2.asset1.extension", "pom");
//                for (int i = 0; i < versionedArtifact.artifacts.size(); i++) {
//                    final Artifact artifact = versionedArtifact.artifacts.get(i);
//                    final File artifactFile = new File(artifact.fullQualifedFile);
//                    bodyBuilder.addFormDataPart("maven2.asset" + i + 1, artifact.fileName, RequestBody.create(JAR_TYPE, artifactFile));
//                    bodyBuilder.addFormDataPart("maven2.asset" + i + 1 + ".classifier", artifact.classifier);
//                    bodyBuilder.addFormDataPart("maven2.asset" + i + 1 + ".extension", "jar");
//
//                }
//                return new Request.Builder()
//                    .url(builder.build())
//                    .header("accept", "application/json")
//                    .header("User-Agent", "Sponge-Artifact-Migrator")
//                    .post(bodyBuilder.build())
//                    .build();
//            })
//            .collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnapshotComponent.class.getSimpleName() + "[", "]")
            .add("version='" + version + "'")
            .add("snapshots=" + snapshots)
            .toString();
    }
}
