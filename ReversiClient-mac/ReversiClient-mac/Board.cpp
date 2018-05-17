//
//  Board.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/6.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Board.h"
//up, up_right, right, down_right, down, down_left, left, up_left,
int dx[DIR] = {0, 1, 1, 1, 0 , -1, -1, -1};
int dy[DIR] = {1, 1, 0, -1, -1, -1, 0, 1};
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
    
    std::string str;
    //cancel prohibion
    for(int i = 0; i < ROWS; i ++){
        for(int j = 0; j < COLS; j ++){
            squares[i][j].cancelProhibition();
            squares[i][j].print(str);
        }
        str += "\r\n";
    }
    
    printf("%s\r\n", str.c_str());
    
    //travel all direction of position (x,y) to find the chessman confirms to reversi rules
    for(int dir = 0; dir < DIR; dir ++){
        if(countReversiInDirection(x, y , color, dir) > 0){
            //move a distance in the direction dir
            int pos_x = x + dx[dir];
            int pos_y = y + dy[dir];
            
            //if the color is opposite to the chessman in the position(pos_x, pos_y)
            while(inBoard(pos_x,pos_y) &&  squares[pos_x][pos_y].compareColor(color)){
                squares[pos_x][pos_y].reversi();
                pos_x += dx[dir];
                pos_y += dy[dir];
            }
            
        }
    }
    
    //set prohibition
    for(int dir = 0; dir < DIR; dir ++){
        int pos_x = x + dx[dir];
        int pos_y = y + dy[dir];
        squares[pos_x][pos_y].setProhibition();
        
    }
    
    return squares[x][y].lazi(color);
}

// update pos (x,y) color
void Board::updateColor(int x, int y, int color)
{
    squares[x][y].setColor(color);
}

//can player put down a chessman on position (x,y)
bool Board::canLazi(int x, int y, int color){
    //this square is not empty
    if(!squares[x][y].isEmpty() ){
        return false;
    }
    
    bool lazi = false;
    //travel all directions of the position (x, y)
    for(int dir = 0; dir < DIR; dir ++){
        if(countReversiInDirection(x, y, color, dir) > 0){
            lazi = true;
            break;
        }
    }
    
    return lazi;
}


/*player put down a chessman on position (x,y)
 can reversi some chessman in direction dir
 */
int Board::countReversiInDirection(int x, int y, int color, int dir){
    int ret = 0;
    //the position move 1 distance unit in the direction dir
    int pos_x = x + dx[dir], pos_y = y + dy[dir];
    
    bool reversi = false;
    
    //is there a chessman that has opposite color
    bool opposite = false;
    //is there a violation in the direction dir
    bool violation = false;
    
    //travel neighbor in the direction dir
    while(inBoard(pos_x, pos_y) && (!violation) && (!reversi) ){
        switch(squares[pos_x][pos_y].compareColor(color)){
                //colors are the same
            case 0:
                //there is some opposite color chessman between this position and target position then reversi is true
                //there is no opposite color chessman between this position and target position then cannot reversi
                (opposite)?reversi = true: violation = true;
                break;
                //colors are opposite
            case 1:
                opposite = true;
                ret ++;
                break;
            default:
                ret = 0;
                violation = true;
                break;
        }
        //move a distance in the direction dir
        pos_x += dx[dir];
        pos_y += dy[dir];
    }
    if(violation || !reversi){
        ret = 0;
    }
    return ret;
}

/*set prohibition in the board
 *    in the position (x, y) a distance in direction up, right, down, left
 */
void Board::setProhibition(int x, int y)
{    //travel in the direction up, right, down, left
    for(int dir = 0; dir < DIR; dir += 2){
        //move a distance in the direction dir
        int pos_x = x + dx[dir], pos_y = y + dy[dir];
        if(inBoard(pos_x, pos_y) ){
            squares[pos_x][pos_y].setProhibition();
        }
    }
}

//cencel prohibition in the board
void Board::cancelProhibition(){
    for(int x = 0; x < ROWS; x ++){
        for(int y = 0; y < COLS; y ++){
            squares[x][y].cancelProhibition();
        }
    }
}

//reset the board
void Board::resetBoard(){
    for(int x = 0; x < ROWS; x ++){
        for(int y = 0; y < COLS; y ++){
            //put black chessman on the position (3,3) (4,4)
            if( (x == 3 && y == 3) || (x == 4 && y == 4) ){
                squares[x][y].setColor(0);
            }
            //put white chessman on the position (3,4) (4,3)
            else if( (x == 3 && y == 4) || (x == 4 && y == 3) ){
                squares[x][y].setColor(1);
            }
            else{
                squares[x][y].clear();
            }
        }
    }
}


//exist a position that a chessman can lazi
bool Board::existLazi(int color){
    for(int i = 0; i < ROWS; i ++){
        for(int j = 0; j < COLS; j ++){
            if(canLazi(i,j,color)){
                return true;
            }
        }
    }
    return false;
}

//print the total board
void Board::print(){
    std::string str;
    for(int r = 0; r < ROWS; r ++){
        for(int c = 0; c < COLS; c ++){
            squares[r][c].print(str);
        }
        str += "\n";
    }
    printf("%s\n", str.c_str());
}
