#This file contains utility functions for the game play thread
import Queue

#Check if given user has won
def hasWon(board,chip):
    #check horizontal
    for row in range(0,6):
        for col in range(0,4):
            if board[row][col] == chip and board[row][col+1] == chip and board[row][col+2] == chip and board[row][col+3] == chip:
                return True
    #check vertical
    for col in range(0,7):
        for row in range(0,3):
            if board[row][col] == chip and board[row+1][col] == chip and board[row+2][col] == chip and board[row+3][col] == chip:
                return True
    #check / diagonal
    for row in range(3,6):
        for col in range(0, 4):
            if board[row][col] == chip and board[row-1][col+1] == chip and board[row-2][col+2] == chip and board[row-3][col+3] == chip:
                return True
    #check \ diagonal
    for row in range(0,3):
        for col in range(0,4):
            if board[row][col] == chip and board[row+1][col+1] == chip and board[row+2][col+2] == chip and board[row+3][col+3] == chip:
                return True
    return False


#Function to add chip to board
def makeMove(board,position,chip):
    #loop rows upwards
    for i in range(5,-1,-1):
        if board[i][position] == 0:
            board[i][position] = chip
            return True
    return False

#Function to check for tie
def isTie(board):
    for row in range(0,6):
        for col in range(0,7):
            if board[row][col] != 0:
                return False
    return True

#Function to update player stats
def updateStats(winner,loser,queueToDatabase):

    answerQueue = Queue.Queue()
    winnerType = winner.get('type')

    #Normal user
    if winnerType == '0':
        queueToDatabase.put({'operation':4,'answer':answerQueue,'name':winner.get('name'),'win':1,'loss':0})
    #Fb user
    else:
        queueToDatabase.put({'operation':5,'answer':answerQueue,'id':winner.get('id'),'win':1,'loss':0})

    #read answer
    answer = answerQueue.get()

    loserType = loser.get('type')

    #Normal user
    if loserType == '0':
        queueToDatabase.put({'operation':4,'answer':answerQueue,'name':loser.get('name'),'win':1,'loss':0})
    #Fb user
    else:
        queueToDatabase.put({'operation':5,'answer':answerQueue,'id':loser.get('id'),'win':1,'loss':0})

    #read answer
    answer = answerQueue.get()
