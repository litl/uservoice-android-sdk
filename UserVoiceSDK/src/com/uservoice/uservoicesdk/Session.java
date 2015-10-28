package com.uservoice.uservoicesdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.litl.signpost.okhttp.OkHttpOAuthConsumer;
import com.uservoice.uservoicesdk.model.AccessToken;
import com.uservoice.uservoicesdk.model.ClientConfig;
import com.uservoice.uservoicesdk.model.Forum;
import com.uservoice.uservoicesdk.model.RequestToken;
import com.uservoice.uservoicesdk.model.Topic;
import com.uservoice.uservoicesdk.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuthConsumer;

public class Session {

    private static Session instance;

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private Session() {
    }

    private Context context;
    private Config config;
    private OAuthConsumer oauthConsumer;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private User user;
    private ClientConfig clientConfig;
    private Forum forum;
    private List<Topic> topics;
    private Map<String, String> externalIds = new HashMap<String, String>();
    private Runnable signinListener;
    private UserVoice.IHelpLinkClickedCallback helpLinkCallback;

    public Context getContext() {
        return context;
    }

    public Config getConfig() {
        if (config == null && context != null) {
            config = Config.load(getSharedPreferences(), "config", "config", Config.class);
        }
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
        if (config.getEmail() != null) {
            persistIdentity(config.getName(), config.getEmail());
        }
        config.persist(getSharedPreferences(), "config", "config");
        persistSite();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void persistIdentity(String name, String email) {
        Editor edit = getSharedPreferences().edit();
        edit.putString("user_name", name);
        edit.putString("user_email", email);
        edit.commit();
    }

    public String getName() {
        if (user != null)
            return user.getName();
        return getSharedPreferences().getString("user_name", null);
    }

    public String getEmail() {
        if (user != null)
            return user.getEmail();
        return getSharedPreferences().getString("user_email", null);
    }

    public RequestToken getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(RequestToken requestToken) {
        this.requestToken = requestToken;
    }

    public OAuthConsumer getOAuthConsumer() {
        if (oauthConsumer == null) {
            if (getConfig().getKey() != null)
                oauthConsumer = new OkHttpOAuthConsumer(getConfig().getKey(), getConfig().getSecret());
            else if (clientConfig != null)
                oauthConsumer = new OkHttpOAuthConsumer(clientConfig.getKey(), clientConfig.getSecret());
        }
        return oauthConsumer;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(Context context, AccessToken accessToken) {
        this.accessToken = accessToken;
        accessToken.persist(getSharedPreferences(), "access_token", "access_token");
        if (signinListener != null)
            signinListener.run();
    }

    protected void persistSite() {
        Editor edit = context.getSharedPreferences("uv_site", 0).edit();
        edit.putString("site", config.getSite());
        edit.commit();
    }

    public SharedPreferences getSharedPreferences() {
        String site;
        if (config != null) {
            site = config.getSite();
        } else {
            site = context.getSharedPreferences("uv_site", 0).getString("site", null);
        }
        return context.getSharedPreferences("uv_" + site.replaceAll("\\W", "_"), 0);
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        persistIdentity(user.getName(), user.getEmail());
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void setExternalId(String scope, String id) {
        externalIds.put(scope, id);
    }

    public Map<String, String> getExternalIds() {
        return externalIds;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setSignInListener(Runnable runnable) {
        signinListener = runnable;
    }

    public UserVoice.IHelpLinkClickedCallback getHelpLinkCallback() {
        return helpLinkCallback;
    }

    public void setHelpLinkCallback(UserVoice.IHelpLinkClickedCallback helpCallback) {
        this.helpLinkCallback = helpCallback;
    }
}
