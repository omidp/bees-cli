package com.cloudbees.sdk.commonds.debugger;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copies stream and closes at the end.
 * @author Kohsuke Kawaguchi
 */
class StreamCopyThread extends Thread {
    private final InputStream in;
    private final OutputStream out;

    StreamCopyThread(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            IOUtils.copy(in,out);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Connection aborted",e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(StreamCopyThread.class.getName());
}
