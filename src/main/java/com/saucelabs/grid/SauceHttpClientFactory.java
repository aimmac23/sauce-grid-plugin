package com.saucelabs.grid;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openqa.selenium.remote.internal.HttpClientFactory;

/**
 * @author Ross Rowe
 */
public class SauceHttpClientFactory extends HttpClientFactory {

    private SauceOnDemandRemoteProxy proxy;

    public SauceHttpClientFactory(SauceOnDemandRemoteProxy proxy) {
        this.proxy = proxy;
    }

    public HttpClient getGridHttpClient(int timeout) {
        HttpClient client = super.getGridHttpClient(timeout);

        if (proxy.getRemoteHost().getUserInfo() != null) {
            // Use HTTP Basic auth
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxy.getRemoteHost().getUserInfo());
            ((DefaultHttpClient) client).getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        }
        return client;
    }
}
