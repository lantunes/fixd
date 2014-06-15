/*
 * Copyright (C) 2014 BigTesting.org
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
package org.bigtesting.fixd.core.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bigtesting.fixd.core.RequestHandlerImpl;

/**
 * 
 * @author Luis Antunes
 */
public class Subscriber {
    
    private final BlockingQueue<Broadcast> broadcasts = 
            new LinkedBlockingQueue<Broadcast>();
    
    private final RequestHandlerImpl handler;
    
    public Subscriber(RequestHandlerImpl handler) {
        
        this.handler = handler;
    }
    
    public Broadcast getNextBroadcast() throws InterruptedException {
        
        return broadcasts.take();
    }
    
    public void addNextBroadcast(Broadcast broadcast) {
        
        broadcasts.add(broadcast);
    }
    
    public RequestHandlerImpl getHandler() {
        
        return handler;
    }
}
