/*
 * Copyright (C) 2015 BigTesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigtesting.fixd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.core.FixdServer;
import org.bigtesting.fixd.core.FixtureContainer;
import org.bigtesting.fixd.core.RequestHandlerImpl;
import org.bigtesting.fixd.core.RequestMarshallerImpl;
import org.bigtesting.fixd.core.RequestUnmarshallerImpl;
import org.bigtesting.fixd.util.LoggingAgent;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * 
 * @author Luis Antunes
 */
public class ServerFixture {

    private final int port;
    private final FixtureContainer container;
    
    private Server server;
    private Connection connection;
    private InetSocketAddress actualConnectionAddress;
    
    /**
     * Constructs a ServerFixture with the port chosen automatically.
     * The actual port used can be obtained from the {@link #getPort() getPort} method.
     */
    public ServerFixture() {
        this(0);
    }
    
    public ServerFixture(int port) {
        
        this.port = port;
        this.container = new FixtureContainer();
    }
    
    public ServerFixture(int port, int aysncThreadPoolSize) {
        
        this.port = port;
        this.container = new FixtureContainer(aysncThreadPoolSize);
    }
    
    public void start() throws IOException {
        
        server = new FixdServer(new ContainerServer(container));
        connection = new SocketConnection(server, new LoggingAgent());
        SocketAddress address = new InetSocketAddress(port);
        
        actualConnectionAddress = (InetSocketAddress)connection.connect(address);
    }
    
    public void stop() throws IOException {
        
        if (connection == null || server == null) {
            throw new IllegalStateException("server has not been started");
        }
        container.stop();
        connection.close();
        server.stop();
    }
    
    public int getPort() {
        
        if (actualConnectionAddress == null) {
            throw new IllegalStateException("server has not been started");
        }
        return actualConnectionAddress.getPort();
    }
    
    public RequestHandler handle(Method method, String resource) {
        
        RequestHandlerImpl handler = new RequestHandlerImpl(container);
        container.addHandler(handler, method, resource);
        return handler;
    }
    
    public RequestHandler handle(Method method, String resource, String contentType) {
        
        RequestHandlerImpl handler = new RequestHandlerImpl(container);
        container.addHandler(handler, method, resource, contentType);
        return handler;
    }
    
    public Collection<CapturedRequest> capturedRequests() {
        
        return container.getCapturedRequests();
    }
    
    public CapturedRequest request() {
        
        return container.nextCapturedRequest();
    }
    
    public void setMaxCapturedRequests(int limit) {
        
        container.setCapturedRequestLimit(limit);
    }
    
    public RequestMarshaller marshal(String contentType) {
        
        RequestMarshallerImpl marshaller = new RequestMarshallerImpl(contentType);
        container.addContentMarshaller(contentType, marshaller);
        return marshaller;
    }
    
    public RequestUnmarshaller unmarshal(String contentType) {
        
        RequestUnmarshallerImpl marshaller = new RequestUnmarshallerImpl(contentType);
        container.addContentUnmarshaller(contentType, marshaller);
        return marshaller;
    }
}
