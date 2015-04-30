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

import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public abstract class ResponseBody {

    public abstract void send(Response resp, String contentType);
    
    public abstract void sendAndCommit(Response resp, String contentType);
    
    public abstract boolean hasContent();
    
    protected void addStandardHeaders(Response response, String responseContentType) {
        
        long time = System.currentTimeMillis();
        response.setValue("Content-Type", responseContentType);
        response.setValue("Server", "Fixd/1.0 (Simple 5.1.4)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
    }
}
