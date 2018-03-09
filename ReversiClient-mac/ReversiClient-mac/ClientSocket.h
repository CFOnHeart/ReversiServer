//
//  ClientSocket.h
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#ifndef ClientSocket_h
#define ClientSocket_h

#pragma once
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <iostream>
#include <stdio.h>
#include <string>
#include "Define.h"
#define BUFSIZE    16

class ClientSocket
{
private:
    //server address
    sockaddr_in server;
    
    //client SOCKET that connect to the server
    int clientSocket;
    
    //the message buffer that is received from the server
    char recvBuf[BUFSIZE];
    
    //the message buffer that is sent to the server
    char sendBuf[BUFSIZE];
    
public:
    ClientSocket();
    ~ClientSocket(void);
    
    //connect to the server
    int connectServer();
    
    //receive message from server
    int recvMsg();
    
    //send message to server
    int sendMsg(const char* msg);
    
    //close socket
    void close();
    
    
    //get the received message
    char* getRecvMsg();
};

#endif /* ClientSocket_h */
