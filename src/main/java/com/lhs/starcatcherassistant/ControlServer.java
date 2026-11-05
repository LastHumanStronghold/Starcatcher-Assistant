package com.lhs.starcatcherassistant;

import java.io.*;
import java.net.*;

final class ControlServer {
    private static ClientAutomation automation;
    static void start() {
        try {
            var field=StarcatcherAssistant.class.getDeclaredField("unused");
        } catch(Exception ignored) {}
        Thread t=new Thread(() -> {
            try(ServerSocket server=new ServerSocket(28741,8,InetAddress.getByName("127.0.0.1"))) {
                while(true) try(Socket s=server.accept(); BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream())); BufferedWriter out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
                    String line=in.readLine(); if(line!=null) { ClientAutomationBridge.dispatch(line); out.write("OK\n"); out.flush(); }
                } catch(IOException ignored) {}
            } catch(IOException ignored) {}
        },"StarcatcherAssistant-Control"); t.setDaemon(true); t.start();
    }
}
