//
//  Reversi.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/6.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Reversi.h"

#define random(x) (rand()%x)
#define ROWS 8
#define COLS 8
#define ROUNDS 2

Reversi::Reversi(){
    client_socket = ClientSocket();
    board = Board();
    oppositeColor = ownColor = -1;
    memset(lastmsg , 0 , sizeof(lastmsg));
}

Reversi::~Reversi(){};

/*
 send id and password to server by socket
 rtn != 0 represents socket transfer error
 */
void Reversi::authorize(const char *id , const char *pass)
{
    client_socket.connectServer();
    std::cout << "Authorize " << id << std::endl;
    char msgBuf[BUFSIZE];
    memset(msgBuf , 0 , BUFSIZE);
    msgBuf[0] = 'A';
    memcpy(&msgBuf[1] , id , 9);
    memcpy(&msgBuf[10] , pass , 6);
    int rtn = client_socket.sendMsg(msgBuf);
    if (rtn != 0) printf("Authorized Failed!\n");
}

// 用户id输入，服务器上需要有对应的账号密码：对应文件 players-0.txt
void Reversi::gameStart()
{
    char id[12] , passwd[10];
    printf("ID: \n");
    scanf("%s" , id);
    printf("PASSWD: \n");
    scanf("%s", passwd);
    
    authorize(id, passwd);
    
    printf("Game Start!\n");
    
    for (int round = 0 ; round < ROUNDS ; round++){
        roundStart(round);
        oneRound();
        roundOver(round);
    }
    gameOver();
    client_socket.close();
}

void Reversi::gameOver()
{
    printf("Game Over!\n");
}

// 发一次消息，走哪一步，等两个消息，1.自己的步数行不行 2. 对面走了哪一步
void Reversi::roundStart(int round)
{
    printf("Round %d Ready Start!\n" , round);
    for (int r=0 ; r<ROWS ; r++){
        for (int c=0 ; c<COLS ; c++){
            board.resetBoard();
        }
    }
    
    memset(lastmsg , 0 , sizeof(lastmsg));
    // first time receive msg from server
    int rtn = client_socket.recvMsg();
    if (rtn != 0) return ;
    if(strlen(client_socket.getRecvMsg()) < 2)
        printf("Authorize Failed!\n");
    else
        printf("Round start received msg %s\n", client_socket.getRecvMsg());
    switch (client_socket.getRecvMsg()[1]) {
            // this client : black chessman
        case 'B':
            ownColor = 0;
            oppositeColor = 1;
            rtn = client_socket.sendMsg("BB");
            printf("Send BB -> rtn: %d\n", rtn);
            if (rtn != 0) return ;
            break;
        case 'W':
            ownColor = 1;
            oppositeColor = 0;
            rtn = client_socket.sendMsg("BW");
            printf("Send BW -> rtn: %d\n", rtn);
            if (rtn != 0) return ;
            break;
        default:
            printf("Authorized Failed!\n");
            break;
    }
}

void Reversi::oneRound()
{
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

void Reversi::roundOver(int round)
{
    printf("Round %d Over!\n", round);
    // reset initializer
    board.resetBoard();
    ownColor = oppositeColor = -1;
}

int Reversi::observe()
{
    int rtn = 0;
    int recvrtn = client_socket.recvMsg();
    if (recvrtn != 0) return 1;
    printf("receive msg %s\n", client_socket.getRecvMsg());
    switch (client_socket.getRecvMsg()[0]) {
        case 'R':
        {
            switch (client_socket.getRecvMsg()[1]) {
                case '0':   // valid step
                    switch (client_socket.getRecvMsg()[2]) {
                        case 'P':   // update chessboard
                        {
                            int desRow = (client_socket.getRecvMsg()[3] - '0') * 10 + client_socket.getRecvMsg()[4] - '0';
                            int desCol = (client_socket.getRecvMsg()[5] - '0') * 10 + client_socket.getRecvMsg()[6] - '0';
                            board.lazi(desRow, desCol, client_socket.getRecvMsg()[7] - '0');
                            memcpy(lastmsg, client_socket.getRecvMsg(), strlen(client_socket.getRecvMsg()));
                            break;
                        }
                        case 'D':   // Disappeared
                        {
                            int desRow = (client_socket.getRecvMsg()[3] - '0') * 10 + client_socket.getRecvMsg()[4] - '0';
                            int desCol = (client_socket.getRecvMsg()[5] - '0') * 10 + client_socket.getRecvMsg()[6] - '0';
                           board.lazi(desRow, desCol, client_socket.getRecvMsg()[7] - '0');
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
                    printf("Error -1: Msg format error!\n");
                    rtn = -1;
                    break;
                case '2':
                    printf("Error -2: Corrdinate error!\n");
                    rtn = -2;
                    break;
                case '4':
                    printf("Error -4: Invalid step!\n");
                    rtn = -4;
                    break;
                default:
                    printf("Error -5: Other error!\n");
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
        default:
            break;
    }
    return rtn;
}

void Reversi::putDown(int row, int col)
{
    char msg[6];
    memset(msg , 0 , sizeof(msg));
    msg[0] = 'S';
    msg[1] = 'P';
    msg[2] = '0' + row / 10;  // 之后可以删掉，在这个期盼中始终为0
    msg[3] = '0' + row % 10;
    msg[4] = '0' + col / 10;  // 之后可以删掉，在这个期盼中始终为0
    msg[5] = '0' + col % 10;
    
    board.lazi(row, col, ownColor);
    debug_lastmsg();
    printf("put down (%c%c, %c%c)\n", msg[2], msg[3], msg[4], msg[5]);
    debug_lastmsg();
    printf("put down (%c%c, %c%c)\n", msg[2] , msg[3] , msg[4] , msg[5]);
    client_socket.sendMsg(msg);
}

void Reversi::noStep()
{
    client_socket.sendMsg("SN");
    printf("send msg %s\n" , "SN");
}

void Reversi::step()
{
    int r = -1, c = -1;
    // printf("%s\n", lastMsg());
    while (!(r >= 0 && r < ROWS && c >= 0 && c < COLS && board.canLazi(r, c, ownColor))) {
        r = random(8);
        c = random(8);
        // System.out.println("Rand " + r + " " + c);
    }
    // saveChessBoard();
    putDown(r, c);
}

void Reversi::saveChessBoard()
{
    
}

void Reversi::debug_lastmsg()
{
    printf("%s\n" , lastmsg);
}


