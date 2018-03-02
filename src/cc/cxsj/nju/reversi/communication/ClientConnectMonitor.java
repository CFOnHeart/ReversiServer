package cc.cxsj.nju.reversi.communication;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.task.CreateServiceRunnable;
import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

/**
 * monitor clients access
 * 
 * @author coldcode
 * modifier whatbeg
 */
public class ClientConnectMonitor extends Thread {

	private static final Logger LOG = Logger.getLogger(Main.class);
	
	private static ClientConnectMonitor instance = new ClientConnectMonitor();
	// server ip
	private String ip;
	// server port
	private int port;
	// server socket
	private ServerSocket serverSocket = null;
	
	public static ClientConnectMonitor instance() {
		return instance;
	}

	private ClientConnectMonitor() {
		this.port = Integer.valueOf(ServerProperties.instance().getProperty("server.port"));
		try {
			this.ip = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOG.error(e);
			System.exit(0);
		}
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
		    e.printStackTrace();
			LOG.error(e);
			System.exit(0);
		} catch (SecurityException e) {
			LOG.error(e);
			System.exit(0);
		} catch (IllegalArgumentException e) {
			LOG.error(e);
			System.exit(0);
		}
		
		LOG.info("Create server socket success!");
		MainFrame.instance().log("Creat server socket success!");
		LOG.info("Server address is: " + this.ip + ":" + this.port);
		MainFrame.instance().log("Server address is: " + this.ip + ":" + this.port);
		LOG.info("ClientConnectMonitor initialization complete!");
		MainFrame.instance().log("ClientConnectMonitor initialization complete!");
	}

	/**
	 * start monitor
	 */
	@Override
	public void run() {
		
		LOG.info("Start monitor...");
		MainFrame.instance().log("Start monitor...");

		try {
			while (this.serverSocket != null && !Thread.currentThread().isInterrupted()) {

				Socket socket = null;
				try {
					LOG.info("Waitting clients to access...");
					MainFrame.instance().log("Waitting clients to access...");
					socket = serverSocket.accept();
				} catch (IOException e) {
					MainFrame.instance().log(e.toString());
					LOG.error(e);
					continue;
				} catch (SecurityException e) {
                    MainFrame.instance().log(e.toString());
					LOG.error(e);
					continue;
				}
				
				if (socket == null) {
					LOG.error("Socket is null");
					MainFrame.instance().log("Socket is null");
					continue;
				}

				CreateServiceRunnable.instance().addSocket(socket);
				LOG.info("Accepted one accession: " + socket.getInetAddress().getHostAddress()
						+ ":" + socket.getPort());
				MainFrame.instance().log("Accepted one accession: " + socket.getInetAddress().getHostAddress()
						+ ":" + socket.getPort());
				// System.out.println("Next...");
			}
		} catch (Exception e) {
		    e.printStackTrace();
		    MainFrame.instance().log(e.toString());
			LOG.error(e);
		} finally {
		    if (serverSocket == null) {
		        System.out.println("ServerSocket is NULL!");
		        MainFrame.instance().log("ServerSocket is NULL!");
            }
            else {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    MainFrame.instance().log("Server Socket Close Exception");
                    LOG.error(e);
                } finally {
                    LOG.error("System will exit!");
                    System.exit(0);
                }
            }
		}
	}
}
