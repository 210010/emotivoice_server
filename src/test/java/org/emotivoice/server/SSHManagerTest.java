package org.emotivoice.server;

import java.io.File;
import java.io.FileOutputStream;

import org.emotivoice.server.SSHManager.CommandResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SSHManagerTest {

    @Value("${hostname}") private String hostname;
    @Value("${username}") private String username;
    @Value("${password}") private String password;
    @Value("${remote_path}") private String remotePath;
    @Value("${local_path}") private String localPath;

    @Test
    public void executeCommand() throws Exception {
        SSHManager manager = new SSHManager(hostname, username, password);

        manager.connect();

        CommandResult result = manager.executeCommand("ls");
        System.out.println(result.toString());

        manager.disconnect();
    }

    @Test
    public void executeCommand_complex() throws Exception {
        SSHManager manager = new SSHManager(hostname, username, password);
        manager.connect();

        String interpreter = "/usr/bin/python3";
        String script = "~/arg_test.py";
        String arg_foo = "'first line'";
        String arg_bar = "'second line'";

        CommandResult result = manager.executeCommand(
                String.format("%s %s --foo %s --bar %s", interpreter, script, arg_foo, arg_bar)
        );
        System.out.println(result.toString());

        manager.disconnect();
    }

    @Test
    public void download() throws Exception {
        SSHManager manager = new SSHManager(hostname, username, password);

        manager.connect();

        File file = new File(localPath);
        FileOutputStream fout = new FileOutputStream(file);
        byte[] binaryData = manager.download(remotePath);
        fout.write(binaryData);
        fout.close();

        manager.disconnect();
    }
}
