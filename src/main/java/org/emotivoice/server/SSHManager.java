package org.emotivoice.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SSHManager {

    public static class CommandResult {
        public final String stdout;
        public final int exitStatus;

        public CommandResult(String stdout, int exitStatus) {
            this.stdout = stdout;
            this.exitStatus = exitStatus;
        }

        @Override
        public String toString() {
            return "CommandResult{" +
                   "stdout='" + stdout + '\'' +
                   ", exitStatus=" + exitStatus +
                   '}';
        }
    }

    private String hostname;
    private int port;
    private String username;
    private String password;

    private JSch jsch;
    private Session session;

    public SSHManager(String hostname, String username, String password) {
        this(hostname, 22, username, password);
    }

    public SSHManager(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        jsch = new JSch();
    }

    public void connect() throws JSchException {
        session = jsch.getSession(username, hostname, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
    }

    public void disconnect() {
        session.disconnect();
    }

    public CommandResult executeCommand(String command) throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        InputStream in = channel.getInputStream();
        channel.connect();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];

        while(true) {
            while (in.available() > 0) {
                int nRead = in.read(tmp, 0, 1024);
                baos.write(tmp, 0, nRead);
            }

            if (channel.isClosed() && in.available() == 0) {
                break;
            }
            else if(!channel.isClosed()) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    /* NOTHING */
                }
            }
        }

        CommandResult result = new CommandResult(
                baos.toString(StandardCharsets.UTF_8),
                channel.getExitStatus()
        );

        channel.disconnect();
        return result;
    }

    public byte[] download(String filename) throws JSchException, SftpException, IOException {
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();

        Path path = Paths.get(filename);
        sftp.cd(path.getParent().toString());
        InputStream in = sftp.get(path.getFileName().toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] tmp = new byte[8192];
        while (true) {
            int nRead = in.read(tmp, 0, 8192);
            if(nRead == -1) break;
            baos.write(tmp, 0, nRead);
        }
        baos.close();
        sftp.disconnect();

        return baos.toByteArray();
    }
}
