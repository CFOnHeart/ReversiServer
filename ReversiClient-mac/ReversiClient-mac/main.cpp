//
//  main.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Reversi.h"
#include "Train.h"

void train()
{
    Train train = Train();
    train.train(1);
    char * model_name = new char[20];
    strcpy(model_name , "model/1.txt");
    train.writeModel(model_name);
    train.readModel(model_name);
}

int main() {
    Reversi reversi = Reversi();
    reversi.gameStart();
//    train();
    return 0;
}


