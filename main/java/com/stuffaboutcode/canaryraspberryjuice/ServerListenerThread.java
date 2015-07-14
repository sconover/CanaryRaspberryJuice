package com.stuffaboutcode.canaryraspberryjuice;

import java.io.*;
import java.net.*;
import net.canarymod.Canary;

public class ServerListenerThread implements Runnable {

	public ServerSocket serverSocket;

	public SocketAddress bindAddress;

	public boolean running = true;

	private CanaryRaspberryJuicePlugin plugin;

	public ServerListenerThread(CanaryRaspberryJuicePlugin plugin, SocketAddress bindAddress) throws IOException {
		this.plugin = plugin;
		this.bindAddress = bindAddress;
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(bindAddress);
	}

	public void run() {
		while (running) {
			try {
				Socket newConnection = serverSocket.accept();
				if (!running) return;
				plugin.handleConnection(
						RemoteSession.create(Canary.getServer(), plugin.getLogman(), newConnection));
			} catch (Exception e) {
				// if the server thread is still running raise an error
				if (running) {
					plugin.getLogman().warn("Error creating new connection");
					e.printStackTrace();
				}
			}
		}
		try {
			serverSocket.close();
		} catch (Exception e) {
			plugin.getLogman().warn("Error closing server socket");
			e.printStackTrace();
		}
	}
}
