//
//  Board.cpp
//  ReversiClient-mac
//
//  Created by ganjun on 2018/5/17.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Board.h"

Board::Board(){
    memset(square, -1, sizeof(square));
}

Board::~Board(){}

void Board::initBoard(){
    memset(square, -1, sizeof(square));
    square[3][3] = square[4][4] = 0;
    square[3][4] = square[4][3] = 1;
}

bool Board::inBoard(int row, int col){
    return row>=0 && row<8 && col>=0 && col<8;
}

bool Board::canPlace(int row, int col, int color){
    for(int i=0 ; i<8 ; i++){
        int x = row + dir[i][0];
        int y = col + dir[i][1];
        if(inBoard(x, y) && square[x][y] == 1-color){
            while(inBoard(x, y) && square[x][y] == 1-color){
                x = x + dir[i][0];
                y = y + dir[i][1];
            }
            if(inBoard(x, y) && square[x][y] == color) return true;
        }
    }
    return false;
}


vector<pii> Board::validSteps(int color){
    vector<pii> valid_steps = vector<pii>();
    for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            if(square[i][j] == -1 && canPlace(i, j, color))
                valid_steps.push_back(make_pair(i, j));
        }
    }
    return valid_steps;
}

pii Board::step(int color){
// random
//    vector<pii> valid_steps = validSteps(color);
//    if(valid_steps.size() == 0) return make_pair(-1, -1);
//    int index = rand() % valid_steps.size();
//    return valid_steps[index];
    // max-min dfs
    int row = -1 , col = -1;
    dfs(4, true, color, row , col);
    return make_pair(row, col);
}

void Board::excuteStep(int row, int col, int color){
    if(row >= 0 && col >= 0){
        for(int i=0 ; i<8 ; i++){
            int x = row + dir[i][0];
            int y = col + dir[i][1];
            if(inBoard(x, y) && square[x][y] == 1-color){
                while(inBoard(x, y) && square[x][y] == 1-color){
                    x = x + dir[i][0];
                    y = y + dir[i][1];
                }
                if(inBoard(x, y) && square[x][y] == color){
                    while(true){
                        x -= dir[i][0];
                        y -= dir[i][1];
                        square[x][y] = color;
                        if(x == row && y == col) break;
                    }
                }
            }
        }
    }
    cancelProhibition();
    setProhibition(row, col);
}

void Board::setProhibition(int row, int col){
    for(int i=0 ; i<4 ; i++){
        int x = row+dir[i][0];
        int y = col+dir[i][1];
        if(inBoard(x, y) && square[x][y]<0) square[x][y] = 2;
    }
}

void Board::cancelProhibition(){
    for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            if(square[i][j] == 2) square[i][j] = -1;
        }
    }
}

void Board::print(){
    for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            if(square[i][j] == -1) cout<<" ";
            else if(square[i][j] == 0) cout<<"b";
            else if(square[i][j] == 1) cout<<"w";
            else cout<<"x";
        }
        cout<<endl;
    }
}

int Board::evaluate(int color){
    int value = 0;
    for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            if(square[i][j] == color) value++;
            else if(square[i][j] == 1-color) value--;
        }
    }
    return value;
}

int Board::dfs(int depth, bool flag, int color, int &row, int &col) // flag: true -> maximum false -> minimum
{
    if(depth == 0) return evaluate(color);
    vector<pii> valid_steps = validSteps(flag?color:1-color);
    int temp[8][8] ;
    for(int i=0 ; i<8 ; i++)
        for(int j=0 ; j<8 ; j++)
            temp[i][j] = square[i][j];
        
    int ret = INF;
    if(flag) ret = -INF;
    if(valid_steps.size() == 0){
        int val = dfs(depth-1 , flag^1 , color , row , col);
        if(flag){
            if(ret < val){
                ret = val;
                if(depth == 4) row = -1 , col = -1;
            }
        }
        else ret = min(ret , val);
    }
    else{
        for(int i=0 ; i<valid_steps.size() ; i++){
            excuteStep(valid_steps[i].first, valid_steps[i].second, flag?color:1-color);
            int val = dfs(depth-1, flag^1, color, row, col);
            if(flag){
                if(ret < val){
                    ret = val;
                    if(depth == 4) row = valid_steps[i].first , col = valid_steps[i].second;
                }
            }
            else ret = min(ret , val);
            //reset
            for(int i=0 ; i<8 ; i++)
                for(int j=0 ; j<8 ; j++) square[i][j] = temp[i][j];
        }
    }
    return ret;
}

bool Board::isEnd(){
    for (int row=0 ; row<8 ; row++){
        for (int col=0 ; col<8 ; col++){
            if(square[row][col] == 0 || square[row][col] == 1) continue;
            for(int color=0 ; color<2 ; color++){
                for(int i=0 ; i<8 ; i++){
                    int x = row + dir[i][0];
                    int y = col + dir[i][1];
                    if(inBoard(x, y) && square[x][y] == 1-color){
                        while(inBoard(x, y) && square[x][y] == 1-color){
                            x = x + dir[i][0];
                            y = y + dir[i][1];
                        }
                        if(inBoard(x, y) && square[x][y] == color) return false;
                    }
                }
            }
        }
    }
    
    return true;
}

uint64_t Board::encode(){
    uint64_t code= 0;
    for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            int t = square[i][j];
            if(t<0) t = 3;
            code = code * 7 + t;
        }
    }
    return code;
}

