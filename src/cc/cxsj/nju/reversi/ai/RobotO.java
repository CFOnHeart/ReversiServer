package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.chess.Square;
import cc.cxsj.nju.reversi.config.ServerProperties;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RobotO extends RobotAI {

    private boolean full = false;
    private Square[][] tboard;
    private int[] dx = new int[]{0, 1, 1, 1};
    private int[] dy = new int[]{1, 1, 0, -1};
    private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
    private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
    private String[] path;
    private int depth;
    private HashMap<String, Integer> SCORE;

    public RobotO(int dep) {
        SCORE = new HashMap<String, Integer>();
        SCORE.put("ONE", 10);        // 单子
        SCORE.put("TWO", 100);       // 活二，包括跳活二
        SCORE.put("THREE", 1000);    // 活三，包括跳活三
        SCORE.put("FOUR", 10000);    // 活四，冲四活三
        SCORE.put("FIVE", 100000);   // 五连
        SCORE.put("MONE", 1);        // 眠一
        SCORE.put("MTWO", 10);       // 眠二
        SCORE.put("MTHREE", 100);    // 眠三
        SCORE.put("MFOUR", 1000);    // 眠四(冲四)
        depth = dep;
    }

    @Override
    public String step() {

        boolean isThisStep = false;

        // find the first empty grid and put down
        if (!isThisStep) {
            isThisStep = SearchStep(depth);
        }

        // no step is last step
        if (!isThisStep) {
            noStep();
        }

        return thisStep;
    }

    private boolean SearchStep(int depth) {
        tboard = board;
        path = new String[depth+1];
        alphabeta(tboard, depth, -Long.valueOf("2147483648"), Long.valueOf("2147483648"), true);
        System.out.println("PATH: ");
        for (int i = 0; i < path.length; i++) {
            System.out.print(path[i] + " ");
        }
        System.out.println();
        System.out.println("Best Choice is " + path[depth]);
        thisStep = "SP" + path[depth];
        return true;
    }

    private void genTboard(List<String> node) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                tboard[i][j].color = -1;
                tboard[i][j].empty = true;
            }
        }
        updateTboard(node);
    }

    private long alphabeta(Square[][] tboard, int depth, long alpha, long beta, boolean maxPlayer) {
        // System.out.println("Alpha, Beta = " + alpha + " " + beta);
        if (depth == 0 || isEnded(tboard)) return evaluate(tboard, ownColor);
        String bestChild = "";
        // printtboardWithIndent(tboard, depth*2);
        if (maxPlayer) {
            System.out.println("maxPlayer");
            HashMap<Integer, String> childlist = generateSteps(tboard);
//            for (int i = 0; i < childlist.size(); i++) {
//                System.out.println(childlist.get(i));
//            }
            for (int i = 0; i < childlist.size(); i++) {
                // node.add(childlist.get(i) + "0");
                String stp = childlist.get(i);
                int Row = stp.charAt(1) - '0' + (stp.charAt(0) - '0')*10;
                int Col = stp.charAt(3) - '0' + (stp.charAt(2) - '0')*10;
               // System.out.println(String.format("%d %d puted black", Row, Col));
                tboard[Row][Col].empty = false;
                tboard[Row][Col].color = 0;
                // printtboardWithIndent(tboard, depth-1);
                long subalpha = alphabeta(tboard, depth-1, alpha, beta, false);
                System.out.println("subalpha = " + subalpha);
                if (subalpha > alpha) {
                    alpha = subalpha;
                    bestChild = childlist.get(i);
                }
                tboard[Row][Col].empty = true;
                tboard[Row][Col].color = -1;
                // node.remove(node.size()-1);
                if (beta <= alpha) break;    // beta cut off
            }
            path[depth] = bestChild;
            return alpha;
        }
        else {
            System.out.println("minPlayer");
            HashMap<Integer, String> childlist = generateSteps(tboard);
            for (int i = 0; i < childlist.size(); i++) {
                System.out.println(childlist.get(i));
            }
            for (int i = 0; i < childlist.size(); i++) {
                // node.add(childlist.get(i) + "1");
                String stp = childlist.get(i);
                int Row = stp.charAt(1) - '0' + (stp.charAt(0) - '0')*10;
                int Col = stp.charAt(3) - '0' + (stp.charAt(2) - '0')*10;
                // System.out.println(String.format("%d %d puted white", Row, Col));
                tboard[Row][Col].empty = false;
                tboard[Row][Col].color = 1;
                long subbeta = alphabeta(tboard, depth-1, alpha, beta, true);
                // System.out.println("subbeta = " + subbeta + " Beta = " + beta);
                if (subbeta < beta) {
                    beta = subbeta;
                    bestChild = childlist.get(i);
                    // System.out.println("bestChild = " + bestChild);
                }
                tboard[Row][Col].empty = true;
                tboard[Row][Col].color = -1;
                // node.remove(node.size()-1);
                if (beta <= alpha) break;    // alpha cut off
            }
            // System.out.println("Path[" + depth + "] = " + bestChild);
            path[depth] = bestChild;
            return beta;
        }
    }

    public boolean inBoard(int row, int col) {
        return (row >= 0 && row < ROWS && col >= 0 && col < COLS);
    }

    private void updateTboard(List<String> node) {       // 01010: put down (01,01) with color 0
        for (int i = 0; i < node.size(); i++) {          // 010120: disappear (01, 01) color 0
            String stp = node.get(i);
            int Row = stp.charAt(1) - '0' + (stp.charAt(0) - '0')*10;
            int Col = stp.charAt(3) - '0' + (stp.charAt(2) - '0')*10;
            if (stp.charAt(4) == '2') {
                tboard[Row][Col].color = -1;
                tboard[Row][Col].empty = true;
            }
            else {
                tboard[Row][Col].color = stp.charAt(4) - '0';
                tboard[Row][Col].empty = false;
            }
        }
    }

    private void restoreTboard(List<String> node) {
        for (int i = 0; i < node.size(); i++) {
            String stp = node.get(i);
            int Row = stp.charAt(1) - '0' + (stp.charAt(0) - '0')*10;
            int Col = stp.charAt(3) - '0' + (stp.charAt(2) - '0')*10;
            if (stp.charAt(4) == '2') {
                tboard[Row][Col].color = stp.charAt(5) - '0';
                tboard[Row][Col].empty = false;
            }
            else {
                tboard[Row][Col].color = -1;
                tboard[Row][Col].empty = true;
            }
        }
    }

    private boolean isEnded(Square[][] tboard) {            // 15x15x4
        // updateTboard(node);
        full = true;
        for (int sR = 0; sR < ROWS; sR++) {
            for (int sC = 0; sC < COLS; sC++) {
                if (tboard[sR][sC].empty) {
                    full = false;
                    continue;
                }
                int nowColor = tboard[sR][sC].color;
                int MaxSeq = 1;
                for (int dir = 0; dir < 4; dir++) {       // 4 directions and their opposite directions
                    int seq = 1;
                    boolean d0 = true, d1 = true;
                    for (int len = 1; len <= 4; len++) {  // walk at most 4 steps
                        int deltax = dx[dir] * len;
                        int deltay = dy[dir] * len;
                        if (d0 && inBoard(sR + deltax, sC + deltay) && board[sR + deltax][sC + deltay].color == nowColor)
                            seq++;
                        else
                            d0 = false;
                        if (d1 && inBoard(sR - deltax, sC - deltay) && board[sR - deltax][sC - deltay].color == nowColor)
                            seq++;
                        else
                            d1 = false;
                        MaxSeq = seq > MaxSeq ? seq : MaxSeq;
                    }
                }
                if (MaxSeq >= 5) {
                    // restoreTboard(node);
                    return true;
                }
            }
        }
        // restoreTboard(node);
        return full;
    }

    private LinkedList<LinkedList<Integer> > flat() {
        LinkedList<LinkedList<Integer> > linkedLists = new LinkedList<>();
        // 水平
        for (int i = 0; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int j = 0; j < COLS; j++) {
                list.add(tboard[i][j].color);
            }
            linkedLists.add(list);
        }
        // 垂直
        for (int i = 0; i < COLS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int j = 0; j < ROWS; j++) {
                list.add(tboard[j][i].color);
            }
            linkedLists.add(list);
        }
        // 对角
        int dx = 1, dy = 1;
        for (int j = COLS-1, i = 0; j >= 0; j--) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        for (int j = 0, i = 1; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        // 反对角
        dx = 1;
        dy = -1;
        for (int j = COLS-1, i = 0; j >= 0; j--) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        for (int j = 0, i = 1; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        return linkedLists;
    }

    private long evaluateColor(int color) {
        long score = 0;
        int ONE = 0, TWO = 0, THREE = 0, FOUR = 0, FIVE = 0, MONE = 0, MTWO = 0, MTHREE = 0, MFOUR = 0;
        LinkedList<LinkedList<Integer> > lines = flat();
        for (int _l = 0; _l < lines.size(); _l++) {
            LinkedList<Integer> list = lines.get(_l);
            // 成五
            for (int i = 0; i < list.size()-4; i++) {
                if (list.get(i) == color && list.get(i+1) == color &&
                        list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == color)
                    FIVE++;
            }
            // 活四
            for (int i = 1; i < list.size()-4; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    FOUR++;
            }
            // 冲四
            for (int i = 0; i < list.size()-3; i++) {
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color && (i+4 < list.size() && list.get(i+4) == -1))
                    MFOUR++;
                if ((i >= 1 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color && (i+4 == list.size() || list.get(i+4) == 1-color))
                    MFOUR++;
            }
            // 活三
            for (int i = 1; i < list.size()-3; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == -1)
                    THREE++;
            }
            for (int i = 1; i < list.size()-4; i++) {   // 跳活三
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    THREE++;
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == -1 && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    THREE++;
            }
            // 眠三
            for (int i = 0; i < list.size()-2; i++) {
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && (i+3 < list.size() && list.get(i+3) == -1))
                    MTHREE++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && (i+3 == list.size() || list.get(i+3) == 1-color))
                    MTHREE++;
            }
            for (int i = 0; i < list.size()-3; i++) {   // 跳眠三
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && list.get(i+3) == color && (i+4 < list.size() && list.get(i+4) == -1))
                    MTHREE++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == -1 && list.get(i+3) == color && (i+4 == list.size() || list.get(i+4) == 1-color))
                    MTHREE++;
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == -1 && list.get(i+3) == color && (i+4 < list.size() && list.get(i+4) == -1))
                    MTHREE++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && list.get(i+3) == color && (i+4 == list.size() || list.get(i+4) == 1-color))
                    MTHREE++;
            }
            // 活二
            for (int i = 1; i < list.size()-2; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == -1)
                    TWO++;
            }
            for (int i = 1; i < list.size()-3; i++) {   // 跳活二
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && list.get(i+3) == -1)
                    TWO++;
            }
            for (int i = 1; i < list.size()-4; i++) {   // 大跳活二
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == -1 && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    TWO++;
            }
            // 眠二
            for (int i = 0; i < list.size()-1; i++) {
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == color
                        && (i+2 < list.size() && list.get(i+2) == -1))
                    MTWO++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == color
                        && (i+2 == list.size() || list.get(i+2) == 1-color))
                    MTWO++;
            }
            for (int i = 0; i < list.size()-2; i++) {   // 跳眠二
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && (i+3 < list.size() && list.get(i+3) == -1))
                    MTWO++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == color && (i+3 == list.size() || list.get(i+3) == 1-color))
                    MTWO++;
            }
            for (int i = 0; i < list.size()-3; i++) {   // 大跳眠二
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == -1 && list.get(i+3) == color && (i+4 < list.size() && list.get(i+4) == -1))
                    MTWO++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == -1
                        && list.get(i+2) == -1 && list.get(i+3) == color && (i+4 == list.size() || list.get(i+4) == 1-color))
                    MTWO++;
            }
            // 活一
            for (int i = 1; i < list.size()-1; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == -1)
                    ONE++;
            }
            // 眠一
            for (int i = 0; i < list.size(); i++) {
                if ((i == 0 || list.get(i-1) == 1-color) && list.get(i) == color
                        && (i+1 < list.size() && list.get(i+1) == -1))
                    MONE++;
                if ((i > 0 && list.get(i-1) == -1) && list.get(i) == color
                        && (i+1 == list.size() || list.get(i+1) == 1-color))
                    MONE++;
            }
        }
        if (FIVE > 0) score = Integer.MAX_VALUE;
        else
            score = ONE * SCORE.get("ONE") + TWO * SCORE.get("TWO") + THREE * SCORE.get("THREE")
                    + FOUR * SCORE.get("FOUR") + MONE * SCORE.get("MONE") + MTWO * SCORE.get("MTWO")
                    + MTHREE * SCORE.get("MTHREE") + MFOUR * SCORE.get("MFOUR");
        return score;
    }

    private long evaluate(Square[][] tboard, int color) {    // get heuristic score of board
        // updateTboard(node);
        long self = evaluateColor(color);
        long other = evaluateColor(1-color);
        // restoreTboard(node);
        return self - other;
    }

    private HashMap<Integer, String> generateSteps(Square[][] chessBoard) {     // return neighbor-len1 and neighbor-len2 steps
        HashMap<Integer, String> candidate = new HashMap<Integer, String>();
        List<String> len2 = new LinkedList<String>();
        len2.clear();
        candidate.clear();
        int c = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!chessBoard[i][j].empty) continue;
                if (hasNeighbor(i, j, 1)) {
                    candidate.put(c, String.format("%02d%02d", i, j));
                    c++;
                }
                else if (hasNeighbor(i, j, 2)) {
                    len2.add(String.format("%02d%02d", i, j));
                }
            }
        }
        for (int i = 0; i < len2.size(); i++) {
            candidate.put(c, len2.get(i));
            c++;
        }
        if (c == 0) {
            candidate.put(c, String.format("%02d%02d", 7, 7));
            c++;
        }
//        System.out.println("Generate Steps: ");
//        for (int i = 0; i < candidate.size(); i++) {
//            System.out.println(candidate.get(i));
//        }
        return candidate;
    }

    private boolean hasNeighbor(int r, int c, int len) {
        int rstart = r - len, rend = r + len;
        int cstart = c - len, cend = c + len;
        for (int i = max(rstart, 0); i < min(rend, ROWS); i++) {
            for (int j = max(cstart, 0); j < min(cend, COLS); j++) {
                if (!tboard[i][j].empty)
                    return true;
            }
        }
        return false;
    }

    private void printtboardWithIndent(Square[][] tboard, int indent) {
        String IND = "";
        for (int i = 0; i < indent; i++)
            IND += " ";
        for (int i = 0; i < ROWS; i++) {
            System.out.print(IND);
            for (int j = 0; j < COLS; j++) {
                if (tboard[i][j].empty) System.out.print("-");
                else System.out.print(tboard[i][j].color);
            }
            System.out.println();
        }
    }
}