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
package org.bigtesting.fixd.interpolation.lib.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigtesting.fixd.interpolation.lib.SubstitutionHandler;
import org.bigtesting.fixd.interpolation.lib.Substitutor;

/**
 * 
 * @author Luis Antunes
 */
public abstract class SubstitutionHandlerImpl implements SubstitutionHandler {

    protected Substitutor substitutor;
    
    public void handleWith(Substitutor substitutor) {
        
        this.substitutor = substitutor;
    }

    protected abstract Pattern getPattern();
    
    protected abstract String getCaptured(String found);
    
    public String interpolate(String toInterpolate, Object arg) {
        
        if (substitutor != null) {
            Matcher m = getPattern().matcher(toInterpolate);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                
                String found = m.group(1);
                String captured = getCaptured(found);
                String substitution = substitutor.substitute(captured, arg);
                
                if (substitution != null) {
                    m.appendReplacement(sb, substitution);
                }
            }
            m.appendTail(sb);
            toInterpolate = sb.toString();
        }
        
        return toInterpolate;
    }
}
