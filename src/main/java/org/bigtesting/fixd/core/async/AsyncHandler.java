/*
 * Copyright (C) 2013 BigTesting.org
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import org.bigtesting.fixd.core.RequestHandlerImpl;
import org.bigtesting.fixd.core.ResponseBody;
import org.bigtesting.fixd.marshalling.MarshallerProvider;
import org.bigtesting.fixd.marshalling.UnmarshallerProvider;
import org.bigtesting.routd.Route;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public class AsyncHandler {

    private final List<Queue<Broadcast>> subscribers = 
            Collections.synchronizedList(new ArrayList<Queue<Broadcast>>());
    
    private final ExecutorService asyncExecutor;
    
    public AsyncHandler(ExecutorService asyncExecutor) {
        
        this.asyncExecutor = asyncExecutor;
    }
    
    public void doAsync(Response response, RequestHandlerImpl handler, 
            String responseContentType, ResponseBody responseBody,
            MarshallerProvider marshallerProvider,
            UnmarshallerProvider unmarshallerProvider) {
        
        AsyncTask task = new AsyncTask(response, handler, subscribers,
                responseContentType, responseBody, 
                marshallerProvider, unmarshallerProvider);
        asyncExecutor.execute(task);
    }
    
    public void broadcastToSubscribers(Request request, Route route) {
        
        synchronized (subscribers) {
            for (Queue<Broadcast> broadcasts : subscribers) {
                broadcasts.add(new Broadcast(request, route));
            }
        }
    }
    
    public void stop() {
        
        asyncExecutor.shutdown();
    }
}
