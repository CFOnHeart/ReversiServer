package cc.cxsj.nju.reversi.chess;

import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Vector;

public class ChessBoard {
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static final int INTERVAL = Integer.valueOf(ServerProperties.instance().getProperty("play.interval"));
	private static String spliter = "--------------------------------------------";
    private int[] dx = new int[]{0, 1, 1,  1, 0, -1, -1, -1};
    private int[] dy = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
	
	// the chess board
	public Square[][] board = new Square[ROWS][COLS];
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
	 * @param
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
		System.out.println("The " + stepNum + " of color " +
				(color==0?"Black":"White")+" Step " + " with message: " + step);
		if(existLazi(color) == false){
			return "R0N";
		}
    	// ganjun add 下过来的棋子出错功能
		if (step.substring(0 , 2).compareTo("In") == 0){
			MainFrame.instance().log("color : " + (color==0?"black":"white")
					+ " play an invalid step\n It will get an 5 score punishment");
			return step("SP"+randomStep(color) , stepNum , color);
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
					printChessBoard();
                    
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
					printChessBoard();
                    return "R4";
                }

            }

            case 'N': {   // Nostep
            	if(existLazi(color)){
            		return "R0N";
            	}
            	else{
            		return "R0N";
            		//return "R1N";
            	}
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
				if( ((board[x][y].color == -1 || board[x][y].color==2)
						&& (stopCanLazi(x,y,0) || stopCanLazi(x,y,1))) ){
					return false;
					
				}
			}
		}
		
		return gameEnd;
	}

	/**
	 * can player put chessman on the position (x,y)
	 * function same as canLazi
	 * but x,y can be stop position
	 */
	public boolean stopCanLazi(int x, int y, int chessmanColor){
		boolean lazi = false;


		//travel all direction of position (x,y) to find the chessman confirm to reversi rules
		for(int dir = 0; dir < dx.length; dir ++){

			//can reversi in an direction
			if(canReversiInDirection(x,y,chessmanColor, dir)){
				//System.out.println("dir��" + dir);
				lazi = true;
				break;
			}
		}
		return lazi;
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
				System.out.println("This round BLACK WIN");
				return 0;
			}
			else if(blackCount < whiteCount){
				System.out.println("This round WHITE WIN");
				return 1;
			}
			else{
				System.out.println("This round BLACK AND WHITE ALL WIN");
				return 2;
			}
		}
		return -1;
	}

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

	/*
	随机下下一步可以下的棋，返回行和列 如row = 2 , col = 3 , 返回 "0203"
	 */
	public String randomStep(int color){
		List<String> list = new ArrayList<>();
		for(int r=0 ; r<ROWS ; r++){
			for (int c=0 ; c<COLS ; c++){
				if(canLazi(r , c , color)) list.add(int2String(r) + int2String(c));
			}
		}
		return list.get((int)Math.random()*list.size());
	}
	// 将整数转化为2位数字符串
	public String int2String(int x){
		if(x<10) return "0"+String.valueOf(x);
		else
			return String.valueOf(x);
	}

	// if exist an position that a chessman can lazi at
	public boolean existLazi(int color){
		for(int i = 0; i < ROWS; i ++){
			for(int j = 0; j < COLS; j ++){
				if(canLazi(i, j , color)){
					return true;
				}

			}
		}
		return false;
	}
	// 打印board
	public void printChessBoard(){
//		for (int i=0; i<ROWS; i++) {
//////			for (int j=0; j<COLS; j++) {
//////				if (this.board[i][j].color != -1)
//////					System.out.print(this.board[i][j].color + " ");
//////				else
//////					System.out.print("-" + " ");
//////			}
//////			System.out.println();
//////		}
		System.out.println(toStringToDisplay());
	}

	public static void main(String [] args) {
		ChessBoard cb = new ChessBoard();
		cb.generateEmptyChessBoard();
		for(int i = 0 ; i<ChessBoard.ROWS ; i++){
			for(int j=0 ; j<ChessBoard.COLS ; j++){
				cb.board[i][j].reset();
			}
		}
		cb.board[0][0].color = 0;
		cb.board[0][1].color = cb.board[1][0].color = 1;
		cb.board[0][2].color = cb.board[2][0].color = 2;
		System.out.println("here");
		System.out.println(cb.isGameEnd());
	}
}



