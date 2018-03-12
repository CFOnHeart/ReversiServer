package cc.cxsj.nju.reversi.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import cc.cxsj.nju.reversi.chess.ChessBoard;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.info.ContestResults;
import cc.cxsj.nju.reversi.info.Player;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.ai.RobotAIModel;
import cc.cxsj.nju.reversi.info.ContestResult;
import cc.cxsj.nju.reversi.info.RobotPlayerAdapter;
import cc.cxsj.nju.reversi.ui.MainFrame;

public class TestServiceRunnable implements Runnable{

    private static final Logger LOG = Logger.getLogger(TestServiceRunnable.class);
    private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
    private static final int STEPS = Integer.valueOf(ServerProperties.instance().getProperty("round.steps"));
    private static final int ERRORS = Integer.valueOf(ServerProperties.instance().getProperty("step.error.number"));
    private static final RobotAIModel ROBOT_MODEL = RobotAIModel.values()[Integer.valueOf(ServerProperties.instance().getProperty("robot.model"))];
    private static final int DIS_FREQ = Integer.valueOf(ServerProperties.instance().getProperty("disappear_freq"));
    private static int ID = 0;

    private Player[] players;
    private String info;
    private ContestResult result;
    private ArrayList<ArrayList<String>> record;
    private Queue<String> blackMoves, whiteMoves;

