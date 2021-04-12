package com.jetbrains.summer.ssh_proxy;

public class CLI {
    public static class CLIException extends Exception {
        public CLIException(String message) {
            super(message);
        }

        public CLIException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final String host;
    private final int port;

    CLI(String[] args) throws CLIException {
        if (args.length != 2) {
            throw new CLIException("Usage: client <host> <port>");
        }
        host = args[0];
        try {
            port = Integer.parseInt(args[1]);
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
}
