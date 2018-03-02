package cc.cxsj.nju.reversi.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.ui.MainFrame;

public class Players {

	private static final Logger LOG = Logger.getLogger(Main.class);

	private static HashMap<String, Player> playersInfo = new HashMap<String, Player>();

	public static void loadPlayers() {
		File dir = new File(System.getProperty("user.dir") + "/players");
		System.out.println(dir.getPath());
		if (!dir.exists()) {    // 判断文件是否存在
 			MainFrame.instance().log("No found players dir!");
			LOG.error("No found players dir!");
			System.exit(0);
		}
		File[] files = dir.listFiles(new FilenameFilter() {    // 得到固定文件名格式的文件列表
			Pattern pattern = Pattern.compile("players\\-[0-9]+\\.txt");

			@Override
			public boolean accept(File dir, String name) {   // 复写accept方法
				Matcher matcher = pattern.matcher(name);
				return matcher.matches();
			}
		});
		if (files.length == 0) {    // 如果没有符合条件的文件
			MainFrame.instance().log("There is no players-xx.list file!");
			LOG.error("There is no players-xx.list file!");
			System.exit(0);
		}

		int currentId = 0, index = 0;
		for (int i = 0; i < files.length; i++) {
			int id = Integer
					.valueOf(files[i].getName().substring(files[i].getName().indexOf('-') + 1, files[i].getName().indexOf('.')));
			if (id > currentId) {
				currentId = id;
				index = i;
			}
		}
		File file = files[index];
		int nextId = currentId + 1;
		ServerProperties.instance().setProperty("next.players.file", "players-" + nextId + ".txt");
		ServerProperties.instance().setProperty("current.record.dir", "record-" + currentId);
		ServerProperties.instance().setProperty("current.result.dir", "result-" + currentId);
		MainFrame.instance().log("Load list file: " + file.getName());
		LOG.info("Load list file: " + file.getName());
		try {

			BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				String s;
				LOG.info("The players authoried in this round as follow:");
				MainFrame.instance().log("The players authoried in this round as follow:");
				while ((s = in.readLine()) != null) {
					if (s.equals(""))
						continue;
					String[] info = s.split(",");
					if (info.length == 3) {
						playersInfo.put(info[0].trim(), new Player(info[0].trim(), info[1].trim(), info[2].trim()));
						LOG.info(info[0].trim());
						MainFrame.instance().log(info[0].trim() + "," + info[2].trim());
					}
					else if (info.length == 2) {
                        playersInfo.put(info[0].trim(), new Player(info[0].trim(), info[1].trim(), null));
                        LOG.info(info[0].trim());
                        MainFrame.instance().log(info[0].trim());
                    }
				}
				LOG.info("Total players: " + playersInfo.size());
				MainFrame.instance().log("Total: " + playersInfo.size());
			} finally {
				in.close();
			}
		} catch (IOException e) {
			LOG.error(e);
			System.exit(0);
		}
	}

	// 生成本轮结束后晋级的选手，list编号+1
	public static void GenerateNextPlayersNameListFile() {
		String path = System.getProperty("user.dir") + "/players/"
				+ ServerProperties.instance().getProperty("next.players.file");

		// contain the winner
		ArrayList<Player> nextPlayers = new ArrayList<Player>();
		Set<String> noContestPlayersSet = playersInfo.keySet();
		LOG.error("The winners as follow:");
		MainFrame.instance().log("The winners as follow:");
		for (Integer id : ContestResults.getContestIdsOrderly()) {
			ContestResult result = ContestResults.getContestResult(id);
			switch (result.winner) {
				case 0:   // A赢
					nextPlayers.add(result.players[0]);
					LOG.error(result.players[0].getId());
					MainFrame.instance().log(result.players[0].getId() + "," + result.players[0].getName());
					break;
				case 1:   // B赢
					nextPlayers.add(result.players[1]);
					LOG.error(result.players[1].getId());
					MainFrame.instance().log(result.players[1].getId() + "," + result.players[1].getName());
					break;
				case 2:   // 平局
					nextPlayers.add(result.players[0]);
					nextPlayers.add(result.players[1]);
					LOG.error(result.players[0].getId());
					MainFrame.instance().log(result.players[0].getId() + "," + result.players[0].getName());
					LOG.error(result.players[1].getId());
					MainFrame.instance().log(result.players[1].getId() + "," + result.players[1].getName());
					break;
				default:
					break;
				}

			noContestPlayersSet.remove(result.players[0].getId());
			noContestPlayersSet.remove(result.players[1].getId());
		}
		LOG.error("Total winners: " + nextPlayers.size());
		MainFrame.instance().log("Total winners: " + nextPlayers.size());

		// the players not to contest
		if (noContestPlayersSet.size() != 0) {
			LOG.error("The passed players as follow:");
			MainFrame.instance().log("The passed players as follow:");
			for (String id : noContestPlayersSet) {
				LOG.error(id);
				MainFrame.instance().log(id);
				nextPlayers.add(playersInfo.get(id));
			}
			LOG.error("Total passed players: " + noContestPlayersSet.size());
			MainFrame.instance().log("Total passed players: " + noContestPlayersSet.size());
		}

		nextPlayers.sort(new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		LOG.error("Total promotion palyers: " + nextPlayers.size());
		MainFrame.instance().log("Total promotion palyers: " + nextPlayers.size());

		PrintWriter out = null;
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			out = new PrintWriter(file);
			for (Player player : nextPlayers) {
			    if (!player.getName().equalsIgnoreCase(""))
				    out.println(player.getId() + "," + player.getPassword() + "," + player.getName());
			    else
                    out.println(player.getId() + "," + player.getPassword());
			}
			out.flush();
			LOG.error("Export promotion palyers sucess!");
			MainFrame.instance().log("Export promotion palyers sucess!");
		} catch (FileNotFoundException e) {
			LOG.error(e);
			MainFrame.instance().log(e.toString());
		} catch (IOException e) {
			LOG.error(e);
			MainFrame.instance().log(e.toString());
		} finally {
			out.close();
		}
	}

	public static Player getPlayer(String id) {
		if (id == null)
			return null;
		synchronized (playersInfo) {
			// System.out.println(playersInfo.get(id));
			return playersInfo.get(id);
		}
	}

	public static int getPlayersNum() {
		synchronized (playersInfo) {
			return playersInfo.size();
		}
	}

	public static boolean isContainedPlayer(String id) {
		synchronized (playersInfo) {
			return playersInfo.containsKey(id);
		}
	}

    public static HashMap<String, Player> getPlayersInfo() {
        return playersInfo;
    }
}
