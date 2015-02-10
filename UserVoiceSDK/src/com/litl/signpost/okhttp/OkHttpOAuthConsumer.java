package com.litl.signpost.okhttp;

import com.squareup.okhttp.Request;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

public class OkHttpOAuthConsumer extends AbstractOAuthConsumer {
    public OkHttpOAuthConsumer(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret);
    }

    @Override
    protected HttpRequest wrap(Object request) {
        if (request instanceof Request) {
            return new OkHttpRequestAdapter((Request) request);
        } else {
            throw new IllegalArgumentException("Not an okhttp.Request instance");
        }
    }
}
