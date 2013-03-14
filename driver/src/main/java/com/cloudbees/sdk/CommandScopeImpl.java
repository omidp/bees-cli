/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk;

import com.cloudbees.sdk.cli.CommandScope;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.internal.CircularDependencyProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Scope} implementation for {@link CommandScope}.
 *
 * @author Kohsuke Kawaguchi
 */
public class CommandScopeImpl implements Scope {
    private static final ThreadLocal<Map<Key,Object>> SCOPED_OBJECTS = new ThreadLocal<Map<Key, Object>>();

    /**
     * Begines a new command scope for the current thread.
     *
     * In the corresponding finally block, you must restore the end block.
     */
    public static Object begin() {
        Map<Key, Object> old = SCOPED_OBJECTS.get();
        SCOPED_OBJECTS.set(new HashMap<Key, Object>());
        return old;
    }

    public static void end(Object old) {
        SCOPED_OBJECTS.set((Map)old);
    }

    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
        return new Provider<T>() {
            public T get() {
                Map<Key, Object> table = SCOPED_OBJECTS.get();
                synchronized (table) {
                    Object obj = table.get(key);
                    if (obj == NULL) {
                        return null;
                    }
                    T t = (T) obj;
                    if (t == null) {
                        t = creator.get();
                        if (!(t instanceof CircularDependencyProxy)) {// not sure exactly what this marker interface means but I'm just following Scopes.SINGLETON
                            table.put(key, (t != null) ? t : NULL);
                        }
                    }
                    return t;
                }
            }

            public String toString() {
                return String.format("CommandScoped[%s]", creator);
            }
        };
    }

    private static final Object NULL = new Object();
}
