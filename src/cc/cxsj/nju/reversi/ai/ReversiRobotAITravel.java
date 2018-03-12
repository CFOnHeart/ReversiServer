package cc.cxsj.nju.reversi.ai;

import java.util.Random;


public class ReversiRobotAITravel extends ReversiRobotAI{

	
	@Override
	public String step() {
		// TODO 自动生成的方法存根
		boolean isThisStep = false;
		
		// find the first empty grid and put down
		if (!isThisStep) {
			isThisStep = randomPutDown();
		}

		// no step is last step
		if (!isThisStep) {
			noStep();
			System.out.println("Robot no step");
		}
		
		return thisStep;
	}
	
	private boolean randomPutDown() {
       
        Random rand = new Random(System.currentTimeMillis());
        int r = -1, c = -1;
		while (!(r >= 0 && r < ROWS && c >= 0 && c < COLS && chessboard.canLazi(r, c, ownColor))) {
			
			r = rand.nextInt(ROWS);
			c = rand.nextInt(COLS);
			// System.out.println("Rand " + r + " " + c);
		}
		putDown(r, c);
		
        return true;
    }

}
