package cc.cxsj.nju.reversi.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.ui.MainFrame;

public class RecordResolver {

	private static final Logger LOG = Logger.getLogger(Main.class);

	public static ArrayList<ReplayStep> record = new ArrayList<ReplayStep>();

	public static boolean resolve(int mode, ContestResult result, int round) {
		String RECORD_DIR = ServerProperties.instance().getProperty("current.record.dir");
		final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
		boolean rtn = false;
		String path;
		switch (mode) {
		case 0:
			path = System.getProperty("user.dir") + "/record/contest/" + RECORD_DIR;
			break;
		case 1:
			path = System.getProperty("user.dir") + "/record/test/" + RECORD_DIR;
			break;
		case 2:
			path = System.getProperty("user.dir") + "/record/debug/" + RECORD_DIR;
			break;
		default:
			path = null;
			break;
		}
		if (path == null) {
			LOG.error("There is no dir denoted by path for server mode!");
			MainFrame.instance().log("There is no dir denoted by path for server mode!");
			return rtn;
		}
		StringBuilder sb = new StringBuilder("");
		sb.append(path);
		sb.append("/");
		sb.append(result.players[0].getId());
		sb.append("vs");
		sb.append(result.players[1].getId());
		switch (mode) {
            case 0:
                sb.append("-contest-");
                break;
            case 1:
                sb.append("-test-");
                break;
            case 2:
                sb.append("-debug-");
                break;
            default:
                LOG.error("There is no dir denoted by path for server mode!");
                MainFrame.instance().log("There is no dir denoted by path for server mode!");
                return rtn;
		}
		sb.append(result.id);
		sb.append("-round-");
		sb.append(round);
		sb.append(".record");
		File file = new File(sb.toString());
		if (!file.exists()) {
			LOG.error("There is no corresponding record file: " + sb.toString());
			MainFrame.instance().log("There is no corresponding record file " + sb.toString());
			return rtn;
		}
		try {
			// System.out.println("THis");
			ArrayList<ReplayStep> temp = new ArrayList<ReplayStep>();
			BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				for (int l = 0; l < 4; l++) {
					in.readLine();
				}
				String line;
				while ((line = in.readLine()) != null) {
					if (line.contains("VALID_STEP")) {
						ReplayStep step = new ReplayStep(line, true);
						in.readLine();   // ------
						for (int r = 0; r < ROWS; r++) {
							line = in.readLine();
							step.append(r, line);
						}
						in.readLine();   // ------
						temp.add(step);
					} else {
						ReplayStep step = new ReplayStep(line, false);
						temp.add(step);
					}
				}
			} finally {
				if (!temp.isEmpty()) {
					rtn = true;
					record.clear();
					record = temp;
					MainFrame.instance().log(file.getName().replace(".record", "") + ":" + record.size() + " steps");
				}
				in.close();
			}
		} catch (Exception e) {
            e.printStackTrace();
			LOG.error(e);
			MainFrame.instance().log(e.toString());
		}
		return rtn;
	}
}
