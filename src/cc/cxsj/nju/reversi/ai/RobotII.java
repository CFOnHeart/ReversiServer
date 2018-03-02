package cc.cxsj.nju.reversi.ai;

/**
 * Created by huqiu on 17-4-3.
 */
public class RobotII extends RobotAI {

    private boolean full = false;
    private int lastRow = 0;
    private int lastCol = 0;

    @Override
    public String step() {
        boolean isThisStep = false;

        if (!isThisStep) {
            isThisStep = findFirstEmptyAndPutDown();
        }

        if (!isThisStep) {
            noStep();
        }

        return thisStep;
    }

    private boolean findFirstEmptyAndPutDown() {
        int iter = 0;
        while (!board[lastRow][lastCol].empty) {
            lastRow++;
            lastCol++;
            lastRow = lastRow % 15;
            lastCol = lastCol % 15;
            iter++;
            if (iter > 20)
                break;
        }
        if (iter > 20) return false;
        putDown(lastRow, lastCol);
        return true;
    }
}
