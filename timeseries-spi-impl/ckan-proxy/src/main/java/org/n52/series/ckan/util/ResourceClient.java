/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.ckan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ProxySelector;
import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;

public class ResourceClient {
        
    private final ResourceClientConfig config;
    
    public ResourceClient() {
        this(new ResourceClientConfig());
    }
    
    public ResourceClient(ResourceClientConfig config) {
        this.config = config;
    }
    
    public String downloadTextResource(String url) throws IOException {
        try (CloseableHttpClient client = create()) {
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            HttpGet httpget = new HttpGet(url);
            return client.execute(httpget, responseHandler);
        }
    }

    private CloseableHttpClient create() {
        return HttpClients.custom()
                .setConnectionManager(createPooledConnectionManager())
                .setDefaultCredentialsProvider(createCredentialsProvider())
                .setDefaultRequestConfig(createDefaultRequestConfig())
                .setRoutePlanner(createProxyRoutePlanner())
                .build();
    }

    private static SystemDefaultRoutePlanner createProxyRoutePlanner() {
        return new SystemDefaultRoutePlanner(ProxySelector.getDefault());
    }
    
    private CredentialsProvider createCredentialsProvider() {
        return new BasicCredentialsProvider();
    }
    
    private PoolingHttpClientConnectionManager createPooledConnectionManager() {
        final Registry<ConnectionSocketFactory> connSocketFactory = createConnectionSocketFactory();
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(connSocketFactory);
        cm.setDefaultConnectionConfig(createConnectionConfig());
        cm.setMaxTotal(config.getMaxConnectionPoolSize());
        return cm;
    }
    
    private Registry<ConnectionSocketFactory> createConnectionSocketFactory() {
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        return RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https", new SSLConnectionSocketFactory(sslcontext))
            .build();
    }
    
    private ConnectionConfig createConnectionConfig() {
        return ConnectionConfig.custom()
                .setCharset(Consts.UTF_8)
                .build();
    }
    
    private RequestConfig createDefaultRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(config.getSocketTimeout())
                .setConnectTimeout(config.getConnectTimeout())
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .build();
    }

}
