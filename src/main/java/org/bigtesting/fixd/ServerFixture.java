/*
 * Copyright (C) 2011-2013 Bigtesting.org
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
    
    public ServerFixture(int port) {
        this(port, 10);
    }
    
    public ServerFixture(int port, int aysncThreadPoolSize) {
        
        this.port = port;
        this.container = new FixtureContainer(aysncThreadPoolSize);
    }
    
    public void start() throws IOException {
        
        server = new ContainerServer(container);
        connection = new SocketConnection(server, new LoggingAgent());
        SocketAddress address = new InetSocketAddress(port);
        connection.connect(address);
    }
    
    public void stop() throws IOException {
        
        connection.close();
        server.stop();
    }
    
    public RequestHandler handle(Method method, String resource) {
        
        RequestHandler handler = new RequestHandler();
        container.addHandler(handler, method, resource);
        return handler;
    }
    
    public RequestHandler handle(Method method, String resource, String contentType) {
        
        RequestHandler handler = new RequestHandler();
        container.addHandler(handler, method, resource, contentType);
        return handler;
    }
}
