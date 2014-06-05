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

import java.util.regex.Pattern;

import org.bigtesting.fixd.interpolation.lib.PrefixHandler;

/**
 * 
 * @author Luis Antunes
 */
public class PrefixHandlerImpl extends SubstitutionHandlerImpl
    implements PrefixHandler {

    private final Pattern pattern;
    
    private final String prefix;
    
    public PrefixHandlerImpl(String prefix, String characterClass) {
        
        this.prefix = prefix;
        
        String quotedPrefix = Pattern.quote(prefix);
        if (characterClass == null) {
            characterClass = "[^" + quotedPrefix + "\\s]+";
        }
        this.pattern = Pattern.compile("(" + quotedPrefix + characterClass + ")");
    }
    
    @Override
    protected Pattern getPattern() {
        
        return pattern;
    }
    
    @Override
    protected String getCaptured(String found) {

        return found.substring(prefix.length());
    }
}
