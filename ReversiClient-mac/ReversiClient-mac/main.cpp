//
//  main.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//
//
#include "Reversi.h"

int main() {
    Reversi reversi = Reversi();
    reversi.gameStart();
    return 0;
}

//#include <stdio.h>
//#include <string.h>
//
//int main()
//{
//    char str[100][100] , c[100] , index = 0;
//    while(scanf("%s" , c) && strcmp(c, "XXXXXX") !=0){
//        strcpy(str[index++] , c);
//    }
//    for(int i=0 ; i<index ; i++)
//        printf("%s\n" , str[i]);
//}

