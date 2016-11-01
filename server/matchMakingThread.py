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
import Queue

#inputQueue is for getting players from account threads
#outputQueue is for sending match tokes to the thread that handles the matches
def mmThread(inputQueue,outputQueue):
    noviceList = []
    apprenticeList = []
    adeptList = []
    expertList = []
    playerList = [noviceList,apprenticeList,adeptList,expertList]
    while True:
        #loop over new entries at most ten times then do it again
        while True:
            try:
                newPlayer = inputQueue.get(False)
                playerRank = newPlayer.get('rank')
                listIndex = playerRank // 3

            except:
                break
        for category in playerList:
            if len(category) >= 2:
                #create new match object
                matchToken =
