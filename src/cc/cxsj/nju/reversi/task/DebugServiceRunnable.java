package cc.cxsj.nju.reversi.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.cxsj.nju.reversi.ai.RobotAIModel;
import cc.cxsj.nju.reversi.chess.ChessBoard;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.info.ContestResult;
import cc.cxsj.nju.reversi.info.ContestResults;
import cc.cxsj.nju.reversi.info.Player;
import cc.cxsj.nju.reversi.info.RobotPlayerAdapter;
import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

public class DebugServiceRunnable implements Runnable{

	private static final Logger LOG = Logger.getLogger(DebugServiceRunnable.class);
	private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
	private static final int STEPS = Integer.valueOf(ServerProperties.instance().getProperty("round.steps"));
	private static final int ERRORS = Integer.valueOf(ServerProperties.instance().getProperty("step.error.number"));
	private static final RobotAIModel ROBOT_MODELA =
            RobotAIModel.values()[Integer.valueOf(ServerProperties.instance().getProperty("debug.robot.a"))];
    private static final RobotAIModel ROBOT_MODELB =
            RobotAIModel.values()[Integer.valueOf(ServerProperties.instance().getProperty("debug.robot.b"))];
	private static final int DIS_FREQ = Integer.valueOf(ServerProperties.instance().getProperty("disappear_freq"));
    private static int ID = 0;
	
	private Player[] players;
	private String info;
	private ContestResult result;
	private ArrayList<ArrayList<String>> record;
	private Queue<String> blackMoves, whiteMoves;
	
	public DebugServiceRunnable() {
		
		this.players = new Player[2];
		players[0] = new RobotPlayerAdapter(ROBOT_MODELA.toString(), ROBOT_MODELA.toString(), ROBOT_MODELA);
		players[1] = new RobotPlayerAdapter(ROBOT_MODELB.toString(), ROBOT_MODELB.toString(), ROBOT_MODELB);
        MainFrame.instance().setPlayerId(players[0].getId(), players[1].getId());
		this.info = players[0].getId() + "vs" + players[1].getId() + "-debug-" + ID;
		
		this.result = new ContestResult(ID, players[0], players[1]);
		
		this.record = new ArrayList<ArrayList<String>>();
		for (int r = 0; r < ROUNDS; r++) {
			this.record.add(new ArrayList<String>());
			record.get(r).add("DEBUG " + ID);
			record.get(r).add("INFO " + players[0].getId() + " VS " + players[1].getId());
		}
		ID++;
	}

