#ifndef Square_h
#define Square_h
#pragma once
#include <string>
/* a square in the board
 *    in the position (x, y)
 *    color = -1: empty
 *    color = 0: a black chessman is on this square
 *    color = 1; a white chessman is on this square
 *    color = 2; prohibition on this square this turn.
 */

class Square
{
private:
    int x, y;
    int color;
public:
    Square(int color = -1);
    ~Square(void);
    
    //if this square is prohibtion last turn, cancel the prohibtion
    void cancelProhibition();
    
    //if this square is empty, set prohibtion
    void setProhibition();
    
    /*lazi at this square
     * return true means that this square is empty
     * return false means that this square is not empty
     */
    bool lazi(int color);
    
    //reversi the chessman
    inline void reversi(){
        if(color == 0 || color == 1){
            color = 1 - color;
        }
    }
    //set the square empty
    void clear();
    
    /* compare this->color to another chessman color
     * return 0 represents colors are the same
     * return 1 represents colors are opposite
     * return -1 represents a color is empty or prohibition
     */
    int compareColor(int color);
    
    //is this square empty
    inline bool isEmpty(){
        return (color == -1);
    }
    //is this square prohibited
    inline bool isProhibition(){
        return (color == 2);
    }
    
    //get the position x;
    inline int getX(){
        return this->x;
    }
    //get the position y;
    inline int getY(){
        return this->y;
    }
    // set the color
    inline void setColor(int color){
        this->color = color;
    }
    
    void print(std::string& str);
};

#endif
