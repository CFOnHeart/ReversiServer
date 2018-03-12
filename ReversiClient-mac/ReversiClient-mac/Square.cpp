#include "Square.h"


Square::Square(int color)
{
    this->color = color;
}

Square::~Square(void)
{
}

//if this square is prohibtion last turn, cancel the prohibtion
void Square::cancelProhibition(){
    if(color == 2){
        color = -1;
    }
}

//if this square is empty, set prohibition
void Square::setProhibition(){
    if(color == -1){
        color = 2;
    }
}

/*lazi at this square
 * return true means that this square is empty
 * return false means that this square is not empty
 */
bool Square::lazi(int color){
    if(this->color == -1){
        this->color = color;
        return true;
    }
    return false;
}

//set the square empty
void Square::clear(){
    this->color = -1;
}

/* compare this->color to another chessman color
 * return 0 represents colors are the same
 * return 1 represents colors are opposite
 * return -1 represents a color is empty or prohibition
 */
int Square::compareColor(int color){
    //this->color can compare with color
    if( (color == 0 || color == 1) && (this->color == 0 || this->color == 1)){
        if(this->color == color){
            return 0;
        }
        else{
            return 1;
        }
    }
    return -1;
}


void Square::print(std::string& str){
    switch(color){
        case -1:
            str += '-';
            break;
        case 0:
            str += 'B';
            break;
        case 1:
            str += 'W';
            break;
        case 2:
            str += 'P';
            break;
        default:
            str += '-';
            break;
            
    }
}
