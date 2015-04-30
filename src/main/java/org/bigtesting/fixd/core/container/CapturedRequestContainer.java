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
package org.bigtesting.fixd.core.container;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.capture.impl.SimpleCapturedRequest;

/**
 * 
 * @author Luis Antunes
 */
class CapturedRequestContainer {
    
    private int capturedRequestLimit = -1;

    private final Queue<CapturedRequest> capturedRequests = 
            new ConcurrentLinkedQueue<CapturedRequest>();
    
    public Queue<CapturedRequest> getCapturedRequests() {
        
        return capturedRequests;
    }
    
    public CapturedRequest nextCapturedRequest() {
        
        return capturedRequests.poll();
    }
    
    public void setCapturedRequestLimit(int limit) {
        
        this.capturedRequestLimit = limit;
    }
    
    public void addCapturedRequest(SimpleCapturedRequest captured) {
        
        capturedRequests.add(captured);
        
        if (capturedRequestLimit > -1) {
            while(capturedRequests.size() > capturedRequestLimit) capturedRequests.remove();
        }
    }
}
