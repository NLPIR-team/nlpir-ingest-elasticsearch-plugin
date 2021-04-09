package com.lingjoin.nlpir.plugin.ingest;

import com.google.gson.Gson;
import org.elasticsearch.SpecialPermission;

import java.beans.JavaBean;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

@JavaBean
public class HookInfo {
    protected String scheme;
    protected String host;
    protected int port;
    protected String path;
    protected List<Header> headers;
    protected Map<String, Object> Body;
    protected int retry;


    public static HookInfo fromMap(Map<String, Object> map) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        return AccessController.doPrivileged(
                (PrivilegedAction<HookInfo>) () -> {
                    Gson gson = new Gson();
                    return gson.fromJson(gson.toJson(map), HookInfo.class);
                }
        );

    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public HookInfo() {
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getBody() {
        return Body;
    }

    public void setBody(Map<String, Object> body) {
        Body = body;
    }

    @JavaBean
    public static class Header {
        protected String name;
        protected String value;

        public Header() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
