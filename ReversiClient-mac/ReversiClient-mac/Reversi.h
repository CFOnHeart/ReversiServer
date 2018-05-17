//
//  Reversi.h
//  ReversiClient
//
//  Created by ganjun on 2018/3/6.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Reversi_h
#define Reversi_h
#include <stdio.h>
#include "ClientSocket.h"
#include "Board.h"

using namespace std;

class Reversi{
private:
    ClientSocket client_socket;
    int ownColor;
    int oppositeColor;
    char lastmsg[16];
public:
    Reversi();
    ~Reversi();
    
    int authorize(const char *id , const char *pass);
    
    void gameStart();
    
    void gameOver();
    
    void roundStart(int round);
    
    void oneRound();
    
    void roundOver(int round);
    
    int observe();
    
    void putDown(int row , int col);
    
    // according to chessman position (row , col) , generate one step message in order to send to server
    void generateOneStepMessage(int row , int col);
    
    void noStep();
    
    pair<int,int> step();
    
    void saveChessBoard();
    
    void debug_lastmsg();
    
    void handleMessage(int row, int col, int color);
    
    void initChessBoard();
};

#endif /* Reversi_h */
