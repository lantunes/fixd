package org.bigtesting.fixd.capture.impl;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.simpleframework.http.Request;

public class SimpleCapturedRequest implements CapturedRequest {

    private final Request request;
    
    public SimpleCapturedRequest(Request request) {
        this.request = request;
    }
    
    public String getPath() {
        
        return request.getPath().getPath();
    }

    public String getRequestLine() {
        
        String header = request.getHeader().toString();
        String lines[] = header.split("\\r?\\n");
        
        return lines[0];
    }
}
