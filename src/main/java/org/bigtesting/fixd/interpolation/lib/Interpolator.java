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
package org.bigtesting.fixd.interpolation.lib;

import java.util.ArrayList;
import java.util.List;

import org.bigtesting.fixd.interpolation.lib.core.InterpolationHandlerImpl;

/**
 * 
 * @author Luis Antunes
 */
public class Interpolator {
    
    private final List<InterpolationHandlerImpl> handlers = new ArrayList<InterpolationHandlerImpl>();
    
    /*
     * TODO implement escape
     */
    private String escape;
    
    public InterpolationHandler when() {
        
        InterpolationHandlerImpl handler = new InterpolationHandlerImpl();
        handlers.add(handler);
        return handler;
    }
    
    public InterpolationHandler when(String characterClass) {
        
        InterpolationHandlerImpl handler = new InterpolationHandlerImpl(characterClass);
        handlers.add(handler);
        return handler;
    }

    public void escapeWith(String escape) {
        
        this.escape = escape;
    }
    
    public String interpolate(String toInterpolate, Object arg) {
        
        for (InterpolationHandlerImpl handler : handlers) {
            
            toInterpolate = handler.interpolate(toInterpolate, arg);
        }
        
        return toInterpolate;
    }
}
