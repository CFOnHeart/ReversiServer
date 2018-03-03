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
import cc.cxsj.nju.reversi.info.ContestResult;
import cc.cxsj.nju.reversi.info.ContestResults;
import cc.cxsj.nju.reversi.info.Player;
import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

public class ContestServiceRunnable implements Runnable{

    private static final Logger LOG = Logger.getLogger(ContestServiceRunnable.class);
    private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
    private static final int STEPS = Integer.valueOf(ServerProperties.instance().getProperty("round.steps"));
    private static final int ERRORS = Integer.valueOf(ServerProperties.instance().getProperty("step.error.number"));
    private static final int DIS_FREQ = Integer.valueOf(ServerProperties.instance().getProperty("disappear_freq"));
    private static final boolean PRINT_ERROR = Boolean.valueOf(ServerProperties.instance().getProperty("see.error"));
    private static int ID = 0;

    private Player[] players;
    private String info;
    private ArrayList<ArrayList<String>> record;
    private ContestResult result;
    private Queue<String> blackMoves, whiteMoves;

    public ContestServiceRunnable(Player user1, Player user2) {

        this.players = new Player[2];
        players[0] = user1;
        players[1] = user2;
        MainFrame.instance().setPlayerId(players[0].getId(), players[1].getId());
        this.info = user1.getId() + "vs" + user2.getId() + "-contest-" + ID;

        this.result = new ContestResult(ID, user1, user2);

        this.record = new ArrayList<ArrayList<String>>();
        for (int r = 0; r < ROUNDS; r++) {
            this.record.add(new ArrayList<String>());
            record.get(r).add("CONTEST " + ID);
            record.get(r).add("INFO " + user1.getId() + " VS " + user2.getId());
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

                // generate chess board randomly
                ChessBoard board = new ChessBoard();
                board.generateEmptyChessBoard();
                blackMoves = new LinkedList<String>();
                whiteMoves = new LinkedList<String>();
                // record.get(round).add("INITIAL CHESS BOARD\n" + board.toStringToRecord());
                int winner = -1;

                try {
                    try {
                        if (players[black].isclosed()) {
                            System.out.println("BLACK SOCKET CLOSED");
                            record.get(round).add("BLACK SOCKET CLOSED");
                            result.errors[black][round]++;
                            result.winner = white;
                            return;
                        }
                        players[black].send("BB");
                    } catch (Exception e) {
                        if (PRINT_ERROR) e.printStackTrace();
                        LOG.error(e);
                        record.get(round).add("SEND_ERROR BLACK");
                        result.errors[black][round]++;
                        result.winner = white;
                        return;
                    }
                    try {
                        if (players[white].isclosed()) {
                            System.out.println("WHITE SOCKET CLOSED");
                            record.get(round).add("WHITE SOCKET CLOSED");
                            result.errors[white][round]++;
                            result.winner = black;
                            return;
                        }
                        players[white].send("BW");
                    } catch (Exception e) {
                        if (PRINT_ERROR) e.printStackTrace();
                        LOG.error(e);
                        record.get(round).add("SEND_ERROR WHITE");
                        result.errors[white][round]++;
                        result.winner = black;
                        return;
                    }

                    int synNum = 0;
                    try {
                        while (synNum < 5) {
                            recvBuffer = players[black].receive();
                            String syn = new String(recvBuffer);
                            if (syn.substring(0, 2).equals("BB")) {
                                break;
                            }
                            synNum++;
                            if (synNum >= 5) {
                                // syn fail too much time
                                result.errors[black][round]++;
                                LOG.error(this.info + " ROUND " + round + " Sync. Failed too much(5) times");
                                record.get(round).add("SYNTIME_EXCEED BLACK " + result.errors[0][round]);
                                result.winner = white;
                                return;
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // step timeout
                        result.errors[black][round]++;
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " TimeoutException when synchronize black round");
                        record.get(round).add("SYN_ERROR BLACK " + result.errors[black][round]);
                        result.winner = white;
                        return;
                    } catch (Exception e) {
                        // other exception
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " Unkown Exception when synchronize black round!");
                        record.get(round).add("UNKOWN_EXCEPTION BLACK");
                        result.errors[black][round]++;
                        result.winner = white;
                        return;
                    }
                    synNum = 0;
                    try {
                        while (synNum < 5) {
                            recvBuffer = players[white].receive();
                            String syn = new String(recvBuffer);
                            if (syn.substring(0, 2).equals("BW")) {
                                break;
                            }
                            synNum++;
                        }
                        if (synNum >= 5) {
                            // syn fail too much time
                            result.errors[white][round]++;
                            LOG.error(this.info + " ROUND " + round + " Sync. Failed too much(5) times");
                            record.get(round).add("SYNTIME_EXCEED WHITE " + result.errors[1][round]);
                            result.winner = black;
                            return;
                        }
                    } catch (SocketTimeoutException e) {
                        // step timeout
                        result.errors[white][round]++;
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " TimeoutException when synchronize white round ");
                        record.get(round).add("SYN_ERROR WHITE " + result.errors[white][round]);
                        result.winner = black;
                        return;
                    } catch (Exception e) {
                        // other exception
                        LOG.error(e);
                        LOG.error(this.info + " ROUND " + round + " Unkown Exception when synchronize white round!");
                        record.get(round).add("UNKOWN_EXCEPTION WHITE");
                        result.errors[white][round]++;
                        result.winner = black;
                        return;
                    }

                    // begin palying chess
                    winner = -1;
                    for ( ; num <= STEPS; num++) {
                        winner = board.isGeneratedWinner();
                        if (winner >= 0) {   // a player won
                            // MainFrame.instance().log("Winner is" + (winner==0?" Black":" White"));
                            break;
                        }
                        if (num != 1 && (num-1) % DIS_FREQ == 0) {
                            if (blackMoves.isEmpty()) {
                                winner = 1;
                                MainFrame.instance().log("Not Enough Black pieces, White Win");
                                record.get(round).add("BLACK NOT ENOUGH");
                                result.errors[black][round]++;
                                result.winner = white;
                                break;
                            }
                            String blackdisappear = blackMoves.poll();
                            String disappearedCode = "R0D" + blackdisappear + "0";
                            try {
                                // System.out.println("BLACK SEND " + disappearedCode);
                                players[black].send(disappearedCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            try {
                                // System.out.println("WHITE SEND " + disappearedCode);
                                players[white].send(disappearedCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            board.step("SD" + blackdisappear, num, 0);
                        }
                        long stepstart = System.nanoTime();
                        // receive black step
                        try {
                            // block...
                            recvBuffer = players[black].receive();
                        } catch (SocketTimeoutException e) {
                            // step timeout
                            result.errors[black][round]++;
                            LOG.error(e);
                            LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: "
                                    + result.errors[black][round] + " time!");
                            record.get(round).add("TIMEOUT BLACK " + result.errors[black][round]);
                            while (result.errors[black][round] <= ERRORS) {
                                try {
                                    recvBuffer = players[black].receive();
                                } catch (SocketTimeoutException ee) {
                                    result.errors[black][round]++;
                                    LOG.error(e);
                                    LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: "
                                            + result.errors[black][round] + " time!");
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
                            if (PRINT_ERROR) e.printStackTrace();
                            LOG.error(e);
                            LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive BLACK step!");
                            record.get(round).add("UNKOWN_EXCEPTION BLACK");
                            result.errors[black][round]++;
                            result.winner = white;
                            break;
                        }
                        result.timecost[black][round] += Long.max(0, System.nanoTime() - stepstart);
                        // test and verify the black step
                        String blackStep = new String(recvBuffer);
                        String blackReturnCode = board.step(blackStep, num,0);
                        if (!blackStep.substring(0, 2).equals("SN"))
                            blackMoves.offer(blackStep.substring(2, 6));
                        if (blackReturnCode.charAt(1) == '0') {
                            // valid step
                            record.get(round).add("VALID_STEP BLACK " + blackStep.substring(0, 6));
                            record.get(round).add(board.toStringToDisplay());
                            try {
                                players[black].send(blackReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            try {
                                if (players[white].isclosed()) {
                                    System.out.println("WHITE SOCKET CLOSED");
                                    record.get(round).add("WHITE SOCKET CLOSED");
                                    result.errors[white][round]++;
                                    result.winner = black;
                                    return;
                                }
                                players[white].send(blackReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
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
                            try {
                                if (players[black].isclosed()) {
                                    System.out.println("BLACK SOCKET CLOSED");
                                    record.get(round).add("BLACK SOCKET CLOSED");
                                    result.errors[black][round] = 1;
                                    result.winner = white;
                                    return;
                                }
                                players[black].send(blackReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            try {
                                if (players[white].isclosed()) {
                                    System.out.println("WHITE SOCKET CLOSED");
                                    record.get(round).add("WHITE SOCKET CLOSED");
                                    result.errors[white][round]++;
                                    result.winner = black;
                                    return;
                                }
                                players[white].send("R0N");
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
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

                        winner = board.isGeneratedWinner();
                        if (winner >= 0) { // a player won
                            // MainFrame.instance().log("Winner is" + (winner==0?" Black":" White"));
                            break;
                        }

                        if (num != 1 && (num-1) % DIS_FREQ == 0) {
                            if (whiteMoves.isEmpty()) {
                                winner = 0;
                                MainFrame.instance().log("Not Enough White pieces, Black Win");
                                record.get(round).add("WHITE NOT ENOUGH");
                                result.errors[white][round]++;
                                result.winner = black;
                                break;
                            }
                            String whitedisappear = whiteMoves.poll();
                            String disappearedCode = "R0D" + whitedisappear + "1";
                            try {
                                if (players[white].isclosed()) {
                                    System.out.println("WHITE SOCKET CLOSED");
                                    record.get(round).add("WHITE SOCKET CLOSED");
                                    result.errors[white][round]++;
                                    result.winner = black;
                                    return;
                                }
                                players[white].send(disappearedCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                if (players[black].isclosed()) {
                                    System.out.println("BLACK SOCKET CLOSED");
                                    record.get(round).add("BLACK SOCKET CLOSED");
                                    result.errors[black][round]++;
                                    result.winner = white;
                                    return;
                                }
                                players[black].send(disappearedCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR BLACK");
                                result.errors[black][round]++;
                                result.winner = white;
                                return;
                            }
                            board.step("SD" + whitedisappear, num,1);
                        }
                        stepstart = System.nanoTime();
                        // receive white player step
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
                        result.timecost[white][round] += Long.max(0, System.nanoTime() - stepstart);
                        // test and verify the white step
                        String whiteStep = new String(recvBuffer);
                        String whiteReturnCode = board.step(whiteStep, num,1);
                        if (!whiteStep.substring(0, 2).equals("SN"))
                            whiteMoves.offer(whiteStep.substring(2, 6));
                        if (whiteReturnCode.charAt(1) == '0') {
                            // valid step
                            record.get(round).add("VALID_STEP WHITE " + whiteStep.substring(0, 6));
                            record.get(round).add(board.toStringToDisplay());
                            try {
                                if (players[white].isclosed()) {
                                    System.out.println("WHITE SOCKET CLOSED");
                                    record.get(round).add("WHITE SOCKET CLOSED");
                                    result.errors[white][round]++;
                                    result.winner = black;
                                    return;
                                }
                                players[white].send(whiteReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                if (players[black].isclosed()) {
                                    System.out.println("BLACK SOCKET CLOSED");
                                    record.get(round).add("BLACK SOCKET CLOSED");
                                    result.errors[black][round]++;
                                    result.winner = white;
                                    return;
                                }
                                players[black].send(whiteReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
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
                                if (players[white].isclosed()) {
                                    System.out.println("WHITE SOCKET CLOSED");
                                    record.get(round).add("WHITE SOCKET CLOSED");
                                    result.errors[white][round]++;
                                    result.winner = black;
                                    return;
                                }
                                players[white].send(whiteReturnCode);
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
                                LOG.error(e);
                                record.get(round).add("SEND_ERROR WHITE");
                                result.errors[white][round]++;
                                result.winner = black;
                                return;
                            }
                            try {
                                if (players[black].isclosed()) {
                                    System.out.println("BLACK SOCKET CLOSED");
                                    record.get(round).add("BLACK SOCKET CLOSED");
                                    result.errors[black][round]++;
                                    result.winner = white;
                                    return;
                                }
                                players[black].send("R0N");
                            } catch (Exception e) {
                                if (PRINT_ERROR) e.printStackTrace();
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
                    if (PRINT_ERROR) e.printStackTrace();
                    LOG.error(e);
                    LOG.error(this.info + " ROUND " + round + " Unkown Exception in " + round + " CONTEST");
                    record.get(round).add("ROUND_ERROR " + round);
                } finally {
                    // notify players that this round is over and record this round result
                    try {
                        System.out.println("Send E1");
                        players[0].send("E1");
                        players[1].send("E1");
                    } catch (Exception e) {
                        if (PRINT_ERROR) e.printStackTrace();
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
            if (PRINT_ERROR) e.printStackTrace();
            LOG.error(e);
            LOG.error(this.info + " Unkown Exception");
        } finally {
            try {
                // notify players that contest is over and save contest result
                System.out.println("Send E0");
                players[0].send("E0");
                players[1].send("E0");
            } catch (Exception e) {
                if (PRINT_ERROR) e.printStackTrace();
                LOG.error(e);
            }

            LOG.info(this.info + " game over");
            MainFrame.instance().log(this.info + " game over");

            // save result
            result.evaluate();
            ContestResults.addContestResult(result);
            saveResult();
            LOG.info(this.info + " result saved!");
            MainFrame.instance().log(this.info + " result saved!");

            // store record into file
            saveRecord();
            LOG.info(this.info + " record save completed!");
            MainFrame.instance().log(this.info + " record saved!");

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
        dir = new File(System.getProperty("user.dir") + "/record/contest");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(System.getProperty("user.dir") + "/record/contest/" + RECORD_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (int r = 0; r < ROUNDS; r++) {
            PrintWriter out = null;
            try {
                File file = new File(System.getProperty("user.dir") + "/record/contest/" + RECORD_DIR + "/" + this.info + "-round-" + r + ".record");
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
            dir = new File(System.getProperty("user.dir") + "/result/contest");
            if (!dir.exists()) {
                dir.mkdir();
            }
            dir = new File(System.getProperty("user.dir") + "/result/contest/" + RESULT_DIR);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(System.getProperty("user.dir") + "/result/contest/" + RESULT_DIR + "/" + this.info + ".result");
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
}
