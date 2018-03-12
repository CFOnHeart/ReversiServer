package cc.cxsj.nju.reversi.chess;

import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;

import java.util.IntSummaryStatistics;

public class ChessBoard {
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static final int INTERVAL = Integer.valueOf(ServerProperties.instance().getProperty("play.interval"));
	private static String spliter = "--------------------------------------------";
    private int[] dx = new int[]{0, 1, 1,  1, 0, -1, -1, -1};
    private int[] dy = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
	
	// the chess board
	private Square[][] board = new Square[ROWS][COLS];
    private int lastStepRow = -1, lastStepCol = -1;

	public ChessBoard() {}

	public void generateEmptyChessBoard() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if( (i == 3 && j == 3) || (i == 4 && j == 4)){
					board[i][j] = new Square(0);
					//MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 4), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, i, j, 0);
				}
				else if( (i == 3 && j == 4) || (i == 4 && j == 3) ){
					board[i][j] = new Square(1);
					MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, i, j, 1);
				}
				else{
					this.board[i][j] = new Square(-1);
				}
            }
        }
        MainFrame.instance().ClearChessBoardUI();
    }

    public boolean inBoard(int row, int col) {
        return (row >= 0 && row < ROWS && col >=0 && col < COLS);
    }

	/**
	 * 
	 * @param step
	 * @param color 0 is black, 1 is white
	 * @return if
	 * 		   code true : success;
	 * 		   code false : cannot lazi at position (x, y);
	 */
    private boolean step(int x, int y, int color){
    	if(board[x][y].color == -1 && canLazi(x,y,color)){
    		lazi(x , y, color);
    		
    		return true;
    	}
    	
    	
    	return false;
    }
    
	public String step(String step, int stepNum, int color) {
		// System.out.println("Handle Step " + step + " " + color);
		// check step or not
		if (step.charAt(0) != 'S') {  // S represents step
            // System.out.println("Step.charAt(0) != S");
            return "R1";
        }
		
		switch (step.charAt(1)) {
            case 'P':
            {
                // put down the piece
            	//for (int i = 2; i < 4; i ++){
                for (int i = 2; i < 6; i++) {
                    if (step.charAt(i) > '9' || step.charAt(i) < '0')
                        return "R2";
                }
            	//int desRow = Integer.valueOf(step.substring(2, 3)), desCol = Integer.valueOf(step.substring(3, 4));
                int desRow = Integer.valueOf(step.substring(2, 4)), desCol = Integer.valueOf(step.substring(4, 6));
                if (desRow >= ROWS || desRow < 0) return "R2";
                if (desCol >= COLS || desCol < 0) return "R2";
                Square desSquare = this.board[desRow][desCol];

                System.out.println("desRow = " + desRow + ". desCol = " + desCol + ". color = " + color);
                if (step(desRow, desCol, color)){
                	MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 6), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, desRow, desCol, color);
                    
                    //used for test
                    for (int i=0; i<ROWS; i++) {
                        for (int j=0; j<COLS; j++) {
                            if (this.board[i][j].color != -1)
                                System.out.print(this.board[i][j].color + " ");
                            else
                                System.out.print("-" + " ");
                        }
                        System.out.println();
                    }
                    
                    lastStepRow = desRow;
                    lastStepCol = desCol;
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "R0" + step.substring(1, 6) + desSquare.color;
                }
                else{
                	System.out.println("Put Down ERROR");
                    for (int i=0; i<ROWS; i++) {
                        for (int j=0; j<COLS; j++) {
                            if (this.board[i][j].color != -1)
                                System.out.print(this.board[i][j].color + " ");
                            else
                                System.out.print("-" + " ");
                        }
                        System.out.println();
                    }
                    return "R4";
                }
                
                /*if (desSquare.empty) {
                    // update step result
                    desSquare.empty = false;
                    desSquare.color = color;
                    MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 6), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, desRow, desCol, color);
                    lastStepRow = desRow;
                    lastStepCol = desCol;
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "R0" + step.substring(1, 6) + desSquare.color;
                } else {
                    System.out.println("Put Down ERROR");
                    for (int i=0; i<15; i++) {
                        for (int j=0; j<15; j++) {
                            if (this.board[i][j].color != -1)
                                System.out.print(this.board[i][j].color + " ");
                            else
                                System.out.print("-" + " ");
                        }
                        System.out.println();
                    }
                    return "R4";
                }*/
            }
            /*case 'D':
            {
                // put down the piece
                for (int i = 2; i < 6; i++) {
                    if (step.charAt(i) > '9' || step.charAt(i) < '0')
                        return "R2";
                }
                int desRow = Integer.valueOf(step.substring(2, 4)), desCol = Integer.valueOf(step.substring(4, 6));
                if (desRow >= ROWS || desRow < 0) return "R2";
                if (desCol >= COLS || desCol < 0) return "R2";
                Square desSquare = this.board[desRow][desCol];

                if (!desSquare.empty) {
                    // update step result
                    desSquare.empty = true;
                    desSquare.color = -1;
                    MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 6), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, desRow, desCol, -1);
                    lastStepRow = desRow;
                    lastStepCol = desCol;
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "R0" + step.substring(1, 6) + color;
                } else {
                    System.out.println("Disappear ERROR");
                    return "R4";
                }
            }*/
            case 'N': {   // Nostep
                return "R0N";
            }
            default:
                return "R1";
		}
	}

	/**
	 * exist an position that can reversi chessman
	 *  
	 */
	public boolean isGameEnd(){
		boolean gameEnd = true;
		
		for(int x = 0; x < ROWS; x ++){
			for(int y = 0; y < COLS; y ++){
<<<<<<< HEAD
				// ganjun need to debug
				// 这个位置不存在棋子，且白棋可落子或者黑气可落子(但是这里落子考虑了当前回合的禁手，之后应该需要改为无禁手位置可落子)
				if(!board[x][y].existChessman()  && (canLazi(x,y,0) || canLazi(x,y,1))){
=======
				if( (board[x][y].color == -1 && (canLazi(x,y,0) || canLazi(x,y,1))) ){
				//if(! (board[x][y].existChessman() || !canLazi(x,y,0) || !canLazi(x,y,1))){
>>>>>>> cxc
					return false;
					
				}
			}
		}
		
		return gameEnd;
	}
	
	/**
	 * can player put chessman on the position (x,y)
	 * 
	 */
	public boolean canLazi(int x, int y, int chessmanColor){
		boolean lazi = false;
		
		//if the position (x,y) is not empty
		if(board[x][y].color != -1){
			lazi = false;
		}
		//
		else{
			//travel all direction of position (x,y) to find the chessman confirm to reversi rules 
			for(int dir = 0; dir < dx.length; dir ++){
				
				//can reversi in an direction 
				if(canReversiInDirection(x,y,chessmanColor, dir)){
					//System.out.println("dir��" + dir);
					lazi = true;
					break;
				}
			}
		}
		

		return lazi;
	}
	
	/**
	 * place a chessman on the position (x, y) 
	 * reversi chessman
	 */
	
	public void lazi(int x, int y, int chessmanColor){
		
		//if(!canLazi(x,y, chessmanColor)){
		//	return false;
		//}
		
		//cancel prohibition
		for(int i = 0; i < ROWS; i ++){
			for(int j = 0; j < COLS; j ++){
				if(board[i][j].color == 2){
					board[i][j].color = -1;
				}
			}
		}
		
		//travel all direction of position (x,y) to find the chessman confirm to reversi rules 
		for(int dir = 0; dir < dx.length; dir ++){
			
			//can reversi in an direction 
			if(canReversiInDirection(x,y,chessmanColor, dir)){
				int pos_x = x + dx[dir];
				int pos_y = y + dy[dir];
				while(board[pos_x][pos_y].color != chessmanColor){
					board[pos_x][pos_y].color = chessmanColor;
					pos_x += dx[dir];
					pos_y += dy[dir];
				}
			}
		}
	
		board[x][y].color = chessmanColor;
		
		//prohibition
		for(int dir = 0; dir < 8; dir += 2){
			int pos_x = x + dx[dir], pos_y = y + dy[dir];
			if(inBoard(pos_x, pos_y) && board[pos_x][pos_y].color == -1){
				board[pos_x][pos_y].color = 2;
			}
		}
		
	}
	
	/**
	 * 
	 */
	private boolean canReversiInDirection(int x, int y, int chessmanColor, int dir){
		int pos_x = x + dx[dir], pos_y = y + dy[dir];
		int color = chessmanColor;
		
		boolean opposite = false;
		boolean reversi = false;
		
		//travel neighbor in direction dir
		while(inBoard(pos_x, pos_y) && !reversi){

			if(!board[pos_x][pos_y].existChessman()){
				break;
			}
			//if the color of neighbor position is the opposite color of player 
			if(board[pos_x][pos_y].color == 1-color){
				opposite = true;
			}
			//if the color of neighbor position is the same color of player 
			//&& there is no opposite color chessman between this position and target position
			else if(!opposite){
				break;
			}
			//if the color of neighbor position is the same color of player 
			//&& there is some opposite color chessman between this position and target position
			else{
				reversi = true;
				break;
			}
			pos_x += dx[dir];
			pos_y += dy[dir];
		}	
		return reversi;
	}
	
	/**
	 * -1 has not winnner, 0 winner is black, 1 winner is white, 2 is draw
	 * 
	 * @return
	 */
	public int isGeneratedWinner(){
		if(isGameEnd()){
			int blackCount = 0, whiteCount = 0;
			
			//count black chessman and white chessman
			for(int x = 0; x < ROWS; x ++){
				for(int y = 0; y < COLS; y ++){
					if(board[x][y].color == 0){
						blackCount ++;
					}
					else if(board[x][y].color == 1){
						whiteCount ++;
					}
				}
			}
			
			if(blackCount > whiteCount){
				return 0;
			}
			else if(blackCount < whiteCount){
				return 1;
			}
			else{
				return 2;
			}
		}
		return -1;
	}
	
	
	/*public int isGeneratedWinnner() {
		int sR = lastStepRow;
        int sC = lastStepCol;
        int nowColor = -1;
        if (inBoard(sR, sC) && board[sR][sC].color == -1) return -1;
        else if(inBoard(sR, sC)) nowColor = board[sR][sC].color;
        int MaxSeq = 1;
        for (int dir = 0; dir < 4; dir++) {       // 4 directions and their opposite directions
            int seq = 1;
            boolean d0 = true, d1 = true;
            for (int len = 1; len <= 4; len++) {  // walk at most 4 steps
                int deltax = dx[dir]*len;
                int deltay = dy[dir]*len;
                if (d0 && inBoard(sR+deltax, sC+deltay) && board[sR+deltax][sC+deltay].color == nowColor)
                    seq++;
                else
                    d0 = false;
                if (d1 && inBoard(sR-deltax, sC-deltay) && board[sR-deltax][sC-deltay].color == nowColor)
                    seq++;
                else
                    d1 = false;
                MaxSeq = seq > MaxSeq ? seq:MaxSeq;
            }
        }
        if (MaxSeq >= 5) {
            System.out.println("Winner is " + (nowColor==0?"Black":"White"));
            return nowColor;
        }
		return -1;
	}*/

	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(spliter);
		sb.append("\n");
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				sb.append(board[i][j].toStringToDisplay());
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}
	
	public Square[][] getSquares(){
		return this.board;
	}
}
