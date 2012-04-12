/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.umlv.qroxy.config.web;

import fr.umlv.qroxy.config.Config;
import java.net.Socket;

/**
 *
 * @author joan
 */
class ThreadServer implements Runnable {
    
    private final Socket socket;
    private final Config config;

    public ThreadServer(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
