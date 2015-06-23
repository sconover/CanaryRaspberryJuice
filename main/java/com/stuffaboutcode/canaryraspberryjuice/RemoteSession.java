package com.stuffaboutcode.canaryraspberryjuice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayDeque;
import net.canarymod.api.Server;
import net.canarymod.api.world.blocks.BlockFace;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.ChatHook;
import net.canarymod.logger.Logman;

// Remote session class manages commands
public class RemoteSession {

	private final CommandHandler commandHandler;

	private Logman logman;
	private Socket socket;

	private BufferedReader in;

	private BufferedWriter out;
	
	private Thread inThread;
	
	private Thread outThread;

	private ArrayDeque<String> inQueue = new ArrayDeque<String>();

	private ArrayDeque<String> outQueue = new ArrayDeque<String>();

	public boolean running = true;

	private int maxCommandsPerTick = 9000;

	private boolean closed = false;
	
	private final ToOutQueue toOutQueue;

	public RemoteSession(Server server, Logman logman, Socket socket) throws IOException {
		this.logman = logman;
		this.toOutQueue = new ToOutQueue(outQueue);
		this.commandHandler = new CommandHandler(server, new ServerWrapper(server), logman, toOutQueue);
		this.socket = socket;
		init();
	}

	public void init() throws IOException {
		socket.setTcpNoDelay(true);
		socket.setKeepAlive(true);
		socket.setTrafficClass(0x10);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		startThreads();
		logman.info("Opened connection to" + socket.getRemoteSocketAddress() + ".");
	}

	protected void startThreads() {
		inThread = new Thread(new InputThread());
		inThread.start();
		outThread = new Thread(new OutputThread());
		outThread.start();
	}

 	public Socket getSocket() {
		return socket;
	}

	/** called from the server main thread */
	public void tick() {
		int processedCount = 0;
		String message;
		while ((message = inQueue.poll()) != null) {
			commandHandler.handleLine(message);
			processedCount++;
			if (processedCount >= maxCommandsPerTick) {
				logman.warn("Over " + maxCommandsPerTick +
						" commands were queued - deferring " + inQueue.size() + " to next tick");
				break;
			}
		}

		if (!running && inQueue.size() <= 0) {
			this.toOutQueue.setPendingRemoval();
		}
	}

	public void close() {
		if (closed) return;
		running = false;
		this.toOutQueue.setPendingRemoval();
		//wait for threads to stop
		try {
			inThread.join(2000);
			outThread.join(2000);
		} catch (InterruptedException e) {
			logman.warn("Failed to stop in/out thread");
			e.printStackTrace();
		}
		
		//close socket
		try {
			socket.close();
		} catch (Exception e) {
			logman.warn("Failed to close socket");
			e.printStackTrace();
		}

		closed = true;
		logman.info("Closed connection to" + socket.getRemoteSocketAddress() + ".");
	}

	public void kick(String reason) {
		try {
			out.write(reason);
			out.flush();
		} catch (Exception e) {
		}
		close();
	}

	/** socket listening thread */
	private class InputThread implements Runnable {
		public void run() {
			logman.info("Starting input thread");
			while (running) {
				try {
					String newLine = in.readLine();
					//System.out.println(newLine);
					if (newLine == null) {
						running = false;
					} else {
						inQueue.add(newLine);
						//System.out.println("Added to in queue");
					}
				} catch (Exception e) {
					// if its running raise an error
					if (running) {
						e.printStackTrace();
						running = false;
					}
				}
			}
			//close in buffer
			try {
				in.close();
			} catch (Exception e) {
				logman.warn("Failed to close in buffer");
				e.printStackTrace();
			}
		}
	}

	private class OutputThread implements Runnable {
		public void run() {
			logman.info("Starting output thread");
			while (running) {
				try {
					String line;
					while((line = outQueue.poll()) != null) {
						out.write(line);
						out.write('\n');
					}
					out.flush();
					Thread.yield();
					Thread.sleep(1L);
				} catch (Exception e) {
					// if its still running raise an error
					if (running) {
						e.printStackTrace();
						running = false;
					}
				}
			}
			//close out buffer
			try {
				out.close();
			} catch (Exception e) {
				logman.warn("Failed to close out buffer");
				e.printStackTrace();
			}
		}
	}

	// turn block faces to numbers
	public static int blockFaceToNotch(BlockFace face) {
		switch (face) {
		case BOTTOM:
			return 0;
		case TOP:
			return 1;
		case NORTH:
			return 2;
		case SOUTH:
			return 3;
		case WEST:
			return 4;
		case EAST:
			return 5;
		default:
			return 7; // Good as anything here, but technically invalid
		}
	}

	// add a block hit to the queue to be processed
	public void queueBlockHit(BlockRightClickHook hitHook) {
		commandHandler.queueBlockHit(hitHook);
	}

	// add a chat posted to the queue to be processed
	public void queueChatPost(ChatHook chatHook) {
		commandHandler.queueChatPost(chatHook);
	}

	public boolean isPendingRemoval() {
		return toOutQueue.isPendingRemoval();
	}

	public interface Out {
		public void send(String str);
	}

	public static class ToOutQueue implements Out {
		private final ArrayDeque<String> outQueue;
		private boolean pendingRemoval = false;

		public ToOutQueue(ArrayDeque<String> outQueue) {
			this.outQueue = outQueue;
			pendingRemoval = false;
		}

		public void setPendingRemoval() {
			pendingRemoval = true;
		}

		public boolean isPendingRemoval() {
			return pendingRemoval;
		}

		@Override public void send(String str) {
			if (pendingRemoval) return;
			synchronized(outQueue) {
				outQueue.add(str);
			}
		}
	}
}