	@Override
	public void run() {
	
		LOG.info(this.info + " begin!");
		MainFrame.instance().log(this.info + " begin!");
	
		byte[] recvBuffer = null;
		
		try {
			for (int round = 0; round < ROUNDS; round++) {
				
				record.get(round).add("ROUND_START " + round);
				LOG.info(this.info + " round " + round + " start");
				MainFrame.instance().log(this.info + " round " + round + " start");
				
				// assign color piece
				int black = round & 0x1, white = 1 - black;
				int num = 1;
				record.get(round).add("COLOR BLACK:P" + black + " WHITE:P" + white);
				
				// generate empty chess board
				ChessBoard board = new ChessBoard();
				board.generateEmptyChessBoard();
				blackMoves = new LinkedList<String>();
				whiteMoves = new LinkedList<String>();
				// record.get(round).add("INITIAL CHESS BOARD\n" + board.toStringToRecord());
				
				try {
					
					players[black].send("BB");
					players[white].send("BW");
					
					// begin palying chess
					for ( ; num <= STEPS && board.isGeneratedWinnner() < 0; num++) {
						if (num != 1 && (num-1) % DIS_FREQ == 0) {
						    String blackdisappear = blackMoves.poll();
						    String disappearedCode = "R0D" + blackdisappear + "0";
                            players[black].send(disappearedCode);
                            players[white].send(disappearedCode);
                            board.step("SD" + blackdisappear, num, 0);
                        }
                        long stepstart = System.nanoTime();
					    // receive black player step
						try {
							// block...
							recvBuffer = players[black].receive();
						} catch (SocketTimeoutException e) {
							// step timeout
							result.errors[black][round]++;
							e.printStackTrace();
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: "
                                    + result.errors[black][round] + " time!");
							record.get(round).add("TIMEOUT BLACK " + result.errors[black][round]);
							while (result.errors[black][round] <= ERRORS) {   // 再次接收
								try {
									recvBuffer = players[black].receive();
								} catch (SocketTimeoutException ee) {
									result.errors[black][round]++;
									LOG.error(e);
									LOG.error(this.info + " ROUND "
                                            + round + " TimeoutException when receive BLACK step: " + result.errors[black][round] + " time!");
									record.get(round).add("TIMEOUT BLACK " + result.errors[black][round]);
									continue;
								}
								break;
							}
							if (result.errors[black][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME BLACK");
								break;
							}
						} catch (Exception e) {
							// other exception
							e.printStackTrace();
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive RED step!");
							record.get(round).add("UNKOWN_EXCEPTION RED");
							break;
						}
						result.timecost[black][round] += System.nanoTime() - stepstart;
						// test and verify the black step
						String blackStep = new String(recvBuffer);
						String blackReturnCode = board.step(blackStep, num, 0);
                        blackMoves.offer(blackStep.substring(2));
                        System.out.println("black Step: " + blackStep);
						if (blackReturnCode.charAt(1) == '0') {
							// valid step
							record.get(round).add("VALID_STEP BLACK " + blackStep.substring(0, 6));
							record.get(round).add(board.toStringToDisplay());
							players[black].send(blackReturnCode);
							players[white].send(blackReturnCode);
						} else {
							// invalid step
							result.errors[black][round]++;
							record.get(round).add("ERROR_STEP BLACK " + result.errors[black][round] + " " + blackStep.substring(0, 6));
							players[black].send(blackReturnCode);
							players[white].send("R0N");
							if (result.errors[black][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME BLACK");
								break;
							}
						}

						if (board.isGeneratedWinnner() >= 0)
                            break;

                        if (num != 1 && (num-1) % DIS_FREQ == 0) {
                            String whitedisappear = whiteMoves.poll();
                            String disappearedCode = "R0D" + whitedisappear + "1";
                            players[white].send(disappearedCode);
                            players[black].send(disappearedCode);
                            board.step("SD" + whitedisappear, num,1);
                        }
                        stepstart = System.nanoTime();
                        // receive white step
						try {
							// block...
							recvBuffer = players[white].receive();
						} catch (SocketTimeoutException e) {
							// step timeout
							result.errors[white][round]++;
							LOG.error(e);
                            e.printStackTrace();
							LOG.error(this.info + " ROUND " + round + " TimeoutException when receive WHITE step: "
                                    + result.errors[white][round] + " time!");
							record.get(round).add("TIMEOUT WHITE " + result.errors[white][round]);
							while (result.errors[white][round] <= ERRORS) {
								try {
									recvBuffer = players[white].receive();
								} catch (SocketTimeoutException ee) {
									result.errors[white][round]++;
									LOG.error(e);
									LOG.error(this.info + " ROUND " + round + " TimeoutException when receive WHITE step: "
                                            + result.errors[white][round] + " time!");
									record.get(round).add("TIMEOUT WHITE " + result.errors[white][round]);
									continue;
								}
								break;
							}
							if (result.errors[white][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME WHITE");
								break;
							}
						} catch (Exception e) {
							// other exception
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive WHITE step!");
							record.get(round).add("UNKOWN_EXCEPTION WHITE");
							break;
						}
						result.timecost[white][round] += System.nanoTime() - stepstart;
						// test and verify the white step
						String whiteStep = new String(recvBuffer);
						String whiteReturnCode = board.step(whiteStep, num, 1);
						System.out.println("black Step: " + blackStep);
						whiteMoves.offer(whiteStep.substring(2));
						if (whiteReturnCode.charAt(1) == '0') {
							// valid step
							record.get(round).add("VALID_STEP WHITE " + whiteStep.substring(0, 6));
							record.get(round).add(board.toStringToDisplay());
							players[white].send(whiteReturnCode);
							players[black].send(whiteReturnCode);
						} else {
							// invalid step
							result.errors[white][round]++;
							record.get(round).add("ERROR_STEP WHITE " + result.errors[white][round] + " " + whiteStep.substring(0, 6));
							players[white].send(whiteReturnCode);
							players[black].send("R0N");
							if (result.errors[white][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME WHITE");
								break;
							}
						}
					}
				} catch (Exception e) {
					// round end abnormally
					e.printStackTrace();
					LOG.error(e);
					LOG.error(this.info + " ROUND " + round + " Unkown Exception in " + round + " CONTEST");
					record.get(round).add("ROUND_ERROR " + round);
				} finally {
					// notify players that this round is over and record this round result
					players[0].send("E1");
					players[1].send("E1");
					LOG.info(this.info + " round " + round + " end");
					MainFrame.instance().log(this.info + " round " + round + " end");
					
					// record result
					result.stepsNum[round] = num;
					int winner = board.isGeneratedWinnner();
					result.scores[black][round] = winner == 0 ? 1 : 0;
					result.scores[white][round] = winner == 1 ? 1 : 0;
					
					record.get(round).add("ROUND_END " + round);
				}
			}
			// notify players that contest is over and save contest result
			players[0].send("E0");
			players[1].send("E0");
		} catch (Exception e) {
            e.printStackTrace();
			LOG.error(e);
			LOG.error(this.info + " Unkown Exception");
		} finally {
			LOG.info(this.info + " game over");
			MainFrame.instance().log(this.info + " game over");
			
			// save result
			result.evaluate();
			ContestResults.addContestResult(result);
			saveResult();	
			LOG.info(this.info + " result save completed!");
			MainFrame.instance().log(this.info + " result save completed!");
			
			// store record into file
			saveRecord();
			LOG.info(this.info + " record save completed!");
			MainFrame.instance().log(this.info + " record save completed!");
			
			// release
			players[0].clear();
			players[1].clear();
		}
		
	}
	
	private void saveRecord() {	
		String RECORD_DIR = ServerProperties.instance().getProperty("current.record.dir");
		File dir = new File(System.getProperty("user.dir") + "/record");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(System.getProperty("user.dir") + "/record/debug");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(System.getProperty("user.dir") + "/record/debug/" + RECORD_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		for (int r = 0; r < ROUNDS; r++) {
			PrintWriter out = null;
			try {
				File file = new File(System.getProperty("user.dir") + "/record/debug/" + RECORD_DIR
                        + "/" + this.info + "-round-" + r + ".record");
				if (!file.exists()) {
					file.createNewFile();
				} else {
					file.delete();
					file.createNewFile();
				}
				out = new PrintWriter(file);
				ArrayList<String> rec = record.get(r);
				for (int i = 0; i < rec.size(); i++) {
					out.println(rec.get(i));
				}
				out.flush();
			} catch (FileNotFoundException e) {
				LOG.error(e);
			} catch (IOException e) {
				LOG.error(e);
			} finally {
				record.get(r).clear();
				out.close();
			}
		}	
	}
	
	public void saveResult() {
		String RESULT_DIR = ServerProperties.instance().getProperty("current.result.dir");
		PrintWriter out = null;
		try {
			File dir = new File(System.getProperty("user.dir") + "/result");
			if (!dir.exists()) {
				dir.mkdir();
			}
			dir = new File(System.getProperty("user.dir") + "/result/debug");
			if (!dir.exists()) {
				dir.mkdir();
			}
			dir = new File(System.getProperty("user.dir") + "/result/debug/" + RESULT_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(System.getProperty("user.dir") + "/result/debug/" + RESULT_DIR + "/" + this.info + ".result");
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			out = new PrintWriter(file);
			out.println(result);
			out.flush();
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			result = null;
			out.close();
		}
	}

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.execute(new DebugServiceRunnable()); 
	}
}
