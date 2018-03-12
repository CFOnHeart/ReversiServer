package cc.cxsj.nju.reversi.ai;

import java.util.Random;


public class ReversiRobotAITravel extends ReversiRobotAI{

	
	@Override
	public String step() {
		// TODO �Զ����ɵķ������
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
        int count = 0;  // 如果随机抽取超过200次，表示robot无棋可以下
        System.out.println("robot start chess");
		while (!(r >= 0 && r < ROWS && c >= 0 && c < COLS && chessboard.canLazi(r, c, ownColor))) {
			
			r = rand.nextInt(ROWS);
			c = rand.nextInt(COLS);
            count++;
            if(count >= 200) return false;
			// System.out.println("Rand " + r + " " + c);
		}
        System.out.println("robot chess at: " + r + " - " + c);
		putDown(r, c);
		
        return true;
    }

}
