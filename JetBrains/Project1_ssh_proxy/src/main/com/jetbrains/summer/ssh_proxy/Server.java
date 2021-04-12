package com.jetbrains.summer.ssh_proxy;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;

public class Server {

    private static class SimpleClientRunnable implements Runnable {
        final Socket socket;

        SimpleClientRunnable(Socket socket) throws IOException {
            this.socket = socket;
            socket.setSoTimeout(100);
        }

        @Override
        public void run() {
            try (
                    final BufferedReader readerSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final BufferedWriter writerSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                while (!Thread.interrupted()) {
                    String nextLine;
                    try {
                        nextLine = readerSocket.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (nextLine == null)
                        break;

                    final long nextNum;
                    try {
                        nextNum = Long.parseLong(nextLine);
                    } catch (NumberFormatException e) {
                        throw new IOException("Error: input is not a number", e);
                    }

                    BigInteger answer = Fibonacci.byNumber(nextNum);

                    writerSocket.write(answer.toString() + "\n");
                    writerSocket.flush();
                }
            } catch (IOException e) {
                System.err.println("Error on connection " + Thread.currentThread().getName());
                System.err.println(e.getMessage());
            }
        }
    }

    private static class SimpleServerRunnable implements Runnable {
        String address;
        int port;
        ArrayList<Thread> clientPool = new ArrayList<>();

        SimpleServerRunnable(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public void run() {
            try (
                    final ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(address))
            ) {
                serverSocket.setSoTimeout(100);
                while (!Thread.interrupted()) {
                    Socket nextSocket;
                    try {
                        nextSocket = serverSocket.accept();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    Thread nextThread = new Thread(new SimpleClientRunnable(nextSocket));
                    nextThread.setName(nextSocket.getInetAddress() + " " + nextSocket.getPort());
                    clientPool.add(nextThread);
                    nextThread.start();
                }
                for (Thread c : clientPool) {
                    c.interrupt();
                }
            } catch (UnknownHostException e) {
                System.err.println("Error: unknown host");
                System.err.println(e.getMessage());
                System.exit(2);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(10);
            }
        }
    }

    public static void main(String[] args) {
        CLI cli;
        try {
            cli = new CLI(args);
        } catch (CLI.CLIException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        Thread serverThread = new Thread(new SimpleServerRunnable(cli.getHost(), cli.getPort()));
        serverThread.start();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            while (true) {
                String nextLine = reader.readLine();
                if ("exit".equals(nextLine)) {
                    serverThread.interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Command line IOException:");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
