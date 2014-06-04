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
import java.util.Collections;
import java.util.List;

import org.bigtesting.fixd.interpolation.lib.core.EscapeHandler;
import org.bigtesting.fixd.interpolation.lib.core.Interpolating;
import org.bigtesting.fixd.interpolation.lib.core.InterpolationHandlerImpl;
import org.bigtesting.fixd.interpolation.lib.core.Substitution;

/**
 * 
 * @author Luis Antunes
 */
public class Interpolator {
    
    private final List<Interpolating> interpolating = new ArrayList<Interpolating>();
    
    public InterpolationHandler when() {
        
        InterpolationHandlerImpl handler = new InterpolationHandlerImpl();
        interpolating.add(handler);
        return handler;
    }
    
    public InterpolationHandler when(String characterClass) {
        
        InterpolationHandlerImpl handler = new InterpolationHandlerImpl(characterClass);
        interpolating.add(handler);
        return handler;
    }

    public void escapeWith(String escape) {
        
        interpolating.add(new EscapeHandler(escape));
    }
    
    public String interpolate(String toInterpolate, Object arg) {
        
        List<Substitution> substitutions = new ArrayList<Substitution>();
        for (Interpolating handler : interpolating) {
            
            substitutions.addAll(handler.interpolate(toInterpolate, arg));
        }
        
        Collections.sort(substitutions);
        
        StringBuilder sb = new StringBuilder(toInterpolate);
        int diff = 0;
        int lastEnd = 0;
        Substitution lastEscape = null;
        for (int i = 0; i < substitutions.size(); i++) {
            
            Substitution sub = substitutions.get(i);
            
            if (sub.start() < lastEnd) continue;
            
            if (sub.isEscape()) {
                
                if (lastEscape != null && sub.isAfter(lastEscape)) {
                    continue;
                }
                    
                if (isActualEscape(sub, substitutions, i)) {
                    lastEscape = sub;
                } else {
                    continue;
                }
                
            } else if (lastEscape != null && sub.isAfter(lastEscape)) {
                
                continue;
            }
            
            if (sub.value() == null) continue;
            
            sb.replace(sub.start() - diff, sub.end() - diff, sub.value());
            diff += sub.found().length() - sub.value().length();
            lastEnd = sub.end();
        }
        return sb.toString();
    }
    
    private boolean isActualEscape(Substitution esc, List<Substitution> substitutions, int currentIndex) {
        
        if (!hasNext(substitutions, currentIndex)) return false;
        
        Substitution nextSub = getNext(substitutions, currentIndex);
        
        if (!nextSub.isAfter(esc)) return false;
        
        if (!nextSub.isEscape()) return true;
        
        /*
         * nextSub is an escape immediately after the current escape;
         * look ahead to see if a non-escape substitution occurs
         */
        return isActualEscape(nextSub, substitutions, ++currentIndex);
    }
    
    private boolean hasNext(List<Substitution> substitutions, int currentIndex) {
        return (currentIndex + 1) < substitutions.size();
    }
    
    private Substitution getNext(List<Substitution> substitutions, int currentIndex) {
        return substitutions.get(currentIndex + 1);
    }
}
