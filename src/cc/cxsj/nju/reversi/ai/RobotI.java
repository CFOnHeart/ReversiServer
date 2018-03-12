package cc.cxsj.nju.reversi.ai;

import java.util.Random;

public class RobotI extends RobotAI{

    private boolean full = false;
	@Override
	public String step() {
		boolean isThisStep = false;
		
		// find the first empty grid and put down
		if (!isThisStep) {
			isThisStep = randomPutDown();
		}

		// no step is last step
		if (!isThisStep) {
			noStep();
		}
		
		return thisStep;
	}

	private boolean randomPutDown() {
        Random rand = new Random(System.currentTimeMillis());
        int r = -1, c = -1;
		while (!(r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c].empty)) {
			
			r = rand.nextInt(ROWS);
			c = rand.nextInt(COLS);
			// System.out.println("Rand " + r + " " + c);
		}
		putDown(r, c);
        return true;
    }
}
