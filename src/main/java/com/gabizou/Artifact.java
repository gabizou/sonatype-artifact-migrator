package com.gabizou;

import java.util.StringJoiner;

public final class Artifact {

    public final String fileName;
    public final String classifier;

    public Artifact(String fileName, String classifier) {
        this.fileName = fileName;
        this.classifier = classifier;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Artifact.class.getSimpleName() + "[", "]")
            .add("fileName='" + fileName + "'")
            .add("classifier='" + classifier + "'")
            .toString();
    }
}
