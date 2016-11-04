#Just for testing
#SUCCESSFULLY TESTED OPERATIONS: 0,1,2,3,4,5,6,7,8,9
#CURRENLTY TESTING OPERATION 8
import Queue,time
from databaseThread import *
from threading import *
from matchMakingThread import *
import socket
from userThread import *

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

#This code tests the listener thread and the user threads
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
time.sleep(1)
clientSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM,0)
clientSocket.connect(('localhost',PORT))
time.sleep(1)
clientSocket.send('0 0 0 Lestrade bakerstreet@gmail.com olaole2')
time.sleep(1)
#Kills threads
#userThread
clientSocket.send('5')
clientSocket.shutdown(socket.SHUT_RDWR)
clientSocket.close()
time.sleep(1)
#databaseThread
answerQueue = Queue.Queue()
queueToDatabase.put({'operation':11,'answer':answerQueue})
time.sleep(1)
#matchMakingThread
exitQueue.put(True)
