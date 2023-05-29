package com.pexels.api.net;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class AcceptCharsetInterceptor implements Interceptor {

    private final String charsetString;

    public AcceptCharsetInterceptor(String chatsetString) {
        this.charsetString = chatsetString;
    }

    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("Accept-Charset", charsetString)
                .build());
    }
}
