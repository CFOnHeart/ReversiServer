//
//  Board.h
//  ReversiClient
//
//  Created by ganjun on 2018/3/6.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Board_h
#define Board_h

#pragma once

#include "Square.h"

#define ROWS 8
#define COLS 8
#define DIR     8

/* the class represent the chess board
 * board[ROWS}[COLS} represent the board
 * dx, dy represent vectors in DIR(8) directions
 */



class Board
{
private:
    Square squares[ROWS][COLS];
    int lastStepCol=-1 ,lastStepRow=-1;
    
public:
    Board(void);
    ~Board(void);
    
    inline Square getSquare(int x, int y){
        return squares[x][y];
    }
    
    void step(int& row, int& col, int color);
    
    //is position(x,y) in the board
    inline bool inBoard(int x, int y){
        return ( x >= 0 && x < ROWS && y >= 0 && y < COLS);
    }
    
    //put down a chessman on position (x,y)
    bool lazi(int x, int y, int color);
    
    // update pos (x,y) color
    void updateColor(int x, int y, int color);
    
    //reset the board
    void resetBoard();
    
    //cencel prohibition in the board
    void cancelProhibition();
    
    /*set prohibition in the board
     *    in the position (x, y) a distance in direction up, right, down, left
     */
    void setProhibition(int x, int y);
    
    //can player put down a chessman on position (x,y)
    bool canLazi(int x, int y, int color);
    //exist a position that a chessman can lazi
    bool existLazi(int color);
    
    /*player put down a chessman on position (x,y)
     can reversi some chessman in direction dir
     return reversi chessman's count
     */
    int countReversiInDirection(int x, int y, int color, int dir);
    
    /*print the total board
     */
    void print();
    
    void updateProhibition();
    
    inline int getLastStepRow(){
        return this->lastStepRow;
    }
    
    inline int getLastStepCol(){
        return this->lastStepCol;
    }
    
    inline void setLastStepRow(int lastStepRow){
        this->lastStepRow = lastStepRow;
    }
    
    inline void setLastStepCol(int lastStepCol){
        this->lastStepCol = lastStepCol;
    }
};
#endif /* Board_h */
