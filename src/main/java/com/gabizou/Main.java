package com.gabizou;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        final File workingDirectory = new File(System.getProperty("user.dir"));
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
                                        new Artifact(pomPath.getFileName().toFile().getName(), "pom")
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
                                                .replace(".jar", "");
                                            return new Artifact(fileQualifiedName, qualifier);
                                        })
                                        .collect(Collectors.toList());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw new IllegalStateException("Illegal Directory");
                                }
                                return new VersionedArtifact(version, pomFile, artifacts);
                            }).collect(Collectors.toList());
                            mavenArtifact = new SnapshotComponent(version, versionedArtifacts);
                        } else {
                            final Artifact poms;
                            System.out.println(versionDir);

                            try {
                                poms = Files.walk(versionDir)
                                    .filter(path ->  {
                                        final String fileName = path.getFileName().toFile().getName();
                                        return fileName.endsWith(".pom");
                                    }).map(
                                    pomPath ->
                                        new Artifact(pomPath.getFileName().toFile().getName(), "pom")
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
                                        return new Artifact(fileQualifiedName, qualifier);
                                    })
                                    .collect(Collectors.toList());
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new IllegalStateException("Illegal Directory");
                            }
                            mavenArtifact = new ReleaseComponent(version, poms, artifacts);
                        }
                        return mavenArtifact;
                    }).collect(Collectors.toList());

                return new ArtifactUpload(artifactId, components);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("Nope");
            }
        }).collect(Collectors.toList());

        uploads.forEach(upload -> {
            System.out.println("**** Artifact *****");
            System.out.println("Id: " + upload.artifactId);
            upload.versions.forEach(System.out::println);
            System.out.println("****** End *******");
        });
        uploads.forEach(System.err::println);

    }


}
