package cc.cxsj.nju.reversi.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.cxsj.nju.reversi.config.ServerProperties;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.ui.MainFrame;

/**
 * store contest mode result defaultly
 * 
 * @author coldcode
 * modifier: whatbeg
 *
 */
public class ContestResults {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final Integer MODE = Integer.valueOf(ServerProperties.instance().getProperty("server.mode"));
    private static final String sep = File.separator;

	public static HashMap<Integer, ContestResult> contestResults = new HashMap<Integer, ContestResult>();

	public static synchronized void addContestResult(ContestResult result) {
		contestResults.put(result.id, result);
	}
	
	public static synchronized ContestResult getContestResult(Integer id) {
		return contestResults.get(id);
	}
	
	public static synchronized ArrayList<Integer> getContestIdsOrderly() {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(contestResults.keySet());
		return temp;
	}

	public static synchronized int getContestResultNum() {
		return contestResults.size();
	}
	
	public static synchronized void clear() {
		contestResults.clear();
	}
	
	public static void loadContestResults(int mode) {
		String RESULT_DIR = ServerProperties.instance().getProperty("current.result.dir");
		String path;

		switch (mode) {
			case 0:
				path = System.getProperty("user.dir") + sep + "result" + sep + "contest" + sep + RESULT_DIR;
				break;
			case 1:
				path = System.getProperty("user.dir") + sep + "result" + sep + "test" + sep + RESULT_DIR;
				break;
			case 2:
				path = System.getProperty("user.dir") + sep + "result" + sep + "debug" + sep + RESULT_DIR;
				break;
			default:
				path = null;
				break;
		}
		if (path == null) {
			LOG.error("There is no dir denoted by path for server mode!");
			MainFrame.instance().log("There is no dir denoted by path for server mode!");
			return;
		}
		File dir = new File(path);
		if (!dir.exists()) {
			LOG.error("There is no dir denoted by " + path);
			MainFrame.instance().log("There is no dir denoted by path " + path);
			return;
		}
		File[] files =  dir.listFiles(new FilenameFilter() {
			Pattern pattern = Pattern.compile(".+\\.result");
			@Override
			public boolean accept(File dir, String name) {  
				Matcher matcher = pattern.matcher(name);
				return matcher.matches();
			}
		});
//		for (int i = 0; i < files.length; i++) {
//		    System.out.println(files[i].getAbsolutePath());
//        }
		HashMap<Integer, ContestResult> results = new HashMap<Integer, ContestResult>();
		for (int i = 0; i < files.length; i++) {
			ContestResult result = new ContestResult();
			try{
				BufferedReader in = new BufferedReader(new FileReader(files[i]));
				try {
					LOG.error("Load " + files[i].getName());
					MainFrame.instance().log("Load " + files[i].getName());
					String line = in.readLine();
					String[] l1 = line.split(" ");
					result.id = Integer.valueOf(l1[1]);    // CONTEST/TEST/DEBUG 0
					
					line = in.readLine();
					String[] l2 = line.split(" ");
					if (l2[1].contains("Robot")) {
						result.players[0] = new Player(l2[1], l2[1], l2[1]);
					} else {
						result.players[0] = Players.getPlayer(l2[1]);
					}
					if (l2[3].contains("Robot")) {
						result.players[1] = new Player(l2[3], l2[3], l2[3]);
					} else {
						result.players[1] = Players.getPlayer(l2[3]);
					}

					line = in.readLine();          // winner, actually line 3
					String[] l22 = line.split(" ");
					result.winner = Integer.valueOf(l22[1]);
					
					line = in.readLine();       // winscore
					String[] l3 = line.split(" ");
					result.winRound[0] = Integer.valueOf(l3[1].split(":")[0]);
					result.winRound[1] = Integer.valueOf(l3[1].split(":")[1]);
					
					line = in.readLine();       // SCORE of ROUND
					String[] l4 = line.split(" ");
					for (int j = 1; j < l4.length; j++) {
						result.scores[0][j - 1] = Integer.valueOf(l4[j].split(":")[0]);
						result.scores[1][j - 1] = Integer.valueOf(l4[j].split(":")[1]);
					}
					
					line = in.readLine();        // STEP NUMBER OF ROUND
					String[] l5 = line.split(" ");
					for (int j = 1; j < l5.length; j++) {
						result.stepsNum[j - 1] = Integer.valueOf(l5[j]);
					}
					
					line = in.readLine();
					String[] l6 = line.split(" ");
					for (int j = 1; j < l6.length; j++) {
						result.errors[0][j - 1] = Integer.valueOf(l6[j].split(":")[0]);
						result.errors[1][j - 1] = Integer.valueOf(l6[j].split(":")[1]);
					}

					line = in.readLine();
					String [] l7 = line.split(" ");
                    for (int j = 1; j < l7.length; j++) {
                        result.timecost[0][j - 1] = Long.valueOf(l7[j].split(":")[0]);
                        result.timecost[1][j - 1] = Long.valueOf(l7[j].split(":")[1]);
                    }
					results.put(result.id, result);
				} finally {
					in.close();
				}
			} catch (Exception e){
				LOG.error(e);
				MainFrame.instance().log(e.toString());
			}
		}

		if (!results.isEmpty()) {
			contestResults.clear();
			contestResults = results;
			LOG.error("Load sucess!");
			MainFrame.instance().log("Load sucess!");
		} else {
			LOG.error("There is no result to be loaded!");
			MainFrame.instance().log("There is no result to be loaded!");
		}
	}
	
	public static synchronized void saveContestResults() {
		String RESULT_DIR = ServerProperties.instance().getProperty("current.result.dir");
		String path;
		switch (MODE) {
		case 0:
			path = System.getProperty("user.dir") + sep + "result" + sep + "contest" + sep + RESULT_DIR;
			break;
		case 1:
			path = System.getProperty("user.dir") + sep + "result" + sep + "test" + sep + RESULT_DIR;
			break;
		case 2:
			path = System.getProperty("user.dir") + sep + "result" + sep + "debug" + sep + RESULT_DIR;
			break;
		default:
			path = null;
			break;
		}
		if (path == null) {
			LOG.error("There is no dir denoted by path for server mode!");
			MainFrame.instance().log("There is no dir denoted by path for server mode!");
			return;
		}
		System.out.println("Save Results to " + path);
		PrintWriter out = null;
		try {
			File dir = new File(System.getProperty("user.dir") + sep + "result");
			if (!dir.exists()) {
				dir.mkdir();
			}
			dir = new File(path);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(path + sep + "total.result");
			System.out.println("Save total.results to " + file.getAbsolutePath());
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			out = new PrintWriter(file);
			for (Entry<Integer, ContestResult> entry : contestResults.entrySet()) {
				System.out.println("TOTAL RESULT");
				out.println(entry.getValue());
				out.println();
			}
			out.flush();
			LOG.error("Save sucess!");
			MainFrame.instance().log("Save sucess!");
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
}
