package com.uservoice.uservoicesdk.rest;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.model.AccessToken;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import oauth.signpost.OAuthConsumer;

public class RestTask extends AsyncTask<String, String, RestResult> {
    private String urlPath;
    private RestMethod method;
    private Map<String, String> params;
    private RestTaskCallback callback;
    private Request request;

    private static OkHttpClient sHttpClient;

    public static void setHttpClient(OkHttpClient client) {
        sHttpClient = client;
    }

    public RestTask(RestMethod method, String urlPath, List<BasicNameValuePair> params, RestTaskCallback callback) {
        this(method, urlPath, params == null ? null : paramsToMap(params), callback);
    }

    public RestTask(RestMethod method, String urlPath, Map<String, String> params, RestTaskCallback callback) {
        this.method = method;
        this.urlPath = urlPath;
        this.callback = callback;
        this.params = params;
    }

    @Override
    protected RestResult doInBackground(String... args) {
        Response response = null;
        try {
            request = createRequest();
            if (isCancelled())
                throw new InterruptedException();
            OAuthConsumer consumer = Session.getInstance().getOAuthConsumer();
            if (consumer != null) {
                AccessToken accessToken = Session.getInstance().getAccessToken();
                if (accessToken != null) {
                    consumer.setTokenWithSecret(accessToken.getKey(), accessToken.getSecret());
                }
                request = (Request) consumer.sign(request).unwrap();
            }
            Log.d("UV", urlPath);
            if (isCancelled())
                throw new InterruptedException();

            // TODO it would be nice to find a way to abort the request on cancellation
            response = sHttpClient.newCall(request).execute();
            if (isCancelled())
                throw new InterruptedException();

            final String body = response.body().string();
            if (isCancelled())
                throw new InterruptedException();
            return new RestResult(response.code(), new JSONObject(body));
        } catch (Exception e) {
            return new RestResult(e);
        } finally {
            if (response != null)
                try {
                    response.body().close();
                } catch (IOException e) {
                }
        }
    }

    private Request createRequest() throws URISyntaxException, UnsupportedEncodingException {
        String host = Session.getInstance().getConfig().getSite();
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(host.contains(".us.com") ? "http" : "https");
        uriBuilder.encodedAuthority(host);
        uriBuilder.path(urlPath);

        final Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent" , String.format("uservoice-android-%s", UserVoice.getVersion()))
                .addHeader("Accept-Language", Locale.getDefault().getLanguage())
                .addHeader("API-Client", String.format("uservoice-android-%s", UserVoice.getVersion()));

        if (method == RestMethod.GET)
            builder.url(requestWithQueryString(uriBuilder));
        else if (method == RestMethod.DELETE)
            builder.delete()
                    .url(requestWithQueryString(uriBuilder));
        else if (method == RestMethod.POST) {
            builder.post(requestWithEntity())
                    .url(uriBuilder.build().toString());
        } else if (method == RestMethod.PUT) {
            builder.put(requestWithEntity())
                    .url(uriBuilder.build().toString());
        } else
            throw new IllegalArgumentException("Method must be one of [GET, POST, PUT, DELETE], but was " + method);

        return builder.build();
    }

    @Override
    protected void onPostExecute(RestResult result) {
        if (result.isError()) {
            callback.onError(result);
        } else {
            try {
                callback.onComplete(result.getObject());
            } catch (JSONException e) {
                callback.onError(new RestResult(e, result.getStatusCode(), result.getObject()));
            }
        }
        super.onPostExecute(result);
    }

    private String requestWithQueryString(Uri.Builder uriBuilder) throws URISyntaxException {
        if (params != null) {
            for (String key : params.keySet()) {
                uriBuilder.appendQueryParameter(key, params.get(key));
            }
        }

        return uriBuilder.build().toString();
    }

    private RequestBody requestWithEntity() throws UnsupportedEncodingException, URISyntaxException {
        final FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }

        return builder.build();
    }

    public static Map<String, String> paramsToMap(List<BasicNameValuePair> pairs) {
        final Map<String, String> map = new HashMap<>();
        for (BasicNameValuePair pair : pairs) {
            map.put(pair.getName(), pair.getValue());
        }

        return map;
    }

    public static List<BasicNameValuePair> paramsToList(Map<String, String> params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value != null)
                formList.add(new BasicNameValuePair(key, value));
        }
        return formList;
    }
}
