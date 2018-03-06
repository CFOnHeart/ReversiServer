//
//  ClientSocket.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "ClientSocket.h"




#pragma comment(lib, "Ws2_32.lib")

ClientSocket::ClientSocket()
{
    
}

ClientSocket::~ClientSocket()
{
    close();
}

/**    connect to the server
 *    return 0 means that connect to the server successfully
 *    return >0 means that an error happened.
 */
int ClientSocket::connectServer()
{
    std::cout << "Connect server: " << SERVER_IP << ":" << SERVER_PORT << std::endl;
    int rtn = 0, err = 0;
    
    
    // creat socket
    clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == -1)
    {
        std::cout << "Socket failed with error: " << std::endl;
        rtn = 2;
        return rtn;
    }
    // server address
    memset(&server, 0, sizeof(sockaddr_in));
    server.sin_family = PF_INET;
    server.sin_port = htons(SERVER_PORT);
    server.sin_addr.s_addr = inet_addr(SERVER_IP);
    // connect
    err = connect(clientSocket, (struct sockaddr *) &server, sizeof(sockaddr_in));
    if (err < 0)
    {
        std::cout << "Connect failed with error: " << err << std::endl;
        // printf("%d\n", WSAGetLastError);
        rtn = 3;
        return rtn;
    }
    return rtn;
}

/*    send message to the server
 *    input the message that you want to send to the message
 *    return 0 means that send message successfully
 *    return 1 means that an error happened
 */
int ClientSocket::sendMsg(const char *msg)
{
    int rtn = 0 , err = 0;
    int len = strlen(msg);
    len = len < BUFSIZE ? len : BUFSIZE;
    memset(sendBuf, 0, BUFSIZE);
    memset(sendBuf, 0, sizeof(sendBuf));
    memcpy(sendBuf, msg, len);
    err = send(clientSocket, sendBuf, BUFSIZE, 0);
    if (err < 0)
    {
        std::cout << "Send msg failed with error: " << err << std::endl;
        rtn = 1;
        return rtn;
    }
    return rtn;
}
/*    receive message from the server
 *    return 0 means that receives message successfully
 *    return 1 means that an error happened
 */
int ClientSocket::recvMsg()
{
    int rtn = 0, err = 0;
    memset(recvBuf, 0, BUFSIZE);
    err = recv(clientSocket, recvBuf, BUFSIZE, 0);
    if (err < 0)
    {
        std::cout << "Receive msg failed with error: " << err << std::endl;
        rtn = 1;
        return rtn;
    }
    return rtn;
}

//get the received message
char* ClientSocket::getRecvMsg(){
    return this->recvBuf;
}

/*close client socket*/
void ClientSocket::close()
{
    // close socket
    shutdown(clientSocket, SHUT_RDWR);
    std::cout << "Close socket" << std::endl;
}

