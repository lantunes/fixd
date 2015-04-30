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
package org.bigtesting.fixd.capture.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.util.RequestUtils;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class SimpleCapturedRequest implements CapturedRequest {

    private final Request request;
    
    private boolean broadcast = false;
    
    public SimpleCapturedRequest(Request request) {
        this.request = request;
    }
    
    public String getPath() {
        
        return request.getPath().getPath();
    }

    public String getRequestLine() {
        
        return headerLines()[0];
    }

    public String getMethod() {
        
        return request.getMethod();
    }

    public List<String> getHeaders() {
        
        List<String> headers = new ArrayList<String>();
        String[] lines = headerLines();
        for (int i = 1; i < lines.length; i++) {
            headers.add(lines[i]);
        }
        
        return headers;
    }

    public byte[] getBody() {
        
        try {
            return RequestUtils.readBody(request.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("error getting body", e);
        }
    }

    public String getBody(String encoding) {
        
        try {
            
            return new String(getBody(), encoding);
            
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("error getting encoded body", e);
        }
    }
    
    private String[] headerLines() {
        
        String header = request.getHeader().toString();
        return header.split("\\r?\\n");
    }
    
    public void setBroadcast(boolean broadcast) {
        
        this.broadcast = broadcast;
    }
    
    public boolean isBroadcast() {
        
        return broadcast;
    }
}