    public TestServiceRunnable(Player user) {

        this.players = new Player[2];
        players[0] = user;
        players[1] = new RobotPlayerAdapter(ROBOT_MODEL.toString(), ROBOT_MODEL.toString(), ROBOT_MODEL);
        MainFrame.instance().setPlayerId(players[0].getId(), players[1].getId());
        this.info = players[0].getId() + "vs" + players[1].getId() + "-test-" + ID;

        this.result = new ContestResult(ID, players[0], players[1]);

        this.record = new ArrayList<ArrayList<String>>();
        for (int r = 0; r < ROUNDS; r++) {
            this.record.add(new ArrayList<String>());
            record.get(r).add("TEST " + ID);
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
                String synMsg = null;
                if (black == 0) {
                    synMsg = "BB";
                } else {
                    synMsg = "BW";
                }
                int num = 1;
                record.get(round).add("COLOR BLACK:P" + black + " WHITE:P" + white);

                // generate empty chess board
                System.out.println("flag new board begin");
                ChessBoard board = new ChessBoard();
                
                System.out.println("flag new board ");
                board.generateEmptyChessBoard();
                
                System.out.println("flag new board end");
                // record.get(round).add("INITIAL CHESS BOARD\n" + board.toStringToRecord());
                blackMoves = new LinkedList<String>();
                whiteMoves = new LinkedList<String>();
                int winner = -1;

                try {

                    try {
                         System.out.println("players[black].send(BB)");
                        players[black].send("BB");
                         System.out.println("players[black].send(BB) end");
                    } catch (Exception e) {
                        LOG.error(e);
                        record.get(round).add("SEND_ERROR BLACK");
                        result.errors[black][round]++;
                        result.winner = white;
                        return;
                    }
                    try {
                        // System.out.println("players[" + white + "].send(BW)");
                        players[white].send("BW");
                        // System.out.println("players[white].send(BW) end");
                    } catch (Exception e) {
                        LOG.error(e);
                        record.get(round).add("SEND_ERROR WHITE");
                        result.errors[white][round]++;
                        result.winner = black;
                        return;
                    }

                    int synNum = 0;
                    try {
                        while (synNum < 5) {
                            recvBuffer = players[0].receive();
                            String syn = new String(recvBuffer);
                            // System.out.println("Syn is " + syn);
                            if (syn.substring(0, 2).equals(synMsg)) {
                                break;
                            }
                            synNum++;
                        }
                        if (synNum >= 5) {
                            // syn fail too much time
                            result.errors[0][round]++;
                            LOG.error(this.info + " ROUND " + round + " Sync. Failed too much(5) times");
                            record.get(round).add("SYNTIME_EXCEED BLACK " + result.errors[0][round]);
                            System.out.println("SYNTIME_EXCEED BLACK");
                            result.winner = 1;
                            return;
                        }
                    } catch (SocketTimeoutException e) {
                        // step timeout
                        result.errors[0][round]++;
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " TimeoutException when synchronize black round");
                        record.get(round).add("SYN_ERROR BLACK " + result.errors[0][round]);
                        result.winner = 1;
                        return;
                    } catch (Exception e) {
                        // other exception
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " Unkown Exception when synchronize black round!");
                        record.get(round).add("UNKOWN_EXCEPTION BLACK");
                        result.errors[0][round]++;
                        result.winner = 1;
                        return;
                    }

                    System.out.println("step begin");
                    // begin palying chess
                    winner = -1;
                    for ( ; num <= STEPS; num++) {
                        winner = board.isGeneratedWinner();
                        if (winner >= 0) {   // a player won
                            MainFrame.instance().log("Winner is" + (winner==0?" Black":" White"));
                            break;
                        }
                        System.out.println("NUM = " + num);

                        long stepstart = System.nanoTime();
                        // receive black player step
                        try {
                            // block...
                            recvBuffer = players[black].receive();
                        } catch (SocketTimeoutException e) {
                            // step timeout
                            result.errors[black][round]++;
                            LOG.error(e);
                            LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: " + result.errors[black][round] + " time!");
                            record.get(round).add("TIMEOUT BLACK " + result.errors[black][round]);
                            while (result.errors[black][round] <= ERRORS) {
                                try {
                                    recvBuffer = players[black].receive();
                                } catch (SocketTimeoutException ee) {
                                    result.errors[black][round]++;
                                    LOG.error(e);
                                    LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: " + result.errors[black][round] + " time!");
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
                            LOG.error(e);
                            LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive BLACK step!");
                            record.get(round).add("UNKOWN_EXCEPTION BLACK");
                            result.errors[black][round]++;
                            result.winner = white;
                            break;
                        }
                        result.timecost[black][round] += System.nanoTime() - stepstart;
                        // test and verify the black step
                        String blackStep = new String(recvBuffer);
                        System.out.println("248 board.step");
                        String blackReturnCode = board.step(blackStep, num,0);
                        // System.out.println("BLACK STEP: " + blackStep);
                        if (!blackStep.substring(0, 2).equals("SN")) {
                            blackMoves.offer(blackStep.substring(2, 6));
                            // System.out.println("offer");
                        }
                        if (blackReturnCode.charAt(1) == '0') {
                            // valid step
                            record.get(round).add("VALID_STEP BLACK " + blackStep.substring(0, 6));
                            System.out.println("258 board.toStringToDisplay");
                            record.get(round).add(board.toStringToDisplay());
                            System.out.println("260 board.toStringToDisplay over");
                            try {
                                players[black].send(blackReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            try {
                                players[white].send(blackReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                        } else {
                            // invalid step
                            result.errors[black][round]++;
                            record.get(round).add("ERROR_STEP BLACK " + result.errors[black][round] + " " + blackStep.substring(0, 6));
                            System.out.println("ERROR_STEP BLACK " + result.errors[black][round] + " " + blackStep.substring(0, 6));
                            try {
                                players[black].send(blackReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            try {
                                players[white].send("R0N");
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            if (result.errors[black][round] > ERRORS) {
                                record.get(round).add("ERROR_MAXTIME BLACK");
                                break;
                            }
                        }
                        System.out.println("307 baord.isGeneratedWinner()");
                        winner = board.isGeneratedWinner();
                        if (winner >= 0) { // a player won
                            MainFrame.instance().log("Winner is" + (winner==0?" Black":" White"));
                            break;
                        }

                        /*if (num != 1 && (num-1) % DIS_FREQ == 0) {
                            if (whiteMoves.isEmpty()) {
                                winner = 0;
                                MainFrame.instance().log("Not Enough White pieces, Black Win");
                                record.get(round).add("WHITE NOT ENOUGH");
                                result.errors[white][round]++;
                                result.winner = black;
                                break;
                            }
                            String whitedisappear = whiteMoves.poll();
                            System.out.println("WHITE_DIS: " + whitedisappear);
                            String disappearedCode = "R0D" + whitedisappear + "1";
                            try {
                                // System.out.println("WHITE SEND " + disappearedCode);
                                players[white].send(disappearedCode);
                            } catch (Exception e) {
                                e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                // System.out.println("BLACK SEND " + disappearedCode);
                                players[black].send(disappearedCode);
                            } catch (Exception e) {
                                e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            System.out.println("348 board.step");
                            board.step("SD" + whitedisappear, num,1);
                        }*/

                        stepstart = System.nanoTime();
                        // receive white step
                        try {
                            // block...
                            recvBuffer = players[white].receive();
                        } catch (SocketTimeoutException e) {
                            // step timeout
                            result.errors[white][round]++;
                            LOG.error(e);
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
                            result.errors[white][round]++;
                            result.winner = black;
                            break;
                        }
                        result.timecost[white][round] += System.nanoTime() - stepstart;
                        // test and verify the white step
                        String whiteStep = new String(recvBuffer);
                        System.out.println("393 board.step");
                        String whiteReturnCode = board.step(whiteStep, num,1);
                        // System.out.println("WHITE STEP: " + whiteStep);
                        if (!whiteStep.substring(0, 2).equals("SN"))
                            whiteMoves.offer(whiteStep.substring(2, 6));
                        if (whiteReturnCode.charAt(1) == '0') {
                            // valid step
                            record.get(round).add("VALID_STEP WHITE " + whiteStep.substring(0, 6));
                            record.get(round).add(board.toStringToDisplay());
                            try {
                                players[white].send(whiteReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                players[black].send(whiteReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                        } else {
                            // invalid step
                            result.errors[white][round]++;
                            record.get(round).add("ERROR_STEP WHITE " + result.errors[white][round] + " " + whiteStep.substring(0, 6));
                            try {
                                players[white].send(whiteReturnCode);
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                players[black].send("R0N");
                            } catch (Exception e) {
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
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
                    try {
                        System.out.println("Send E1, Round Over");
                        players[0].send("E1");
                        players[1].send("E1");
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                    LOG.info(this.info + " round " + round + " end");
                    MainFrame.instance().log(this.info + " round " + round + " end");

                    // record result
                    result.stepsNum[round] = num;
                    if (winner == -1)
                        winner = board.isGeneratedWinner();
                    result.scores[black][round] = winner == 0 ? 1 : 0;
                    result.scores[white][round] = winner == 1 ? 1 : 0;

                    record.get(round).add("ROUND_END " + round);
                }

            }

        } catch (Exception e) {
            LOG.error(e);
            LOG.error(this.info + " Unkown Exception");
        } finally {
            try {
                // notify players that contest is over and save contest result
                System.out.println("Send E0, Contest Over");
                players[0].send("E0");
                players[1].send("E0");
            } catch (Exception e) {
                LOG.error(e);
            }
            LOG.info(this.info + " game over");
            MainFrame.instance().log(this.info + " game over");

            // save result
            result.evaluate();  // evaluate who is winner
            ContestResults.addContestResult(result);  // ContestResults.put(id, result)
            saveResult();                             // save results to file, PrintWriter.println()
            LOG.info(this.info + " result save done!");
            MainFrame.instance().log(this.info + " result save done!");

            // store record into file, make one file for every round
            saveRecord();
            LOG.info(this.info + " record save done!");
            MainFrame.instance().log(this.info + " record save done!");

            // release players, game over
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
        dir = new File(System.getProperty("user.dir") + "/record/test");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(System.getProperty("user.dir") + "/record/test/" + RECORD_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (int r = 0; r < ROUNDS; r++) {
            PrintWriter out = null;
            try {
                File file = new File(System.getProperty("user.dir") + "/record/test/" + RECORD_DIR + "/" + this.info + "-round-" + r + ".record");
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
            dir = new File(System.getProperty("user.dir") + "/result/test");
            if (!dir.exists()) {
                dir.mkdir();
            }
            dir = new File(System.getProperty("user.dir") + "/result/test/" + RESULT_DIR);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(System.getProperty("user.dir") + "/result/test/" + RESULT_DIR + "/" + this.info + ".result");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                System.out.println("Delete " + file.getPath());
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

}
