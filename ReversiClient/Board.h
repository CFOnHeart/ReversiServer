#pragma once

#include "Square.h"

#define ROWS 8
#define COLS 8
#define DIR	 8

/* the class represent the chess board
* board[ROWS}[COLS} represent the board
* dx, dy represent vectors in DIR(8) directions
*/

//up, up_right, right, down_right, down, down_left, left, up_left,
int dx[DIR] = {0, 1, 1, 1, 0 , -1, -1, -1};
int dy[DIR] = {1, 1, 0, -1, -1, -1, 0, 1};

class Board
{
private:
	Square board[ROWS][COLS];

	//can player put down a chessman on position (x,y) 
	bool canLazi(int x, int y, int color);

	/*player put down a chessman on position (x,y)
	can reversi some chessman in direction dir
	*/
	bool canReversiInDirection(int x, int y, int color, int dir);
public:
	Board(void);
	~Board(void);

	
	void step(int& row, int& col, int color);

	//is position(x,y) in the board
	inline bool inBoard(int x, int y){
		return ( x >= 0 && x < ROWS && y >= 0 && y < COLS);
	}

	//put down a chessman on position (x,y)
	bool lazi(int x, int y, int color);

	//reset the board
	void resetBoard();

	//cencel prohibition in the board
	void cancelProhibition();

	/*set prohibition in the board
	*	in the position (x, y) a distance in direction up, right, down, left
	*/
	void setProhibition(int x, int y);
};

