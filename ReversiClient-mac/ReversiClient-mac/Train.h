//
//  Train.h
//  ReversiClient-mac
//
//  Created by ganjun on 2018/5/18.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Train_h
#define Train_h
#include <vector>
#include <map>
#include "Board.h"
#include "stdio.h"
#include <iostream>
using namespace std;
#define pii pair<int,int>
class Train{
public:
    vector<uint64_t> black_status;
    vector<uint64_t> white_status;
    map<uint64_t, pii> black_win_rate; // chessboard status -> black win rate(pii.first: win count, pii.second: all count)
    map<uint64_t, pii> white_win_rate; // chessboard status -> white win rate(pii.first: win count, pii.second: all count)
    int winner; // winner = 0 , black win , winner = 1 , white win
    Train();
    
    void readModel(char * model_name);
    
    void writeModel(char * model_name);
    
    void train(int rounds);
    
    void trainOneRound();
    
    void updateWinRateOfOneRound();
};
#endif /* Train_h */
