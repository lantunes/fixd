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
package org.bigtesting.fixd.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 
 * @author Luis Antunes
 */
public class Session {

    private final String sessionId;
    
    private final Map<String, Object> values = new HashMap<String, Object>();
    
    private boolean valid = true;
    
    public Session() {
        
        this.sessionId = UUID.randomUUID().toString();
    }
    
    public String getSessionId() {
        
        return sessionId;
    }
    
    public Object get(String key) {
        
        return values.get(key);
    }
    
    public void set(String key, Object value) {
        
        values.put(key, value);
    }
    
    public Set<String> getAttributeNames() {
        
        return values.keySet();
    }
    
    public boolean isValid() {
        
        return valid;
    }
    
    public void invalidate() {
        
        this.valid = false;
    }
}
