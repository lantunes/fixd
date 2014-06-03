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

import java.util.ArrayList;
import java.util.List;

import org.bigtesting.fixd.interpolation.lib.EnclosureOpeningHandler;
import org.bigtesting.fixd.interpolation.lib.InterpolationHandler;
import org.bigtesting.fixd.interpolation.lib.PrefixHandler;

/**
 * 
 * @author Luis Antunes
 */
public class InterpolationHandlerImpl implements InterpolationHandler, Interpolating {

    private PrefixHandlerImpl prefixHandler;
    
    private EnclosureOpeningHandlerImpl enclosureOpeningHandler;
    
    private final String characterClass;
    
    public InterpolationHandlerImpl() {
        this(null);
    }
    
    public InterpolationHandlerImpl(String characterClass) {
        this.characterClass = characterClass;
    }
    
    public PrefixHandler prefixedBy(String prefix) {
        
        PrefixHandlerImpl prefixHandler = new PrefixHandlerImpl(prefix, characterClass);
        this.prefixHandler = prefixHandler;
        return prefixHandler;
    }
    
    public EnclosureOpeningHandler enclosedBy(String opening) {
        
        EnclosureOpeningHandlerImpl enclosureOpeningHandler = 
                new EnclosureOpeningHandlerImpl(opening, characterClass);
        this.enclosureOpeningHandler = enclosureOpeningHandler;
        return enclosureOpeningHandler;
    }
    
    public List<Substitution> interpolate(String toInterpolate, Object arg) {
        
        List<Substitution> substitutions = new ArrayList<Substitution>();
        if (prefixHandler != null) {
            
            substitutions.addAll(prefixHandler.interpolate(toInterpolate, arg));
            
        } else if (enclosureOpeningHandler != null) {
            
            substitutions.addAll(enclosureOpeningHandler.getEnclosureClosingHandler()
                                                   .interpolate(toInterpolate, arg));
        }
        
        return substitutions;
    }
}
