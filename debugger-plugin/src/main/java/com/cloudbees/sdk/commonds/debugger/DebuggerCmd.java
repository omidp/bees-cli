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

import javax.inject.Inject;
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

    @Inject
    SshConnectionFactory sshConnectionFactory;

    @Override
    public int main() throws Exception {
        System.out.println("Connecting to the CloudBees debugger switchboard");
        Connection connection = sshConnectionFactory.connect("localhost",2222);

        try {
            System.out.println("Listening on port "+port+" for incoming debugger connections");
            ServerSocket ss = new ServerSocket(port,10,InetAddress.getLocalHost());
            while (true) {
                Socket clientSocket = ss.accept();
                System.out.println("Debugger connected");
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
