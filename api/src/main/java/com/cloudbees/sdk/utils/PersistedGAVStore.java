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

import com.cloudbees.sdk.GAV;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * {@link PersistedStore} for GAV.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class PersistedGAVStore extends PersistedStore<GAV> {
    protected GAV read(File f) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
            try {
                GAV data = null;
                String line;
                while ((line=r.readLine())!=null) {
                    line = line.trim();
                    if (line.startsWith("#") || line.length()==0)
                        continue;   // skip comment line
                    if (data!=null)
                        throw new IllegalArgumentException(f+" contains multiple entries");
                    try {
                        data = new GAV(line);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to parse "+line+" in "+f,e);
                    }
                }
                return data;
            } finally {
                IOUtils.closeQuietly(r);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse "+f,e);
        }
    }

    protected void write(File f, GAV v) {
        try {
            FileOutputStream o = new FileOutputStream(f);
            try {
                IOUtils.write(v.toString(), o,"UTF-8");
            } finally {
                IOUtils.closeQuietly(o);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to "+f,e);
        }
    }
}
