#Just for testing
#SUCCESSFULLY TESTED OPERATIONS: 0,1,2,3,4,5,6,7,8,9
#CURRENLTY TESTING OPERATION 8
import Queue,time
from databaseThread import *
from threading import *
from matchMakingThread import *
import socket
from userThread import *
from gameplayThread import *
from gameUtils import *

#this code tests the database thread
"""requestQueue = Queue.Queue()
answerQueue = Queue.Queue()
myThread = Thread(target=dbThread,args=(requestQueue,))
myThread.start()
#First create a user
data = {'operation':1,'answer':answerQueue,'name':'Watsonxcxc','email':'faixcxcl@gmail.com','password':'elapre','id':'yep1212er'}
requestQueue.put(data)
print 'waiting for answer'
result = answerQueue.get()
if result:
    print 'Success with this user'
else:
    print 'No success with this user'
data = {'operation':10,'answer':answerQueue,'name':'Watson','email':None,'id':'yeper'}
requestQueue.put(data)
result = answerQueue.get()
print result
data = {'operation':11,'answer':answerQueue,'name':'Watson','email':None,'id':'Watsid'}
requestQueue.put(data)
result = answerQueue.get()
if result:
    print 'thread exited normally'"""

#this code tests the match making thread
"""inputQueue = Queue.Queue()
outputQueue = Queue.Queue()
exitQueue = Queue.Queue()
myThread = Thread(target=mmThread,args=(inputQueue,exitQueue,outputQueue))
myThread.start()
inputQueue.put({'rank':0})
inputQueue.put({'rank':5})
time.sleep(4)
while True:
    try:
        token = outputQueue.get(False)
        print token.get('players')
    except:
        break
exitQueue.put(True)
print 'exit main'"""

#This code tests the whole server code
#Setup threads and queues
queueToDatabase = Queue.Queue()
queueToMatchMaking = Queue.Queue()
listenerThread = Thread(target=listener,args=(queueToDatabase,queueToMatchMaking))
listenerThread.start()
dataThread = Thread(target=dbThread,args=(queueToDatabase,))
dataThread.start()
exitQueue = Queue.Queue()
outputQueue = Queue.Queue()
matchMakingThread = Thread(target=mmThread,args=(queueToMatchMaking,exitQueue,outputQueue))
matchMakingThread.start()
exitQueue2 = Queue.Queue()
gThread = Thread(target=gameThread,args=(outputQueue,queueToDatabase,exitQueue2))
gThread.start()

example = raw_input('Press enter when you want to stop simulation')
#Kills threads
#databaseThread
answerQueue = Queue.Queue()
queueToDatabase.put({'operation':11,'answer':answerQueue})
time.sleep(1)
#matchMakingThread
exitQueue.put(True)
exitQueue2.put(True)


"""#This code tests the utilities for the gameplay thread
board = [[0,0,0,0,0,0,0],
         [0,0,0,0,0,0,1],
         [0,0,0,0,0,0,1],
         [0,0,0,0,0,0,1],
         [0,0,0,0,0,0,1],
         [0,0,0,0,0,0,1]]

result = makeMove(board,6,1)
if result:
    print 'move was ok'
else:
    print 'Invalid move'
print board"""
