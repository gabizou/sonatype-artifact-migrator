package com.gabizou;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public final class ReleaseComponent implements MavenComponent {

    public final String artifactId;
    public final String version;
    public final Artifact pom;
    public final List<Artifact> artifacts;

    public ReleaseComponent(String artifactId, String version, Artifact pom, List<Artifact> artifacts) {
        this.artifactId = artifactId;
        this.version = version;
        this.pom = pom;
        this.artifacts = artifacts;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String prettyPrint() {
        final StringJoiner artifactPrinter = new StringJoiner(",\n", "{", "}");

        this.artifacts.stream()
            .map(Artifact::toString)
            .forEach(artifactPrinter::add);
        return new StringJoiner(",\n", "[", "]")
            .add(pom.toString())
            .add(version)
            .add(artifactPrinter.toString())
            .toString();
    }

    @Override
    public List<Request> buildRequests(
        HttpUrl.Builder builder, String creds, String snapshotRepo, String releaseRepo
    ) {
        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        bodyBuilder.addFormDataPart("maven2.generate-pom", "false");
        final File pomFile = new File(this.pom.fullQualifedFile);

        bodyBuilder.addFormDataPart("maven2.asset1", this.pom.fileName, RequestBody.create(MediaType.parse("application/octet-stream"), pomFile));
        bodyBuilder.addFormDataPart("maven2.asset1.extension", "pom");
        for (int i = 0; i < this.artifacts.size(); i++) {
            final Artifact artifact = this.artifacts.get(i);
            final File artifactFile = new File(artifact.fullQualifedFile);
            bodyBuilder.addFormDataPart("maven2.asset" + i + 1, artifact.fileName, RequestBody.create(MediaType.parse("application/octet-stream"), artifactFile));
            if (!artifact.classifier.isEmpty()) {
                bodyBuilder.addFormDataPart("maven2.asset" + i + 1 + ".classifier", artifact.classifier);
            }
            bodyBuilder.addFormDataPart("maven2.asset" + i + 1 + ".extension", "jar");

        }
        builder.addQueryParameter("repository", releaseRepo);
        return Collections.singletonList(new Request.Builder()
            .url(builder.build())
            .header("Authorization", creds)
            .header("accept", "application/json")
            .header("User-Agent", "Sponge-Artifact-Migrator")
            .post(bodyBuilder.build())
            .build());
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
