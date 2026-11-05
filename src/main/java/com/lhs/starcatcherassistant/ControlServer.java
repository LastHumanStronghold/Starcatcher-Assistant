package com.lhs.starcatcherassistant;

import java.io.*;
import java.net.*;

final class ControlServer {
    static void start() {
        Thread t = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(28741, 8, InetAddress.getByName("127.0.0.1"))) {
                while (true) {
                    try (Socket socket = server.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                        String line = in.readLine();
                        if (line != null) {
                            ClientAutomationBridge.dispatch(line);
                            out.write("OK\n");
                            out.flush();
                        }
                    } catch (IOException ignored) {
                        // Keep the local control endpoint alive if a desktop client disconnects.
                    }
                }
            } catch (IOException exception) {
                System.err.println("[Starcatcher Assistant] Could not start local control server: " + exception.getMessage());
            }
        }, "StarcatcherAssistant-Control");
        t.setDaemon(true);
        t.start();
    }
}
