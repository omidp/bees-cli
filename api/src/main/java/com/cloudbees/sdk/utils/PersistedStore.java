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

package com.cloudbees.sdk.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Persisted configuration store of {@code Map<String,V>} (for arbitrary V) for configuration data.
 * 
 * <p>
 * The contents is persisted as a directory where the key name is the file name and {@link V} as the content of the file.
 * This format allows users to add/remove these things easily from shell. It also simplifies the 3rd party to instruct
 * users to tweak the configuration, as well as making it easy to programmatically modify them without losing comments.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class PersistedStore<V> extends AbstractMap<String,V> {
    /**
     * Returns the directory that serves as the persistent storage.
     */
    protected abstract File getStorageDirectory();

    /**
     * Reads a data file 'f' into the value.
     *
     * @throws IllegalArgumentException
     *      if you fail to read it because the content is invalid
     * @return
     *      null if the file doesn't exist or the content is empty.
     */
    protected abstract V read(File f) throws IllegalArgumentException;

    /**
     * Writes the given value to the file
     *
     * @param data
     *      Never null.
     */
    protected abstract void write(File f, V data);


    /**
     * Where do we store the data for the given key?
     */
    private File file(String key) {
        return new File(getStorageDirectory(), key+ EXT);
    }
    
    @Override
    public V get(Object key) {
        return key instanceof String ? this.get((String) key) : null;
    }

    public V get(String key) {
        return read(file(key));
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key)!=null;
    }

    @Override
    public V put(String key, V value) {
        return _write(file(key), value);
    }

    @Override
    public V remove(Object key) {
        return key instanceof String ? put((String)key,null) : null;
    }

    public V remove(String key) {
        V old = get(key);
        file(key).delete();
        return old;
    }

    @Override
    public void clear() {
        for (File f : listFiles())
            f.delete();
    }

    protected V _write(File f, V value) {
        V old = null;
        try {
            old = get(f);
        } catch (Exception e) {
            // if we fail to read the current value, just ignore it because we'll be writing a new value.
        }
        if (value==null) {
            f.delete();
        } else {
            f.getParentFile().mkdirs();
            write(f,value);
        }
        return old;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return new AbstractSet<Entry<String, V>>() {
            @Override
            public Iterator<Entry<String, V>> iterator() {
                List<Entry<String, V>> r = new ArrayList<Entry<String, V>>();
                for (final File f : listFiles()) {
                    r.add(new Entry<String, V>() {
                        public String getKey() {
                            return getKey_(f.getName());
                        }

                        public V getValue() {
                            return read(f);
                        }

                        public V setValue(V value) {
                            return _write(f, value);
                        }
                    });
                }
                return r.iterator();
            }

            @Override
            public int size() {
                return keySet().size();
            }
        };
    }

    @Override
    public Set<String> keySet() {
        Set<String> r = new TreeSet<String>();
        for (File f : listFiles()) {
            r.add(getKey_(f.getName()));
        }
        return r;
    }

    private String getKey_(String name) {
        return name.substring(0, name.lastIndexOf('.'));
    }

    private File[] listFiles()  {
        File[] r = getStorageDirectory().listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isFile() && f.getName().endsWith(EXT);
            }
        });
        if (r==null)    return new File[0];
        return r;
    }

    /**
     * Using a file name extension allows the user to "comment out" entries by renaming them.
     */
    private static final String EXT = ".txt";
}
