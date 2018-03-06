//
//  Gobang.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Gobang.h"

#define random(x) (rand()%x)
#define ROWS 8
#define COLS 8
#define ROUNDS 2

ClientSocket client_socket = ClientSocket();
Board board = Board();

int ownColor = -1, oppositeColor = -1;
char lastmsg[16] = { "" };

void authorize(const char *id, const char *pass) {
    client_socket.connectServer();
    std::cout << "Authorize " << id << std::endl;
    char msgBuf[BUFSIZE];
    memset(msgBuf, 0, BUFSIZE);
    msgBuf[0] = 'A';
    memcpy(&msgBuf[1], id, 9);
    memcpy(&msgBuf[10], pass, 6);
    int rtn = client_socket.sendMsg(msgBuf);
    // printf("Authorize Return %d\n", rtn);
    if (rtn != 0) printf("Authorized Failed\n");
}

// 用户id输入，服务器上需要有对应的账号密码 players-0.list
void gameStart() {
    char id[12], passwd[10];
    std::cout << "ID: " << std::endl;
    std::cin >> id;
    std::cout << "PASSWD: " << std::endl;
    std::cin >> passwd;
    authorize(id, passwd);
    
    std::cout << "Game Start" << std::endl;
    for (int round = 0; round < ROUNDS; round++) {
        roundStart(round);
        oneRound();
        roundOver(round);
    }
    gameOver();
    client_socket.close();
}

void gameOver() {
    std::cout << "Game Over" << std::endl;
}

// 发一次消息，走哪一步，等两个消息，1.自己的步数行不行 2. 对面走了哪一步
void roundStart(int round) {
    std::cout << "Round " << round << " Ready Start" << std::endl;
    for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
            board.resetBoard();
        }
    }
    memset(lastmsg, 0, sizeof(lastmsg));
    int rtn = client_socket.recvMsg();
    if (rtn != 0) return;
    if (strlen(client_socket.getRecvMsg()) < 2)
        printf("Authorize Failed\n");
    else
        printf("Round start received msg %s\n", client_socket.getRecvMsg());
    switch (client_socket.getRecvMsg()[1]) {
        case 'B':
            ownColor = 0;
            oppositeColor = 1;
            rtn = client_socket.sendMsg("BB");
            if (rtn != 0) return;
            break;
        case 'W':
            ownColor = 1;
            oppositeColor = 0;
            rtn = client_socket.sendMsg("BW");
            std::cout << "Send BW" << rtn << std::endl;
            if (rtn != 0) return;
            break;
        default:
            printf("Authorized Failed\n");
            break;
    }
}

void oneRound() {
    int DIS_FREQ = 5, STEP = 1;
    switch (ownColor) {
        case 0:
            while (STEP < 10000) {
                
                if (STEP != 1 && (STEP-1) % DIS_FREQ == 0) {
                    int ret = observe();       // self disappeared
                    if (ret >= 1) break;
                    else if (ret != -8) {
                        std::cout << "ERROR: Not Self(BLACK) Disappeared" << std::endl;
                    }
                }
                step();                        // take action, send message
                
                if (observe() >= 1) break;     // receive RET Code
                // saveChessBoard();
                if (STEP != 1 && (STEP-1) % DIS_FREQ == 0) {
                    int ret = observe();       // see white disappear
                    if (ret >= 1) break;
                    else if (ret != -9) {
                        std::cout << ret << " ERROR: Not White Disappeared" << std::endl;
                    }
                }
                
                if (observe() >= 1) break;    // see white move
                STEP++;
                // saveChessBoard();
            }
            printf("One Round End\n");
            break;
        case 1:
            while (STEP < 10000) {
                
                if (STEP != 1 && (STEP-1) % DIS_FREQ == 0) {
                    int ret = observe();       // black disappeared
                    if (ret >= 1) break;
                    else if (ret != -8) {
                        std::cout << "ERROR: Not Black Disappeared" << std::endl;
                    }
                }
                if (observe() >= 1) break;    // see black move
                
                if (STEP != 1 && (STEP-1) % DIS_FREQ == 0) {
                    int ret = observe();      // self disappeared
                    if (ret >= 1) break;
                    else if (ret != -9) {
                        std::cout << "ERROR: Not Self Disappeared" << std::endl;
                    }
                }
                
                step();                        // take action, send message
                if (observe() >= 1) break;     // receive RET Code
                // saveChessBoard();
                STEP++;
            }
            printf("One Round End\n");
            break;
        default:
            break;
    }
}

