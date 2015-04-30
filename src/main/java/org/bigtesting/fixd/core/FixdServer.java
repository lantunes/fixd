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
package org.bigtesting.fixd.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;

/**
 * 
 * @author Luis Antunes
 */
public class FixdServer implements Server {

    private final Server server;
    
    public FixdServer(Server server) {
        
        this.server = server;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void process(Socket socket) throws IOException {
        
        Map atts = socket.getAttributes();
        SocketChannel channel = socket.getChannel();
        atts.put("fixd-socket", channel);
        server.process(socket);
    }

    public void stop() throws IOException {
        server.stop();
    }
}
