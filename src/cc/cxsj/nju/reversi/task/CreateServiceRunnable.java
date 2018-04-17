package cc.cxsj.nju.reversi.task;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.info.ContestResult;
import cc.cxsj.nju.reversi.info.ContestResults;
import cc.cxsj.nju.reversi.info.Player;
import cc.cxsj.nju.reversi.info.Players;
import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

public class CreateServiceRunnable extends Thread {

	private static final Logger LOG = Logger.getLogger(CreateServiceRunnable.class);
	private static final Integer MODE = Integer.valueOf(ServerProperties.instance().getProperty("server.mode"));
	private static boolean isFilterOut;
    private static final Boolean PRINT_ERROR = Boolean.valueOf(ServerProperties.instance().getProperty("see.error"));
	static CreateServiceRunnable instance = new CreateServiceRunnable();

	private HashSet<String> isContested = new HashSet<String>();

	private BlockingQueue<Socket> sockets;
	private ArrayList<Player> matchPlayerList;
	private HashMap<String, Player> matchPlayerMap;
	private ExecutorService executor;

	public static CreateServiceRunnable instance() {
		return instance;
	}

	public static void updateService() {
		instance.clear();
		LOG.info("Update CreateServiceRunnable!");
		MainFrame.instance().log("Update CreateServiceRunnable!");
		instance = new CreateServiceRunnable();
		instance.start();
	}

