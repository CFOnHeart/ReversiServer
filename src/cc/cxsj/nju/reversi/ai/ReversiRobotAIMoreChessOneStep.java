package cc.cxsj.nju.reversi.ai;

public class ReversiRobotAIMoreChessOneStep extends ReversiRobotAI{

	@Override
	public String step() {
		// TODO 自动生成的方法存根
		boolean isThisStep = false;
		
		// find the first empty grid and put down
		if (!isThisStep) {
			isThisStep = moreChessOneStep();
		}

		// no step is last step
		if (!isThisStep) {
			noStep();
			System.out.println("Robot no step");
		}
		
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}*/
		
		return thisStep;
	}

	private boolean moreChessOneStep(){
		if(!chessboard.existLazi(ownColor)){
        	return false;
        }
		
		int maxNum = 0;
		int max_x = 0, max_y = 0;
		for(int i = 0; i < ROWS; i ++){
			for(int j = 0; j < COLS; j ++){
				if(!chessboard.canLazi(i, j, ownColor)){
					continue;
				}
				int cnt = chessboard.reversiNum(i, j, ownColor);
				if(cnt > maxNum){
					maxNum = cnt;
					max_x = i;
					max_y = j;
				}
			}
		}
		
		if(maxNum == 0){
			return false;
		}
		putDown(max_x, max_y);
		System.out.println("robot(" + max_x + "," + max_y + "):" + maxNum);
		
		return true;
	}
	
}
