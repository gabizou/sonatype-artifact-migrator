package com.gabizou;

import okhttp3.HttpUrl;
import okhttp3.Request;

import java.util.List;

public interface MavenComponent {
    String version();
    String prettyPrint();

    List<Request> buildRequests(
        HttpUrl.Builder builder,
        String creds,
        String snapshotRepo,
        String releaseRepo
    );
}
