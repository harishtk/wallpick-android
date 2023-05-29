package com.pexels.api.net;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class GCloudApiInterceptor implements Interceptor {

    private final String packageName;
    private final String certSha1;

    public GCloudApiInterceptor(String packageName, String certSha1) {
        this.packageName = packageName;
        this.certSha1 = certSha1;
    }

    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("X-Android-Package", packageName)
                .header("X-Android-Cert", certSha1)
                .build());
    }
}
