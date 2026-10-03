package com.carrental;

import com.carrental.api.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        new Server().start(port);
    }
}
