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

/**
 * 
 * @author Luis Antunes
 */
public class Substitution implements Comparable<Substitution> {

    private final String found;
    private final String value;
    private final int start;
    private final int end;
    private final boolean escape;

    public Substitution(String found, String value, int start, int end) {
        this(found, value, start, end, false);
    }
    
    public Substitution(String found, String value, int start, int end, boolean escape) {
        
        this.found = found;
        this.value = value;
        this.start = start;
        this.end = end;
        this.escape = escape;
    }

    public String found() {
        return found;
    }
    
    public String value() {
        return value;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public boolean isEscape() {
        return escape;
    }
    
    public boolean isAfter(Substitution that) {
        return this.start() == that.end();
    }
    
    public int compareTo(Substitution that) {
        return Integer.compare(this.start, that.start);
    }
}