	public synchronized void addSocket(Socket socket) {
		if (sockets == null) {
			LOG.info("Contest Started or Contest over!");
			MainFrame.instance().log("Contest Started or Contest over!");
			try {
				socket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
			return;
		}
		try {
			this.sockets.put(socket);
		} catch (InterruptedException e) {
		    if (PRINT_ERROR) MainFrame.instance().log("InterruptedException when add socket");
			LOG.error("InterruptedException when add socket");
			System.exit(0);
		}
	}

	private CreateServiceRunnable() {
		int isFilterOut = Integer.valueOf(ServerProperties.instance().getProperty("filter.out"));
		switch (isFilterOut) {
			case 0:
				CreateServiceRunnable.isFilterOut = false;
				break;
			case 1:
				CreateServiceRunnable.isFilterOut = true;
				break;
			default:
				LOG.error("Server filter.out property is invalid");
				System.exit(0);
				break;
		}
		this.sockets = new LinkedBlockingQueue<Socket>();
		this.matchPlayerMap = new HashMap<String, Player>();
		this.matchPlayerList = new ArrayList<Player>();
		this.executor = Executors.newFixedThreadPool(Players.getPlayersNum() / 2 + 1);
		LOG.info("CreateServiceRunnable initialization complete!");
		MainFrame.instance().log("CreateServiceRunnable initialization complete!");
	}

	private void clear() {
		interrupt();
		executor.shutdownNow();
	}

	@Override
	public void run() {

		LOG.info("CreateServiceRunnable is running...");
		MainFrame.instance().log("CreateServiceRunnable is running...");

		byte[] buffer = new byte[16];

		// start different server mode according to server.mode value
		switch (MODE) {
		case 0: {
			// contest mode
			LOG.info("Start contest mode!");
			MainFrame.instance().log("Start contest mode!");

			Socket socket = null;
			Player user = null;
			BufferedInputStream bfin = null;
			try {
				// blocking to get two socket
				while (!Thread.currentThread().isInterrupted() && matchPlayerMap.size() != Players.getPlayersNum()) {

					LOG.info("The number of registered players: " + matchPlayerMap.size());
					MainFrame.instance().log("The number of registered players: " + matchPlayerMap.size());
                    if (matchPlayerMap.size() >= 0.75 * Players.getPlayersNum()) {
                        MainFrame.instance().log("Un-accessed Players, be soon:");
                        HashMap<String, Player> playerHashMap = Players.getPlayersInfo();
                        for (String key: playerHashMap.keySet()) {
                            if (!matchPlayerMap.containsKey(key)) {
                                MainFrame.instance().log(key);
                            }
                        }
                    }
					while (!Thread.currentThread().isInterrupted()) {
						try {
							// block / wait for take
							socket = this.sockets.take();
							// System.out.println("Take one socket, this.sockets remain " + this.sockets.size() + " ps");
						} catch (InterruptedException e) {
						    if (PRINT_ERROR) e.printStackTrace();
							LOG.error(e);
							break;
						}

						try {
							socket.setSoTimeout(
									Integer.valueOf(ServerProperties.instance().getProperty("step.timeout")));
						} catch (SocketException e) {
                            if (PRINT_ERROR) e.printStackTrace();
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
                                if (PRINT_ERROR) e1.printStackTrace();
								LOG.error(e1);
							}
							continue;
						}

						try {
							bfin = new BufferedInputStream(socket.getInputStream());
						} catch (IOException e) {
                            if (PRINT_ERROR) e.printStackTrace();
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
                                if (PRINT_ERROR) e1.printStackTrace();
								LOG.error(e1);
							}
							continue;
						}

						try {
							// block
							bfin.read(buffer);
						} catch (IOException e) {
                            if (PRINT_ERROR) e.printStackTrace();
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
                                if (PRINT_ERROR) e1.printStackTrace();
								LOG.error(e1);
							}
							continue;
						}

						String msg = new String(buffer);
						Arrays.fill(buffer, (byte) 0);
						if (msg.charAt(0) == 'A' && msg.length() == 16) {
							String id = msg.substring(1, 10);
							if (isFilterOut && isContested.contains(id)) {
								LOG.error("The " + id + " has participated in the contest");
								MainFrame.instance().log("The " + id + " has participated in the contest");
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
									LOG.error(e);
								}
								continue;
							}
							String password = msg.substring(10, 16);
							LOG.info("Accepted " + id);
							MainFrame.instance().log("Accepted " + id);
							if (Players.isContainedPlayer(id)) {
								user = Players.getPlayer(id);
								if (user.getPassword().equals(password)) {
									try {
										user.initial(socket, bfin);
									} catch (IOException e) {
                                        if (PRINT_ERROR) e.printStackTrace();
										LOG.error(e);
										user.clear();
										socket = null;
										bfin = null;
										continue;
									}
									isContested.add(id);
									matchPlayerMap.put(user.getId(), user); // ID : Player
                                    LOG.info("Welcome " + id + "," + user.getName());
                                    MainFrame.instance().log("Welcome " + id + "," +user.getName());
									break;     // break this sub loop
								} else {
									try {
										bfin.close();
										socket.close();
										socket = null;
										bfin = null;
									} catch (IOException e) {
                                        if (PRINT_ERROR) e.printStackTrace();
										LOG.error(e);
									}
									LOG.info("Password is wrong: " + password);
									MainFrame.instance().log("Password is wrong: " + password);
								}
							} else {
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
                                    if (PRINT_ERROR) e.printStackTrace();
									LOG.error(e);
								}
								LOG.info("Not in Player List: " + id);
								MainFrame.instance().log("Not in Player List: " + id);
							}
						} else {
							try {
								bfin.close();
								socket.close();
								socket = null;
								bfin = null;
							} catch (IOException e) {
                                if (PRINT_ERROR) e.printStackTrace();
								LOG.error(e);
							}
							LOG.info("Authorize msg format error");
							MainFrame.instance().log("Authorize msg format error");
						}
					}
				}

				synchronized (this) {
					while (!sockets.isEmpty()) {
						try {
							sockets.take().close();
						} catch (IOException e) {
                            if (PRINT_ERROR) e.printStackTrace();
							LOG.error(e);
						} catch (InterruptedException e1) {
                            if (PRINT_ERROR) e1.printStackTrace();
							LOG.error(e1);
						}
					}
					this.sockets = null;
				}

