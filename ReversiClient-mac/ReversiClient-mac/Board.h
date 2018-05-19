//
//  Board.h
//  ReversiClient-mac
//
//  Created by ganjun on 2018/5/17.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Board_h
#define Board_h
#include "stdio.h"
#include "string.h"
#include <vector>
#include <iostream>
using namespace std;
#define pii pair<int,int>
#define INF 0x7fffffff
class Board{
public:
    int square[8][8]; //0 as black, 1 as white, -1 as null, 2 as prohibit
    const int dir[8][2] = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    Board();
    
    ~Board();
    
    void initBoard();
    
    bool inBoard(int row , int col);
    
    bool canPlace(int row , int col , int color); // judge can put the color of chessman at (row, col)
    
    vector<pii> validSteps(int color);
    
    pii step(int color);
    
    void excuteStep(int row , int col , int color);
    
    void cancelProhibition();
    
    void setProhibition(int row, int col);
    
    void print();
    
    int evaluate(int color);
    
    int dfs(int depth, bool flag, int color, int &row, int &col);
    
    bool isEnd(); // judge the round whether end
    
    uint64_t encode();
};

#endif /* Board_h */
