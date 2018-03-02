package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.chess.Square;
import cc.cxsj.nju.reversi.config.ServerProperties;

public abstract class RobotAI {
	
	protected static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	protected static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	
	// game board
	protected Square[][] board;
	protected int ownColor = -1;
	protected int oppositeColor = -1;
	protected int round = 0;
	
	protected String thisStep;
	
	public RobotAI() {           // 维护本地棋盘
		this.board = new Square[ROWS][COLS];
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				this.board[r][c] = new Square();
			}
		}
	}
	
	protected void putDown(int row, int col) {
		String step = "SP";
		step += String.format("%02d", row);
		step += String.format("%02d", col);
		thisStep = step;
	}
	

	protected void noStep() {
		thisStep = "SN";
	}
	
	protected void updateLastStep(String step) {   // P08070
		switch (step.charAt(0)) {
            case 'P':
            {
                int desRow = Integer.valueOf(step.substring(1, 3)), desCol = Integer.valueOf(step.substring(3, 5));
                int color = step.charAt(5) - '0';
                board[desRow][desCol].empty = false;
                board[desRow][desCol].color = color;
                break;
            }
			case 'D':
			{
				int desRow = Integer.valueOf(step.substring(1, 3)), desCol = Integer.valueOf(step.substring(3, 5));
				int color = step.charAt(5) - '0';
				board[desRow][desCol].empty = true;
				board[desRow][desCol].color = -1;
				break;
			}
            case 'N':
                break;
            default:
                break;
		}
	}
	
	protected void roundStart(int color) {
		// System.out.println("Round Start, I'am " + color);
		this.ownColor = color;
		this.oppositeColor = 1 - color;
	}
	
	protected void roundOver() {
		this.round++;
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				this.board[r][c].reset();
			}
		}
	}
	
	protected void gameOver() {
		
	}
	
	/**
	 * receive msg
	 * 
	 * @param msg
	 */
	public void receiveMsg(String msg) {
		switch (msg.charAt(0)) {
            case 'B':
                // one round begin and the piece color assigned
                System.out.println("Receive Msg Round Start~");
                switch (msg.charAt(1)) {
                    case 'B':
                        roundStart(0);
                        break;
                    case 'W':
                        roundStart(1);
                        break;
                    default:
                        break;
                }
                break;
            case 'R':
                // return code
                switch (msg.charAt(1)) {
                    case '0':
                        // step is valid and update board
                        updateLastStep(msg.substring(2));
                        break;
                    default:
                        System.out.println("Round " + round +  " Error Code " + msg.charAt(1));
                        break;
                }
                break;
            case 'E':     // "E0": game over   "E1": round over
                // round or contest end
                switch (msg.charAt(1)) {
                    case '0':
                        gameOver();
                        break;
                    case '1':
                        roundOver();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
		}
	}
	
	/**
	 * the next step, you must be sure that the lastStep is returned
	 * 
	 * @return
	 */
	public abstract String step();
}
