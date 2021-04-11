package com.jetbrains.summer.ssh_proxy;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private static class SimpleClientRunnable implements Runnable {
        private final String host;
        private final int port;
        private final InputStream in;

        SimpleClientRunnable(String host, int port, InputStream in) {
            this.host = host;
            this.port = port;
            this.in = in;
        }

        @Override
        public void run() {
            try (
                    final BufferedReader readerCLI = new BufferedReader(new InputStreamReader(in));
                    final Socket socket = new Socket(host, port);
                    final BufferedReader readerSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final BufferedWriter writerSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                while (!Thread.interrupted()) {
                    String nextLine;
                    nextLine = readerCLI.readLine();

                    if ("".equals(nextLine)) {
                        break;
                    }

                    try {
                        Long.parseLong(nextLine);
                    } catch (NumberFormatException e) {
                        throw new IOException("Error: input is not a number or empty line", e);
                    }

                    writerSocket.write(nextLine + "\n");
                    writerSocket.flush();

                    String nextAnswer = readerSocket.readLine();
                    if (nextAnswer == null) {
                        System.err.println("Server closed connection");
                        break;
                    }

                    System.out.println(nextAnswer);
                    System.out.flush();
                }
            } catch (UnknownHostException e) {
                System.err.println("Error: unknown host");
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        final CLI cli;
        try {
            cli = new CLI(args);
        } catch (CLI.CLIException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        Thread clientCLI = new Thread(new SimpleClientRunnable(cli.getHost(), cli.getPort(), System.in));
        clientCLI.start();
    }
}
