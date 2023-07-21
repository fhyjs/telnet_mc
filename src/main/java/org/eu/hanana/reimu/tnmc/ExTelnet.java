package org.eu.hanana.reimu.tnmc;

import cn.fhyjs.cirno.Callback;
import cn.fhyjs.cirno.Telnet;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ExTelnet extends Thread{
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback callback = new Callback() {
        @Override
        public void OnReceive(String s) {
            System.out.println("Server response: " + s);
        }

        @Override
        public void OnExit() {

        }
    };
    public  Socket socket;
    public ExTelnet(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        start();
    }
    public ExTelnet(String host) throws IOException {
        this(host.split(":")[0], host.split(":")[1] == null ? 22 : Integer.parseInt(host.split(":")[1]));
    }
    BufferedWriter writer;
    public void send(String s) throws IOException {
        while (writer==null&&socket.isConnected()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        writer.write(s);
        writer.newLine();
        writer.flush();
    }
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String response;
            while ((response = reader.readLine()) != null) {
                callback.OnReceive(response);
            }
            callback.OnExit();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
