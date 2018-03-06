#include "Board.h"


Board::Board(void)
{
	
	resetBoard();
}


Board::~Board(void)
{
}

//you should rewrite this function to get a better strategy
void Board::step(int& row, int& col, int color){


	//find a position can lazi. return
	for(int x = 0; x < ROWS; x ++){
		for(int y = 0; y < COLS; y ++){
			if(canLazi(x, y, color)) {
				row = x;
				col = y;
			}
		}
	}
}

bool Board::lazi(int x, int y, int color){
	//travel all direction of position (x,y) to find the chessman confirms to reversi rules 
	for(int dir = 0; dir < DIR; dir ++){
		if(canReversiInDirection(x, y , color, dir)){
			//move a distance in the direction dir
			int pos_x = x + dx[dir];
			int pos_y = y + dy[dir];

			//if the color is opposite to the chessman in the position(pos_x, pos_y)
			while( board[pos_x][pos_y].compareColor(color)){
				board[pos_x][pos_y].lazi(color);
				pos_x += dx[dir];
				pos_y += dy[dir];
			}

		}
	}

	board[x][y].lazi(color);
}

//can player put down a chessman on position (x,y) 
bool Board::canLazi(int x, int y, int color){
	//this square is not empty
	if(!board[x][y].isEmpty()){
		return false;
	}

	bool lazi = false;
	//travel all directions of the position (x, y)
	for(int dir = 0; dir < DIR; dir ++){
		if(canReversiInDirection(x, y, color, dir)){
			lazi = true;
			break;
		}
	}

	return lazi;
}


/*player put down a chessman on position (x,y)
can reversi some chessman in direction dir
*/
bool Board::canReversiInDirection(int x, int y, int color, int dir){
	//the position move 1 distance unit in the direction dir
	int pos_x = x + dx[dir], pos_y = y + dy[dir];
	
	bool reversi = false;

	//is there a chessman that has opposite color
	bool opposite = false;
	//is there a violation in the direction dir
	bool violation = false; 

	//travel neighbor in the direction dir
	while(inBoard(pos_x, pos_y) && (!violation) && (!reversi) ){
		switch(board[pos_x][pos_y].compareColor(color)){
		//colors are the same
		case 0:
			//there is some opposite color chessman between this position and target position then reversi is true
			//there is no opposite color chessman between this position and target position then cannot reversi
			(opposite)?reversi = true: violation = true;
			break;
		//colors are opposite
		case 1:
			opposite = true;
			break;
		default:
			violation = true;
			break;
		}
		//move a distance in the direction dir
		pos_x += dx[dir];
		pos_y += dy[dir];
	}

	return reversi;
}

/*set prohibition in the board
*	in the position (x, y) a distance in direction up, right, down, left
*/
void Board::setProhibition(int x, int y)
{	//travel in the direction up, right, down, left
	for(int dir = 0; dir < DIR; dir += 2){
		//move a distance in the direction dir
		int pos_x = x + dx[dir], pos_y = y + dy[dir];
		if(inBoard(pos_x, pos_y) ){
			board[pos_x][pos_y].setProhibition();
		}
	}
}

//cencel prohibition in the board
void Board::cancelProhibition(){
	for(int x = 0; x < ROWS; x ++){
		for(int y = 0; y < COLS; y ++){
			board[x][y].cancelProhibition();
		}
	}
}

//reset the board
void Board::resetBoard(){
	for(int x = 0; x < ROWS; x ++){
		for(int y = 0; y < COLS; y ++){
			//put black chessman on the position (3,3) (4,4) 
			if( (x == 3 && y == 3) || (x == 4 && y == 4) ){
				board[x][y].lazi(0);
			}
			//put white chessman on the position (3,4) (4,3) 
			else if( (x == 3 && y == 4) || (x == 4 && y == 3) ){
				board[x][y].lazi(1);
			}
			else{
				board[x][y].clear();
			}
		}
	}
}