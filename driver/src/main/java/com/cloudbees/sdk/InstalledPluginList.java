package com.cloudbees.sdk;

import com.cloudbees.sdk.utils.Helper;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Encapsulates access to the persisted installed plugins list (~/.bees/plugins-list)
 *
 * This table is a look up table from command prefix (such as 'app' or 'db') to its implementation.
 * 
 * 
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class InstalledPluginList {
    @Inject
    DirectoryStructure structure;

    public GAV get(String prefix) throws IOException {
        return get().get(prefix);
    }
    
    public Map<String,GAV> get() throws IOException {
        Properties data = new Properties();
        Helper.loadProperties(getDataFile(),data);

        Map<String,GAV> r = new HashMap<String,GAV>();
        for (Entry<Object, Object> e : data.entrySet()) {
            r.put((String)e.getKey(), new GAV((String)e.getValue()));
        }
        
        return r;
    }

    private File getDataFile() {
        return new File(structure.localRepository, "plugins-list");
    }

    public synchronized void add(String prefix, GAV gav) throws IOException {
        Map<String,GAV> data = get();
        if (!gav.equals(data.put(prefix, gav)))
            set(data);
    }

    public synchronized void set(Map<String,GAV> data) throws IOException {
        Properties props = new Properties();
        for (Entry<String, GAV> e : data.entrySet()) {
            props.put(e.getKey(),e.getValue().toString());
        }
        FileOutputStream out = new FileOutputStream(getDataFile());
        try {
            props.store(out,"Command prefix -> artifact lookup table");
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
