package com.cloudbees.sdk;

import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates access to the persisted installed plugins list (~/.bees/plugins-list)
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class InstalledPluginList {
    @Inject
    DirectoryStructure structure;
    
    private List<GAV> get() throws IOException {
        List<GAV> list = new ArrayList<GAV>();
        
        File f = new File(structure.localRepository, "plugins-list");
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        try {
            String line;
            while ((line=r.readLine())!=null) {
                if (line.startsWith("#"))   continue;
                list.add(new GAV(line.trim()));
            }
        } finally {
            IOUtils.closeQuietly(r);
        }
        return list;
    }
    
    public synchronized void add(GAV gav) throws IOException {
        List<GAV> list = get();
        if (!list.contains(gav))
            list.add(gav);
        set(list);
    }

    public synchronized void set(List<GAV> list) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(structure.localRepository, "plugins-list");
        PrintWriter w = new PrintWriter(f,"UTF-8");
        try {
            for (GAV i : list) {
                w.println(i);
            }
        } finally {
            IOUtils.closeQuietly(w);
        }
    }
}
