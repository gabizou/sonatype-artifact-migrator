package com.gabizou;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static final String BASE_URL_KEY = "BASE_URL";
    public static final String SNAPSHOT_KEY = "SNAPSHOT_REPO";
    public static final String RELEASE_KEY = "RELEASE_REPO";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String TARGET_DIR = "TARGET_DIR";

    public static final String UPLOAD_ENDPOINT = "service/rest/v1/components";

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        final String baseRepoUrl = System.getenv().get(BASE_URL_KEY);
        final String snapshotRepo = System.getenv(SNAPSHOT_KEY);
        final String releaseRepo = System.getenv(RELEASE_KEY);
        final String username = System.getenv(USERNAME);
        final String password = System.getenv(PASSWORD);
        final String targetDir = System.getenv(TARGET_DIR);
        final boolean dryRun = Arrays.stream(args).noneMatch(it -> it.equalsIgnoreCase("commit"));

        final File workingDirectory = new File(targetDir);
        final List<String> artifactIds = Files.list(workingDirectory.toPath())
            .filter(Files::isDirectory)
            .map(Path::getFileName)
            .map(Path::toFile)
            .map(File::getName)
            .collect(Collectors.toList());

        final List<ArtifactUpload> uploads = artifactIds.stream().map(artifactId -> {
            final Path artifactDirectory = new File(workingDirectory, artifactId).toPath();
            try {
                final List<MavenComponent> components = Files.list(artifactDirectory)
                    .filter(Files::isDirectory)
                    .map(versionDir -> {
                        final String version = versionDir.getFileName().toFile().getName();
                        final MavenComponent mavenArtifact;
                        if (version.endsWith("-SNAPSHOT")) {
                            final List<Artifact> poms;
                            try {
                                poms = Files.walk(versionDir).filter(
                                    file -> file.getFileName().toFile().getName().endsWith(".pom")).map(
                                    pomPath ->
                                        new Artifact(
                                            pomPath.getFileName().toFile().getName(),
                                            pomPath.toAbsolutePath().toString(), "pom"
                                        )
                                ).collect(Collectors.toList());
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new IllegalStateException("Illegal Director");
                            }
                            final List<VersionedArtifact> versionedArtifacts = poms.stream().map(pomFile -> {
                                final String baseFile = pomFile.fileName.replace(".pom", "");

                                final List<Artifact> artifacts;
                                try {
                                    artifacts = Files.walk(versionDir)
                                        .filter(file -> file.getFileName().toFile().getName().startsWith(baseFile))
                                        .filter(file -> file.getFileName().toFile().getName().endsWith(".jar"))
                                        .map(jarFile -> {
                                            final String fileQualifiedName = jarFile.getFileName().toFile().getName();
                                            final String qualifier = fileQualifiedName.replace(baseFile, "")
                                                .replace("-", "")
                                                .replace(".jar", "");
                                            final Artifact artifact = new Artifact(
                                                fileQualifiedName, jarFile.toAbsolutePath().toString(), qualifier);
                                            LOGGER.debug("Discovered {}:{} Artifact: {}", artifactId, version, artifact);
                                            return artifact;
                                        })
                                        .collect(Collectors.toList());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw new IllegalStateException("Illegal Directory");
                                }
                                return new VersionedArtifact(version, pomFile, artifacts);
                            }).collect(Collectors.toList());
                            mavenArtifact = new SnapshotComponent(artifactId, version, versionedArtifacts);
                        } else {
                            final Artifact poms;
                            LOGGER.debug("Discovered versioned directory: {}", versionDir);

                            try {
                                poms = Files.walk(versionDir)
                                    .filter(path -> {
                                        final String fileName = path.getFileName().toFile().getName();
                                        return fileName.endsWith(".pom");
                                    }).map(
                                        pomPath ->
                                        {
                                            final Artifact pom = new Artifact(
                                                pomPath.getFileName().toFile().getName(),
                                                pomPath.toAbsolutePath().toString(), "pom"
                                            );
                                            LOGGER.debug("Discovered Pom: {}", pom);
                                            return pom;
                                        }
                                    ).collect(Collectors.toList()).get(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new IllegalStateException("Unsafe operation");
                            }
                            final String baseFile = poms.fileName.replace(".pom", "");

                            final List<Artifact> artifacts;
                            try {
                                artifacts = Files.walk(versionDir)
                                    .filter(file -> file.getFileName().toFile().getName().startsWith(baseFile))
                                    .filter(file -> file.getFileName().toFile().getName().endsWith(".jar"))
                                    .map(jarFile -> {
                                        final String fileQualifiedName = jarFile.getFileName().toFile().getName();
                                        final String qualifier = fileQualifiedName.replace(baseFile, "")
                                            .replace("-", "")
                                            .replace(".jar", "");
                                        final Artifact artifact = new Artifact(
                                            fileQualifiedName, jarFile.toAbsolutePath().toString(),
                                            qualifier.replace("-", "")
                                        );
                                        LOGGER.debug("Discovered {}:{} Artifact: {}", artifactId, version, artifact);
                                        return artifact;
                                    })
                                    .collect(Collectors.toList());
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new IllegalStateException("Illegal Directory");
                            }
                            mavenArtifact = new ReleaseComponent(artifactId, version, poms, artifacts);
                        }
                        return mavenArtifact;
                    }).collect(Collectors.toList());

                return new ArtifactUpload(artifactId, components);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("Nope");
            }
        }).collect(Collectors.toList());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String s = mapper.writeValueAsString(uploads);
        LOGGER.info("Artifacts Discovered:\n {}", s);

        final OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        final String fullUrl = baseRepoUrl + UPLOAD_ENDPOINT;
        final String creds = Credentials.basic(username, password);
        final HttpUrl parse = HttpUrl.parse(fullUrl);

        uploads.parallelStream()
            .forEach(upload -> upload.versions.parallelStream()
                .forEach(mavenComponent -> {
                    final List<Request> requests = mavenComponent.buildRequests(
                        parse.newBuilder(), creds, snapshotRepo, releaseRepo);
                    try {
                        if (!dryRun) {
                            for (Request request : requests) {
                                final Response response = client.newCall(request).execute();
                                if (response.isSuccessful()) {
                                    LOGGER.info("Successfully uploaded artifact {}", mavenComponent);
                                }
                                System.out.println(mapper.writeValueAsString(response));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
            );

    }


}
