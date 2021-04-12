package com.jetbrains.summer.ssh_proxy.general;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;

class CLI {
    public static class CLIException extends Exception {
        public CLIException(String message) {
            super(message);
        }

        public CLIException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final int type;
    private final String host;
    private final int port;
    private final static String help =
            "Usage: \n" +
                    "app client <host> <port>\n" +
                    "or\n" +
                    "app server <host> <port>\n";

    CLI(String[] args) throws CLIException {
        if (args.length != 3) {
            throw new CLIException(help);
        }

        if (args[0].equals("server")) {
            type = 0;
        } else if (args[0].equals("client")) {
            type = 1;
        } else {
            throw new CLIException("Error: first argument must be client or server");
        }
        host = args[1];
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new CLIException("Error: port is not a number", e);
        }
        if (!(1 <= port && port <= 65535)) {
            throw new CLIException("Error: port must be in range 1-65535");
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getType() {
        return type;
    }
}

class Fibonacci {
    private static final BigInteger[][] id;
    private static final BigInteger[][] fibMatrix;

    static {
        id = new BigInteger[][]{
                {BigInteger.valueOf(1), BigInteger.valueOf(0)},
                {BigInteger.valueOf(0), BigInteger.valueOf(1)}
        };
        fibMatrix = new BigInteger[][]{
                {BigInteger.valueOf(0), BigInteger.valueOf(1)},
                {BigInteger.valueOf(1), BigInteger.valueOf(1)}
        };
    }

    private static BigInteger[][] multiply2x2(BigInteger[][] m1, BigInteger[][] m2) {
        BigInteger[][] m3 = new BigInteger[2][2];
        m3[0][0] = m1[0][0].multiply(m2[0][0]).add(m1[0][1].multiply(m2[1][0]));
        m3[1][1] = m1[1][0].multiply(m2[1][0]).add(m1[1][1].multiply(m2[1][1]));
        m3[0][1] = m1[0][0].multiply(m2[1][0]).add(m1[0][1].multiply(m2[1][1]));
        m3[1][0] = m1[1][0].multiply(m2[0][0]).add(m1[1][1].multiply(m2[1][0]));

        return m3;
    }

    private static BigInteger[][] byNumberHelper(BigInteger[][] m, long n) {
        if (n == 0) return id;
        if (n == 1) return m;
        if (n % 2 == 1) return multiply2x2(m, byNumberHelper(m, n - 1));
        BigInteger[][] powHalfN = byNumberHelper(m, n / 2);
        return multiply2x2(powHalfN, powHalfN);
    }

    static BigInteger byNumber(long n) {
        BigInteger[][] fibMatrixPowered = byNumberHelper(fibMatrix, n);
        return fibMatrixPowered[0][1];
    }
}


class Server implements Runnable {

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

    String address;
    int port;
    ArrayList<Thread> clientPool = new ArrayList<>();

    Server(String address, int port) {
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


class Client implements Runnable {
    private final String host;
    private final int port;
    private final InputStream in;

    Client(String host, int port, InputStream in) {
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
//}


public class GeneralClientServer {
    public static void main(String[] args) {
        CLI cli;
        try {
            cli = new CLI(args);
        } catch (CLI.CLIException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        if (cli.getType() == 0) {
            Thread serverThread = new Thread(new Server(cli.getHost(), cli.getPort()));
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
        } else if (cli.getType() == 1) {
            Thread clientCLI = new Thread(new Client(cli.getHost(), cli.getPort(), System.in));
            clientCLI.start();
        }
    }
}
