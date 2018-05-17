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
    private static int ID = 0;

    private Player[] players;
    private String info;
    private ContestResult result;
    private ArrayList<ArrayList<String>> record;

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

                int winCnts = -1;

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
                    winCnts = -100;
                    result.stepsNum[round] = 0;
                    for ( ; num <= STEPS; num++) {
                        result.stepsNum[round] ++;
                        winCnts = board.isGeneratedWinner();
                        if (winCnts != -100) {   // a player won
                            result.scores[black][round] = winCnts > 0 ? winCnts : 0;
                            result.scores[white][round] = winCnts < 0 ? -winCnts : 0;
                            MainFrame.instance().log("Win more chessman is" + (winCnts>0?" Black":" White"));
                            break;
                        }
                        System.out.println("NUM = " + num);

                        long stepstart = System.nanoTime();
                        // receive black player step
                        if((recvBuffer=receiveMsg(black, round)) == null){
                        	System.out.println("173 break flag");
                            break;
                        }
                        result.timecost[black][round] += System.nanoTime() - stepstart;
                        players[black].addCostTime(System.nanoTime() - stepstart);

                        // test and verify the black step
                        String blackStep = new String(recvBuffer);
                        System.out.println("248 board.step");
                        // 向黑棋方返回的消息字符串
                        String blackReturnCode = board.step(blackStep, num,0);

                        board.printChessBoard();
                        if (blackReturnCode.charAt(1) == 'Y') {
                            // valid step
                            if(blackReturnCode.charAt(2) != 'N')
                                record.get(round).add("BLACK: VALID_STEP " + blackStep.substring(2, 6));
                            else
                                record.get(round).add("BLACK: VALID_STEP " + "-1-1");
                        }
                        else{
                            // invalid step
                            result.invalidSteps[black][round] ++;
                            if(blackReturnCode.charAt(2) != 'N')
                                record.get(round).add("BLACK: INVALID_STEP " + blackStep.substring(2, 6)
                                    + " REAL_STEP " + blackReturnCode.substring(3,7));
                            else
                                record.get(round).add("BLACK: INVALID_STEP " + blackStep.substring(2, 6)
                                        + " REAL_STEP " + "-1-1");
                        }
                        record.get(round).add(board.toStringToDisplay());
                        if(sendMsg(black , round , blackReturnCode) == false)
                            return;
                        if(sendMsg(white , round , blackReturnCode) == false){
                            return;
                        }
                        
                        System.out.println("307 baord.isGeneratedWinner()");

                        winCnts = board.isGeneratedWinner();
                        if (winCnts != -100) { // a player won
                            result.scores[black][round] = winCnts > 0 ? winCnts : 0;
                            result.scores[white][round] = winCnts <0 ? -winCnts : 0;
                            MainFrame.instance().log("Win more chessman is" + (winCnts>0?" Black":" White"));
                            break;
                        }

                        stepstart = System.nanoTime();
                        // receive white step
                        if((recvBuffer = receiveMsg(white , round)) == null){
                        	System.out.println("break flag");
                            break;
                        }

                        result.timecost[white][round] += System.nanoTime() - stepstart;
                        // test and verify the white step
                        String whiteStep = new String(recvBuffer);
                        System.out.println("393 board.step");
                        String whiteReturnCode = board.step(whiteStep, num,1);
                        board.printChessBoard();
                        // System.out.println("WHITE STEP: " + whiteStep);

                        if (whiteReturnCode.charAt(1) == 'Y') {
                            // valid step
                            if(whiteReturnCode.charAt(2) != 'N')
                                record.get(round).add("WHITE: VALID_STEP " + whiteStep.substring(2, 6));
                            else
                                record.get(round).add("WHITE: VALID_STEP " + "-1-1");
                        }
                        else{
                            // invalid step
                            result.invalidSteps[white][round] ++;
                            if(whiteReturnCode.charAt(2) != 'N')
                                record.get(round).add("WHITE: INVALID_STEP " + whiteStep.substring(2, 6)
                                    + " REAL_STEP " + whiteReturnCode.substring(3, 7));
                            else
                                record.get(round).add("WHITE: INVALID_STEP " + whiteStep.substring(2, 6)
                                        + " REAL_STEP " + "-1-1");
                        }
                        record.get(round).add(board.toStringToDisplay());
                        if(sendMsg(black , round , whiteReturnCode) == false)
                            return;
                        if(sendMsg(white , round , whiteReturnCode) == false){
                            return;
                        }
                        winCnts = board.isGeneratedWinner();
                        if (winCnts != -100) { // a player won
                            result.scores[black][round] = winCnts > 0 ? winCnts : 0;
                            result.scores[white][round] = winCnts < 0 ? -winCnts : 0;
                            MainFrame.instance().log("Win more chessman is" + (winCnts>0?" Black":" White"));
                            break;
                        }

                    }
                    //}
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



                    // record result
                    result.stepsNum[round] = num;
                    if (winCnts == -100)
                        winCnts = board.isGeneratedWinner();
                    result.scores[black][round] = winCnts > 0 ? winCnts : 0;
                    result.scores[white][round] = winCnts <0 ? -winCnts : 0;

                    String winnerStr = "BLACK AND WHITE";
                    if(winCnts > 0){
                        winnerStr = "BLACK";
                    }else if(winCnts < 0){
                        winnerStr = "WHITE";
                    }
                    MainFrame.instance().log(this.info + " round " + round + " end \n Win more chessman is: " + winnerStr);

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
//        MainFrame.instance().updateContestResultUI(result);
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

    // 对颜色为color方，在第round轮，请求接收对面的消息
    public byte[] receiveMsg(int color , int round) throws Exception{
        byte[] recvBuffer = null;
        try {
            // block...
            recvBuffer = players[color].receive();
        } catch (SocketTimeoutException e) {
            // step timeout
            result.errors[color][round]++;
            LOG.error(e);
            LOG.error(this.info + " ROUND " + round + " TimeoutException when receive " +
                    (color == 0?"BLACK":"WHITE") + " step: " + result.errors[color][round] + " time!");
            record.get(round).add("TIMEOUT " + (color == 0?"BLACK ":"WHITE ") + result.errors[color][round]);
            while (result.errors[color][round] <= ERRORS) {
                try {
                    recvBuffer = players[color].receive();
                } catch (SocketTimeoutException ee) {
                    result.errors[color][round]++;
                    LOG.error(e);
                    LOG.error(this.info + " ROUND " + round + " TimeoutException when receive" +
                            (color == 0?"BLACK":"WHITE") + "step: " + result.errors[color][round] + " time!");
                    record.get(round).add("TIMEOUT " + (color == 0?"BLACK ":"WHITE ") + result.errors[color][round]);
                    continue;
                }
                break;
            }
            if (result.errors[color][round] > ERRORS) {
                record.get(round).add("ERROR_MAXTIME " + (color == 0?"BLACK ":"WHITE "));
                return null;
            }
        } catch (Exception e) {
            // other exception
            LOG.error(e);
            LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive "
                    + (color == 0?"BLACK ":"WHITE ") + " step!");
            record.get(round).add("UNKOWN_EXCEPTION " + (color == 0?"BLACK ":"WHITE "));
            result.errors[color][round]++;
            result.winner = 1-color;
            return null;
        }
        return recvBuffer;
    }

    public boolean sendMsg(int color , int round , String msg){
        try {
            players[color].send(msg);
        } catch (Exception e) {
            LOG.error(e);
            record.get(round).add("SEND_ERROR " + (color == 0? "BLACK":"WHITE"));
            result.errors[color][round]++;
            result.winner = 1-color;
            return false;
        }
        return true;
    }

}
