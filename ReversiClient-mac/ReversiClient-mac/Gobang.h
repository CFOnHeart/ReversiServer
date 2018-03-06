//
//  Gobang.h
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef Gobang_h
#define Gobang_h

#pragma once
#include "ClientSocket.h"
#include "Board.h"
#include <iostream>
#include <stdlib.h>
#include <string>
#include <stdio.h>

void authorize(const char *id, const char *pass);

void gameStart();

void gameOver();

void roundStart(int round);

void oneRound();

void roundOver(int round);

int observe();

void putDown(int row, int col);

void noStep();

void step();

void saveChessBoard();

#endif /* Gobang_h */
