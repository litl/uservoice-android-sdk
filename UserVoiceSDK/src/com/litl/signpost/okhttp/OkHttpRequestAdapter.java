package com.litl.signpost.okhttp;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import oauth.signpost.http.HttpRequest;
import okio.Buffer;

public class OkHttpRequestAdapter implements HttpRequest {
    private Request mRequest;

    public OkHttpRequestAdapter(Request request) {
        mRequest = request;
    }

    @Override
    public String getMethod() {
        return mRequest.method();
    }

    @Override
    public String getRequestUrl() {
        return mRequest.urlString();
    }

    @Override
    public void setRequestUrl(String url) {
        mRequest = mRequest.newBuilder()
                .url(url)
                .build();
    }

    @Override
    public void setHeader(String name, String value) {
        mRequest = mRequest.newBuilder()
                .addHeader(name, value)
                .build();
    }

    @Override
    public String getHeader(String name) {
        return mRequest.header(name);
    }

    @Override
    public Map<String, String> getAllHeaders() {
        final Headers headers = mRequest.headers();
        final int headersCount = headers.size();
        if (headersCount == 0) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new HashMap<>(headersCount);
        for (int i = 0; i < headersCount; i++) {
            map.put(headers.name(i), headers.value(i));
        }

        return map;
    }

    @Override
    public InputStream getMessagePayload() throws IOException {
        final RequestBody body = mRequest.body();
        if (body != null) {
            final Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.inputStream();
        }

        return null;
    }

    @Override
    public String getContentType() {
        final RequestBody body = mRequest.body();
        final MediaType contentType = body != null ? body.contentType() : null;
        return contentType != null ? contentType.toString() : null;
    }

    @Override
    public Object unwrap() {
        return mRequest;
    }
}
