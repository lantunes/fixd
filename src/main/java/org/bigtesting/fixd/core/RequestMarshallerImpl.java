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

import org.bigtesting.fixd.RequestMarshaller;
import org.bigtesting.fixd.marshalling.Marshaller;

/**
 * 
 * @author Luis Antunes
 */
public class RequestMarshallerImpl implements RequestMarshaller {

    private final String contentType;
    
    private Marshaller marshaller;
    
    public RequestMarshallerImpl(String contentType) {
        this.contentType = contentType;
    }
    
    public void with(Marshaller marshaller) {
        this.marshaller = marshaller;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public Marshaller getMarshaller() {
        return marshaller;
    }
}
