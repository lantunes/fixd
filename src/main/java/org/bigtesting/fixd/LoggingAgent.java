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

import java.nio.channels.SocketChannel;

import org.simpleframework.http.core.ContainerEvent;
import org.simpleframework.transport.trace.Agent;
import org.simpleframework.transport.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Antunes
 */
public class LoggingAgent implements Agent {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAgent.class);
    
    public Trace attach(SocketChannel channel) {
        return new LoggingTrace();
    }

    public void stop() {
    }

    private class LoggingTrace implements Trace {

        public void trace(Object event) {
        }

        public void trace(Object event, Object value) {

            if (ContainerEvent.ERROR.equals(event)) {
                logger.error("server encountered error", value);
            }
        }
        
    }
}
