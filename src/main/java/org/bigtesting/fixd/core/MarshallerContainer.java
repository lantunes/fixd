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
package org.bigtesting.fixd.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bigtesting.fixd.marshalling.Marshaller;
import org.bigtesting.fixd.marshalling.MarshallerProvider;
import org.bigtesting.fixd.marshalling.Unmarshaller;
import org.bigtesting.fixd.marshalling.UnmarshallerProvider;

/**
 * 
 * @author Luis Antunes
 */
class MarshallerContainer {
    
    private final Map<String, RequestMarshallerImpl> contentMarshallers = 
            new ConcurrentHashMap<String, RequestMarshallerImpl>();
    
    private final Map<String, RequestUnmarshallerImpl> contentUnmarshallers = 
            new ConcurrentHashMap<String, RequestUnmarshallerImpl>();
    
    public void addContentMarshaller(String contentType, RequestMarshallerImpl marshaller) {
        
        this.contentMarshallers.put(contentType, marshaller);
    }
    
    public void addContentUnmarshaller(String contentType, RequestUnmarshallerImpl unmarshaller) {
        
        this.contentUnmarshallers.put(contentType, unmarshaller);
    }

    public MarshallerProvider newMarshallerProvider() {
        return new MarshallerProvider() {
            public Marshaller getMarshaller(String contentType) {
                RequestMarshallerImpl marsh = contentMarshallers.get(contentType);
                return marsh != null ? marsh.getMarshaller() : null;
            }
        };
    }
    
    public UnmarshallerProvider newUnmarshallerProvider() {
        return new UnmarshallerProvider() {
            public Unmarshaller getUnmarshaller(String contentType) {
                RequestUnmarshallerImpl unmarsh = contentUnmarshallers.get(contentType);
                return unmarsh != null ? unmarsh.getUnmarshaller() : null;
            }
        };
    }
}
