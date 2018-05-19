//
//  Train.cpp
//  ReversiClient-mac
//
//  Created by ganjun on 2018/5/18.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Train.h"

Train::Train(){}


void Train::readModel(char *model_name){
    FILE * fr = fopen(model_name, "r");
    char buffer[1000];
    black_win_rate.clear();
    white_win_rate.clear();
    cout<<"----------------------------"<<model_name<<endl;
    uint64_t status = 0;
    int win_count = 0 , all_count = 0;
    char color[3];
    while(fscanf(fr, "%s",color)>=0){
        // buffer: b,32134,13,24   color,status,win_count,all_count
//        cout<<"here: "<<buffer<<endl;
//
//
//        int index = 2;
//        while(buffer[index] >= '0' && buffer[index] <= '9'){
//            status = status*10+(buffer[index]-'0');
//            index++;
//        }
//        index++;
//        while(buffer[index] >= '0' && buffer[index] <= '9'){
//            win_count = win_count*10+(buffer[index]-'0');
//            index++;
//        }
//        index++;
//        while(buffer[index] >= '0' && buffer[index] <= '9'){
//            all_count = all_count*10+(buffer[index]-'0');
//            index++;
//        }
        
        cout<<color[0]<<","<<status<<","<<win_count<<","<<all_count<<endl;
        if(color[0] == 'b') black_win_rate[status] = make_pair(win_count, all_count);
        else white_win_rate[status] = make_pair(win_count, all_count);
    }
}

void Train::writeModel(char *model_name){
    FILE * fw = fopen(model_name , "w");
//    freopen(model_name, "w", stdout);
    printf("model_name: %s\n", model_name);
    // write black
    for(map<uint64_t, pii>::iterator it=black_win_rate.begin() ; it!=black_win_rate.end() ; it++){
        fprintf(fw, "b,%lu,%d,%d\n", (*it).first, (*it).second.first , (*it).second.second);
        printf("b,%lu,%d,%d\n", (*it).first, (*it).second.first , (*it).second.second);
    }
    // write white
    for(map<uint64_t, pii>::iterator it=white_win_rate.begin() ; it!=white_win_rate.end() ; it++){
        fprintf(fw, "w,%lu,%d,%d\n", (*it).first, (*it).second.first , (*it).second.second);
        printf("w,%lu,%d,%d\n", (*it).first, (*it).second.first , (*it).second.second);
    }
}

void Train::train(int rounds){
    for(int i=0 ; i<rounds ; i++){
        printf("start train %d round ...\n" , i+1);
        trainOneRound();
        printf("finish train %d round\n" , i+1);
    }
}

void Train::trainOneRound(){
    black_status.clear();
    white_status.clear();
    Board board;
    board.initBoard();
    while(board.isEnd() == false){
        // next black place
        black_status.push_back(board.encode());
        pii black_step = board.step(0);
        board.excuteStep(black_step.first, black_step.second, 0);
        
        // next white place
        white_status.push_back(board.encode());
        pii white_step = board.step(1);
        board.excuteStep(white_step.first, white_step.second, 1);
        
//        printf("debug step 2\n");
    }
    int black_more_cnt = board.evaluate(0);
    if(black_more_cnt < 0) winner = 1;
    else winner = 0;
    
    // update map
    updateWinRateOfOneRound();
}

void Train::updateWinRateOfOneRound(){
    // update black
    for(int i=0 ; i<black_status.size() ; i++){
        if(black_win_rate.find( black_status[i] ) == black_win_rate.end() ){
            black_win_rate[black_status[i]] = make_pair(0, 0);
        }
        pii temp = black_win_rate[black_status[i]];
        black_win_rate[black_status[i]] = make_pair(temp.first + (winner==0), temp.second+1);
    }
    
    // update white
    for(int i=0 ; i<white_status.size() ; i++){
        if(white_win_rate.find( white_status[i] ) == white_win_rate.end() ){
            white_win_rate[white_status[i]] = make_pair(0, 0);
        }
        pii temp = white_win_rate[white_status[i]];
        white_win_rate[white_status[i]] = make_pair(temp.first + (winner==1), temp.second+1);
    }
}