void roundOver(int round) {
    std::cout << "Round " << round << " Over" << std::endl;
    for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
            board.resetBoard();
        }
    }
    ownColor = oppositeColor = -1;
}

void lastMsg() {
    printf(lastmsg);
    puts("");
}

int observe() {
    int rtn = 0;
    int recvrtn = client_socket.recvMsg();
    if (recvrtn != 0) return 1;
    printf("receive msg %s\n", client_socket.getRecvMsg());
    switch (client_socket.getRecvMsg()[0]) {
        case 'R':   // return messages
        {
            switch (client_socket.getRecvMsg()[1]) {
                case '0':    // valid step
                    switch (client_socket.getRecvMsg()[2]) {
                        case 'P':   // update chessboard
                        {
                            int desRow = (client_socket.getRecvMsg()[3] - '0') * 10 + client_socket.getRecvMsg()[4] - '0';
                            int desCol = (client_socket.getRecvMsg()[5] - '0') * 10 + client_socket.getRecvMsg()[6] - '0';
                            board.updateColor(desRow, desCol, client_socket.getRecvMsg()[7] - '0');
                            memcpy(lastmsg, client_socket.getRecvMsg(), strlen(client_socket.getRecvMsg()));
                            break;
                        }
                        case 'D':   // Disappeared
                        {
                            int desRow = (client_socket.getRecvMsg()[3] - '0') * 10 + client_socket.getRecvMsg()[4] - '0';
                            int desCol = (client_socket.getRecvMsg()[5] - '0') * 10 + client_socket.getRecvMsg()[6] - '0';
                            board.updateColor(desRow, desCol, -1);
                            if (client_socket.getRecvMsg()[7] - '0' == 0)  // black disappear
                                rtn = -8;
                            else
                                rtn = -9;
                            memcpy(lastmsg, client_socket.getRecvMsg(), strlen(client_socket.getRecvMsg()));
                            break;
                        }
                        case 'N':   // R0N: enemy wrong step
                        {
                            break;
                        }
                    }
                    break;
                case '1':
                    std::cout << "Error -1: Msg format error\n";
                    rtn = -1;
                    break;
                case '2':
                    std::cout << "Error -2: Coordinate error\n";
                    rtn = -2;
                    break;
                case '4':
                    std::cout << "Error -4: Invalid step\n";
                    rtn = -4;
                    break;
                default:
                    std::cout << "Error -5: Other error\n";
                    rtn = -5;
                    break;
            }
            break;
        }
        case 'E':
        {
            switch (client_socket.getRecvMsg()[1]) {
                case '0':
                    // game over
                    rtn = 2;
                    break;
                case '1':
                    // round over
                    rtn = 1;
                default:
                    break;
            }
            break;
        }
    }
    return rtn;
}

void putDown(int row, int col) {
    char msg[6];
    memset(msg, 0, sizeof(msg));
    msg[0] = 'S';
    msg[1] = 'P';
    msg[2] = '0' + row / 10;
    msg[3] = '0' + row % 10;
    msg[4] = '0' + col / 10;
    msg[5] = '0' + col % 10;
    board.updateColor(row, col , ownColor);
    lastMsg();
    printf("put down (%c%c, %c%c)\n", msg[2], msg[3], msg[4], msg[5]);
    client_socket.sendMsg(msg);
}

void noStep() {
    client_socket.sendMsg("SN");
    printf("send msg %s\n", "SN");
}

// 本地保存，回放
void saveChessBoard() {
    
}

// 主要逻辑部分
void step() {
    
    int r = -1, c = -1;
    // printf("%s\n", lastMsg());
    while (!(r >= 0 && r < ROWS && c >= 0 && c < COLS && board.getSquare(r, c).isEmpty())) {
        r = random(8);
        c = random(8);
        // System.out.println("Rand " + r + " " + c);
    }
    // saveChessBoard();
    putDown(r, c);
    // noStep();
}


