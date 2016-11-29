#This thread handles the actual games that are currently active
#It stores active games in a list and loops over them
#it only supports one operation, make move and the index of the move
#It communicates with the matchmaking threads and with the database thread,
#to store a new value of a players rank and wins or losses.
#Once the game play thread gets a token it sends both players a message to signal
#them that the game started.Again individual messages will be separated with a
#space between them
#                   COMMUNICATION PROTOCOL
# GAME START :                           0 turn TODO:Send name of other user
# SEND PLAYER MOVE AND NOTIFY :          1 move/validity state(0 = neutral,1=win,2=lose,3=tie)
#(The player whose turn it is gets a notification to see if his move was valid or not, the other player gets the move)
#Where 0 is valid, 1 is invalid
# NOTIFY FOR TIMEOUT (LOSER)             2
# NOTIFY FOR TIMEOUT (WINNER)            3

#MESSAGE FOR SERVER
# GET PLAYER MOVE                        move

import Queue,time,socket
from gameUtils import *
MAX_FINDS = 5
MAX_WAIT = 5

def gameThread(queueToMatchMaking,queueToDatabase,exitQueue):
    matchList=[]
    #game loop
    while True:
        #read from queue first to check for new matches
        foundCounter = 0
        while foundCounter < MAX_FINDS:
            try:
                newMatch = queueToMatchMaking.get(False)
                #found new pair
                foundCounter += 1
                #now send singal to threads
                firstPlayer = newMatch.get('players')[0]
                secondPlayer = newMatch.get('players')[1]
                firstSocket = firstPlayer.get('socket')
                secondSocket = secondPlayer.get('socket')
                turn = newMatch.get('turn')
                #Assign chip number for each player
                firstPlayer['chip'] = 1
                secondPlayer['chip'] = 2
                #Create a grid
                newMatch['grid'] = [[0,0,0,0,0,0,0],
                                    [0,0,0,0,0,0,0],
                                    [0,0,0,0,0,0,0],
                                    [0,0,0,0,0,0,0],
                                    [0,0,0,0,0,0,0],
                                    [0,0,0,0,0,0,0]]
                #Time stamp because each player has limited time for a move
                newMatch['time'] = time.time()
                #Send info to players
                firstSocket.send('0 ' + str(turn) + ' ' + secondPlayer.get('name') + ' \n')
                secondSocket.send('0 ' + str(turn) + ' ' + firstPlayer.get('name') + ' \n')
                matchList.append(newMatch)
                print '\n---- GAMEPLAY THREAD ----\n'
            except Queue.Empty:
                break

        #Check for exit signal
        try:
            exit = exitQueue.get(False)
            break
        except Queue.Empty:
            pass

        #Update matches
        for match in matchList[:]:
            turn = match.get('turn')
            readSocket = match.get('players')[turn].get('socket')
            readSocket.settimeout(0.0001)
            try:
                move = int(readSocket.recv(512))

                #Make move and check for win
                result =  makeMove(grid,move,match.get('players')[turn].get('chip'))

                #Invalid move
                if not result :
                    readSocket.send('1 1 \n')
                #Valid move
                else:
                    win = hasWon(grid,match.get('players')[turn].get('chip'))
                    if win:
                        state1 = '1'
                        state2 = '2'
                        winner = match.get('players')[turn]
                        loser = match.get('players')[(turn + 1) % 2]
                        updateStats(winner,loser,queueToDatabase)
                    elif isTie(grid):
                        state1 = state2 = '3'
                    else:
                        state1 = state2 = '0'
                        readSocket.send('1 0 state1 \n')
                    match['time'] = time.time()
                    match['turn'] = (match['turn'] + 1 ) % 2
                    turn = match['turn']
                    sendSocket = match.get('players')[turn].get('socket')
                    dataToSend = '1 ' + str(move) + ' state2 \n'
                    sendSocket.send(dataToSend)

            except socket.timeout:
                startTime = match['time']
                if time.time() - startTime > MAX_WAIT:

                    #player lost because no move
                    readSocket.send('2 \n')
                    loser = match.get('players')[turn]
                    turn = (turn + 1) % 2
                    winSocket = match.get('players')[turn].get('socket')
                    winSocket.send('3 \n')

                    #update player stats
                    winner = match.get('players')[turn]
                    updateStats(winner,loser,queueToDatabase)
                    matchList.remove(match)
