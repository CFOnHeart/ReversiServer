//
//  Reversi.h
//  ReversiClient
//
//  Created by ganjun on 2018/3/6.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Reversi_h
#define Reversi_h

class Reversi{
public:
    Reversi();
    ~Reversi();
    
    void authorize(const char *id , const char *pass);
    
    void gameStart();
    
    void gameOver();
    
    void roundStart(int round);
    
    void oneRound();
    
    void rundOver(int round);
    
    int observe();
    
    void putDown(int row , int col);
    
    void noStep();
    
    void step();
    
    void saveChessBoard();
};

#endif /* Reversi_h */
