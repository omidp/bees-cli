package com.cloudbees.sdk.commonds.debugger;

import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import hudson.remoting.SocketInputStream;
import hudson.remoting.SocketOutputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.kohsuke.args4j.Argument;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
@CommandGroup("Debugger")
@CLICommand("debugger:app")
public class DebuggerCmd extends AbstractCommand {
    @Argument(metaVar="APPID",required=true,usage="appId to debug")
    String appId;
    
    @Argument(metaVar="PORT",index=1,required=false,usage="TCP/IP port to listen to")
    int port = 5005;

    @Override
    public int main() throws Exception {
        ServerSocket ss = new ServerSocket(port,10,InetAddress.getLocalHost());

        // the trick is to run the server on port 443 so that we can tunnel
        // this over HTTP proxy by pretending to be HTTPS
        //        Connection connection = new Connection("debugger.cloudbees.com", 443);
        Connection connection = new Connection("localhost", 2222);

        try {
            connection.connect(); // TODO: verify the host key

            // TODO: figure out the user name
            if (!connection.authenticateWithPublicKey("kohsuke", new File("/home/kohsuke/.ssh/id_rsa"), null)) {
                System.err.println("Public key authentication with the server failed");
                return 1;
            }

            while (true) {
                Socket clientSocket = ss.accept();
                clientSocket.setTcpNoDelay(true);
                try {
                    tunnel(clientSocket,connection.openSession());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to tunnel connection", e);
                } finally {
                    clientSocket.close();
                }
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Runs the tunnel and returns when it's shut down.
     */
    private void tunnel(Socket clientSocket, Session ssh) throws IOException, InterruptedException {
        try {
            ssh.execCommand("debug "+appId);
            // connect streams all around
            StreamCopyThread t1 = new StreamCopyThread(ssh.getStdout(), new SocketOutputStream(clientSocket));
            t1.start();
            StreamCopyThread t2 = new StreamCopyThread(new SocketInputStream(clientSocket), ssh.getStdin());
            t2.start();
            StreamCopyThread t3 = new StreamCopyThread(ssh.getStderr(), new CloseShieldOutputStream(System.err));
            t3.start();

            t1.join();
            t2.join();
            t3.join();
        } finally {
            ssh.close();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DebuggerCmd.class.getName());
}
