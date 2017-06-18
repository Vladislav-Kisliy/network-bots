package com.karlsoft.network.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad on 18.06.17.
 */
public class SshClient {

    private final static String OUTPUT_KEY = "output";
    private final static String ERROR_KEY = "error";
    private final String host;
    private final String user;
    private final String password;
    private final JSch jsch = new JSch();

    public SshClient(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public void executeCommand(Map<String, Object> context, String... commands) {
        try {
            // Right way for known hosts
//            jsch.setKnownHosts(knownHostsFileName);
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            // Wrong way but necessary for scanning
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(1500);

            for (String command : commands) {
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);

                channel.setInputStream(null);
                InputStream errStream = ((ChannelExec) channel).getErrStream();
                InputStream inputStream = channel.getInputStream();
                channel.connect();

                Map<String, byte[]> commandOutput = new HashMap<>();
                commandOutput.put(OUTPUT_KEY, ByteStreams.toByteArray(inputStream));
                commandOutput.put(ERROR_KEY, ByteStreams.toByteArray(errStream));
                context.put(command, commandOutput);
                channel.disconnect();
            }
            session.disconnect();
        } catch (JSchException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SshClient sshClient = new SshClient("localhost",
                "test", "test");
        Map<String, Object> context = new HashMap<>();
        sshClient.executeCommand(context, new String[]{"ls", "w", "ps aux"});
        System.out.println("context =" + context);
        for (Map.Entry<String, Object> item : context.entrySet()) {
            Map<String, byte[]> innerMap = (Map<String, byte[]>) item.getValue();
            byte[] errorBytes = innerMap.get(ERROR_KEY);
            byte[] outputBytes = innerMap.get(OUTPUT_KEY);
            System.out.printf("command =" + item.getKey());
            System.out.println(", output =" + new String(outputBytes));
//                    + ", error =" + new String(errorBytes));
        }
    }
}

