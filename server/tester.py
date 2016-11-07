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

"""#this code tests the database thread
requestQueue = Queue.Queue()
answerQueue = Queue.Queue()
myThread = Thread(target=dbThread,args=(requestQueue,))
myThread.start()
#First create a user
data = {'operation':1,'answer':answerQueue,'name':'Watson','email':'fail@gmail.com','password':'elapre','id':'yeper'}
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

"""#This code tests the whole server code
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

time.sleep(1)
clientSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM,0)
clientSocket.connect(('localhost',PORT))
secondSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM,0)
secondSocket.connect(('localhost',PORT))
time.sleep(1)

clientSocket.send('0 0 0 Lestrade bakerstreet@gmail.com olaole2')
answer = clientSocket.recv(512)
print 'GOT ANSWER client ' + answer
clientSocket.send('5')

secondSocket.send('0 0 0 PenkaVegito penkamanstreet@gmail.com elpuentos')
answer = secondSocket.recv(512)
print 'GOT ANSWER second ' + answer
secondSocket.send('5')
time.sleep(1)
#look for answer
answer = clientSocket.recv(512)
print 'Answer for clientSocket ' + str(answer)
answer = clientSocket.recv(512)
print 'Answer for clientSocket ' + str(answer)
answer = secondSocket.recv(512)
print 'Answer for secondSocket ' + str(answer)
answer = secondSocket.recv(512)
print 'Answer for secondSocket ' + str(answer)
#Kills threads
#userThread
time.sleep(1)
#databaseThread
answerQueue = Queue.Queue()
queueToDatabase.put({'operation':11,'answer':answerQueue})
time.sleep(1)
#matchMakingThread
exitQueue.put(True)
exitQueue2.put(True)
time.sleep(1)"""


#This code tests the utilities for the gameplay thread
board = [[0,0,0,0,0,0,0],
         [0,0,0,0,0,0,0],
         [0,0,0,0,0,0,0],
         [0,0,0,0,0,0,0],
         [0,0,0,0,0,0,0],
         [0,0,0,0,0,0,0]]

result = hasWon(board,1)
if result:
    print 'success'
else:
    print 'fail'