                // Ready Start Contest
				if (!Thread.currentThread().isInterrupted()) {
					LOG.info("The number of registered players: " + matchPlayerMap.size());
					for (String key : matchPlayerMap.keySet())
                        MainFrame.instance().log(key);
					MainFrame.instance().log("The number of registered players: " + matchPlayerMap.size());
                    matchPlayerList.clear();
                    // 随机匹配用户比赛
                    matchPlayerList.addAll(matchPlayerMap.values());
                    Collections.shuffle(matchPlayerList, new Random(System.currentTimeMillis()));
					// 判断数量是否偶数，奇数的时候一个人轮空
                    if (matchPlayerList.size() % 2 == 0) {
						LOG.info("No passed player in contest");
						MainFrame.instance().log("No passed player in contest");
						LOG.info("Contest start!");
						MainFrame.instance().log("Contest start!");
						for (int i = 0; i < matchPlayerList.size(); i += 2) {
							// 一个线程负责一个2人的比赛
							ContestServiceRunnable csr = new ContestServiceRunnable(matchPlayerList.get(i),
									matchPlayerList.get(i + 1));
							this.executor.execute(csr);
						}
					} else {
						Player passedPlayer = matchPlayerList.get(matchPlayerList.size() - 1);
						passedPlayer.clear();
						LOG.info("The passed player in contest: " + passedPlayer.getId());
						MainFrame.instance().log("The passed player in contest: " + passedPlayer.getId());
						LOG.info("Contest start!");
						MainFrame.instance().log("Contest start!");
						for (int i = 0; i < matchPlayerList.size() - 1; i += 2) {
							ContestServiceRunnable csr = new ContestServiceRunnable(matchPlayerList.get(i),
									matchPlayerList.get(i + 1));
							this.executor.execute(csr);
						}
					}
				}
			} catch (Exception e) {
                if (PRINT_ERROR) e.printStackTrace();
                LOG.error(e);
            } finally {
//			    if (PRINT_ERROR)
//			    MainFrame.instance().log("May Interrupted Current Thread");
//			    System.exit(0);
				try{
					while(true){
						Thread.sleep(1000);
						if(matchPlayerList.size()/2 == ContestResults.contestResults.size()){
						    Players.GenerateNextPlayersNameListFile();
						    break;
                        }
					}
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
			break;
		}
		case 1: {
			// test mode
			LOG.info("Start test mode!");
			MainFrame.instance().log("Start test mode!");

			Socket socket = null;
			Player user = null;
			BufferedInputStream bfin = null;
			try {
				while (!Thread.currentThread().isInterrupted()) {
					// blocking to get one socket
					while (!Thread.currentThread().isInterrupted()) {
						try {
							// block
							socket = this.sockets.take();
						} catch (InterruptedException e) {
							LOG.error(e);
							break;
						}

						try {
							socket.setSoTimeout(
									Integer.valueOf(ServerProperties.instance().getProperty("step.timeout")));
						} catch (SocketException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						try {
							bfin = new BufferedInputStream(socket.getInputStream());
						} catch (IOException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						try {
							// block
							bfin.read(buffer);
						} catch (IOException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						String msg = new String(buffer);
						Arrays.fill(buffer, (byte) 0);  // clear buffer
						if (msg.charAt(0) == 'A') {
							String id = msg.substring(1, 10);
							String password = msg.substring(10, 16);
							LOG.info("Accepted " + id);
							MainFrame.instance().log("Accepted " + id);
							if (Players.isContainedPlayer(id)) {
								user = Players.getPlayer(id);
								if (user.getPassword().equals(password)) {
									try {
										user.initial(socket, bfin);
									} catch (IOException e) {
										LOG.error(e);
										user.clear();
										socket = null;
										bfin = null;
										continue;
									}
									LOG.info("Welcome " + id + "," + user.getName());
									MainFrame.instance().log("Welcome " + id + "," +user.getName());
									break;
								} else {
									try {
										bfin.close();
										socket.close();
										socket = null;
										bfin = null;
									} catch (IOException e) {
										LOG.error(e);
									}
									LOG.info("Password is wrong: " + password);
									MainFrame.instance().log("Password is wrong: " + password);
								}
							} else {
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
									LOG.error(e);
								}
								LOG.info("Not in Player List" + id);
								MainFrame.instance().log("Not in Player List" + id);
							}
						} else {
							try {
								bfin.close();
								socket.close();
								socket = null;
								bfin = null;
							} catch (IOException e) {
								LOG.error(e);
							}
							LOG.info("Authorize msg format error");
							MainFrame.instance().log("Authorize msg format error");
						}
					}

					if (!Thread.currentThread().isInterrupted()) {
                        // create contest service
                        TestServiceRunnable tsr = new TestServiceRunnable(user);
                        this.executor.execute(tsr);
                    }
				}
			} finally {

			}
			break;
		}
		case 2:
			// debug mode
			LOG.info("Start debug mode!");
			MainFrame.instance().log("Start debug mode!");
			break;
		default:
			LOG.error("No this server mode!");
			System.exit(0);
			break;
		}
	}
}
