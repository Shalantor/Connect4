#This thread handles the actual games that are currently active
#It stores active games in a list and loops over them
#it only supports one operation, make move and the index of the move
#It communicates with the matchmaking threads and with the database thread,
#to store a new value of a players rank and wins or losses.
#Once the game play thread gets a token it sends both players a message to signal
#them that the game started.Again individual messages will be separated with a
#space between them
#                   COMMUNICATION PROTOCOL FOR SERVER MESSAGES
# GAME START :                           0 turn opponent_name
# SEND PLAYER MOVE AND NOTIFY :          1 move/validity state(0 = neutral,1=win,2=lose,3=tie)
# NOTIFY FOR TIMEOUT (LOSER)             2
# NOTIFY FOR TIMEOUT (WINNER)            3

import Queue,time,socket
from gameUtils import *
from threading import *
from userThread import *
MAX_FINDS = 5
MAX_WAIT = 60

def gameThread(queueToMatchMaking,queueToDatabase,exitQueue,queueForUserThread):
    matchList=[]
    #game loop
    while True:
        #read from queue first to check for new matches
        #only read a certain amount of times, to not stall
        #players currently in game for too long
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

                #Check whose turn it is
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

                #Set player turns
                if turn == 0:
                    turn1 = '1'
                    turn2 = '0'
                else:
                    turn1 = '0'
                    turn2 = '1'

                #Send info to players
                firstSocket.send('0 ' + turn1 + ' ' + secondPlayer.get('name') + ' \n')
                secondSocket.send('0 ' + turn2 + ' ' + firstPlayer.get('name') + ' \n')
                matchList.append(newMatch)
                print '\n---- GAMEPLAY THREAD ----\n'
            except Queue.Empty:
                #If Queue is empty, break the loop and go to active matches
                break

        #Check for exit signal, to eventually stop running
        try:
            exit = exitQueue.get(False)
            break
        except Queue.Empty:
            pass

        #Update matches
        for match in matchList[:]:
            #See whose turn it is
            turn = match.get('turn')

            #Get socket to read the move from
            readSocket = match.get('players')[turn].get('socket')
            readSocket.settimeout(0.1)

            try:
                move = readSocket.recv(512)
                if len(move) == 0:
                    #player lost because he disconnected/terminated the app
                    #Send winning and losing messages to both players
                    readSocket.send('2 0 0 \n')
                    loser = match.get('players')[turn]
                    turn = (turn + 1) % 2
                    winSocket = match.get('players')[turn].get('socket')
                    winSocket.send('3 0 0 \n')

                    #update player stats
                    winner = match.get('players')[turn]
                    updateStats(winner,loser,queueToDatabase)

                    #Start new user threads
                    matchList.remove(match)
                    winSocket.shutdown(socket.SHUT_RDWR)
                    readSocket.shutdown(socket.SHUT_RDWR)
                    winSocket.close()
                    readSocket.close()
                    continue
                else:
                    #Got a valid move from player
                    move = int(move)
                    print 'GAME THREAD : GOT MOVE %d ' % move

                #Make move and check for win
                result =  makeMove(match.get('grid'),move,match.get('players')[turn].get('chip'))

                #Invalid move, like out of boudns for example, or a full column
                if not result:
                    readSocket.send('1 1 0 \n')
                #Valid move
                else:
                    #Check for win
                    win = hasWon(newMatch['grid'],match.get('players')[turn].get('chip'))

                    #Set states to send, depending on result of move
                    if win:
                        state1 = '1'
                        state2 = '2'
                        winner = match.get('players')[turn]
                        loser = match.get('players')[(turn + 1) % 2]
                        updateStats(winner,loser,queueToDatabase)
                    elif isTie(match.get('grid')):
                        state1 = state2 = '3'
                    else:
                        state1 = state2 = '0'

                    #Now send them to players
                    readSocket.send('1 0 ' + state1 + ' \n')

                    #Update time of last move
                    match['time'] = time.time()
                    match['turn'] = (match['turn'] + 1 ) % 2
                    turn = match['turn']
                    sendSocket = match.get('players')[turn].get('socket')

                    print 'Send data to player %s ' % match.get('players')[turn].get('name')

                    dataToSend = '1 ' + str(move) + ' ' + state2 + ' \n'
                    sendSocket.send(dataToSend)

                    #Remove from matchlist if game is over
                    #It doesnt matter which state to check for a tie ,
                    #because they are the same, so I chose state1
                    if win or state1 == '3':
                        #now start new threads for players
                        matchList.remove(match)
                        sendSocket.shutdown(socket.SHUT_RDWR)
                        readSocket.shutdown(socket.SHUT_RDWR)
                        sendSocket.close()
                        readSocket.close()

            except socket.timeout:
                #On time out, check if player has been idle for too long
                startTime = match['time']
                if time.time() - startTime > MAX_WAIT:

                    #player lost because he didnt make a move
                    #Send info to players
                    readSocket.send('2 0 0 \n')
                    loser = match.get('players')[turn]
                    turn = (turn + 1) % 2
                    winSocket = match.get('players')[turn].get('socket')
                    winSocket.send('3 0 0 \n')

                    #update player stats
                    winner = match.get('players')[turn]
                    updateStats(winner,loser,queueToDatabase)

                    #Start new user threads
                    matchList.remove(match)
                    winSocket.shutdown(socket.SHUT_RDWR)
                    readSocket.shutdown(socket.SHUT_RDWR)
                    winSocket.close()
                    readSocket.close()
