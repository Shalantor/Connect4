#This will be the thread responsible for the matchmaking which operates as follows:
#There are four lists where the players are divided into based on their rank.
#List 1 is for ranks 0,1,2.
#List 2 is for ranks 3,4,5.
#List 3 is for ranks 6,7,8.
#List 4 is for ranks 9,10.
#When a player waits for a match too long, this thread will start looking for
#players in adjacent lists, first in the higher category list and then in the
#lower one.
#Each player has a dictionary associated with him, which will store his info
#and some other parameters, like his network info to connect to him.
#This thread support only 2 operations:
# 1) Add to match making lists
# 2) Terminate itself
MAX_LOOPS = 10
MAX_WAIT = 1
import Queue,time,random

#inputQueue is for getting players from account threads
#outputQueue is for sending match tokens to the thread that handles the matches
#exitQueue is used for exiting the thread
def mmThread(inputQueue,exitQueue,outputQueue):
    noviceList = []
    apprenticeList = []
    adeptList = []
    expertList = []
    playerList = [noviceList,apprenticeList,adeptList,expertList]
    needRematch = []
    while True:
        loopCounter = 0
        try:
            exit = exitQueue.get(False)
            if exit:
                break
        except:
            pass
        #loop over new entries at most X times then do it again
        while loopCounter < MAX_LOOPS:
            try:
                newPlayer = inputQueue.get(False)
                playerRank = newPlayer.get('rank')
                listIndex = playerRank // 3
                newPlayer['entryTime'] = time.time()
                playerList[listIndex].append(newPlayer)
            except Queue.Empty:
                break
            loopCounter += 1
        #First check for previous players in a copy of list
        for player in needRematch[:]:
            position = player.get('rank') // 3
            foundMatch = False
            if len(playerList[position]) == 0 or playerList[position][0] != player:
                continue
            if position + 1 < len(playerList) and len(playerList[position+1]) >= 1:
                foundMatch = True
                firstPlayer = playerList[position].pop(0)
                secondPlayer = playerList[position+1].pop(0)
                needRematch.remove(player)
            elif (position - 1 >= 0) and len(playerList[position-1]) >= 1:
                foundMatch = True
                firstPlayer = playerList[position].pop(0)
                secondPlayer = playerList[position-1].pop(0)
                needRematch.remove(player)
            if foundMatch:
                bothPlayers = [firstPlayer,secondPlayer]
                data = {'turn':0,'players':bothPlayers}
                print'Add new Player token'
                outputQueue.put(data)

        #Match players in same list
        for category in playerList:
            while True:
                try:
                    firstPlayer = None
                    secondPlayer = None
                    firstPlayer = category.pop(0)
                    secondPlayer = category.pop(0)
                    bothPlayers = [firstPlayer,secondPlayer]
                    data = {'turn':0,'players':bothPlayers}
                    print'Add new Player token'
                    outputQueue.put(data)
                except:
                    if secondPlayer == None and firstPlayer != None:
                        category.insert(0,firstPlayer)
                    break
        #Check for players that didnt find a match and alert thread
        for i in range(0,3):
            if len(playerList[i]) > 0:
                if time.time() - playerList[i][0].get('entryTime') >= MAX_WAIT:
                    needRematch.append(playerList[i][0])
    print 'match making thread out'
