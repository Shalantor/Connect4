#This file contains utility functions for the game play thread

#Check if given user has won
def hasWon(board,chip):
    #check horizontal
    for row in range(0,6):
        for col in range(0,4):
            if board[row][col] == chip and board[row][col+1] == chip and board[row][col+2] == chip and board[row][col+3] == chip:
                return True
    #check vertical
    for col in range(0,6):
        for row in range(0,4):
            if board[row][col] == chip and board[row][col+1] == chip and board[row][col+2] == chip and board[row][col+3] == chip:
                return True
    #check / diagonal
    for row in range(0,4):
        for col in range(3, 6):
            if board[row][col] == chip and board[row+1][col-1] == chip and board[row+2][col-2] == chip and board[row+3][col-3] == chip:
                return True
    #check \ diagonal
    for row in range(0,4):
        for col in range(0,3):
            if board[row][col] == chip and board[row+1][col+1] == chip and board[row+2][col+2] == chip and board[row+3][col+3] == chip:
                return True
    return False
