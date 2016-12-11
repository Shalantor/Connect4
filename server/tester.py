#This file sets up and runs the server.

import Queue,time
from databaseThread import *
from threading import *
from matchMakingThread import *
import socket
from userThread import *
from gameplayThread import *
from gameUtils import *
from setup import *

#Setup database
setupDatabase()

#Setup threads,queues and start threads
queueToDatabase = Queue.Queue()
queueToMatchMaking = Queue.Queue()

#Queue to stop listenerthread
queueToStop = Queue.Queue()

#Listens on server port
listenerThread = Thread(target=listener,args=(queueToDatabase,queueToMatchMaking,queueToStop))
listenerThread.start()

#Database thread, responsible for communication with database
dataThread = Thread(target=dbThread,args=(queueToDatabase,))
dataThread.start()

#Queue to stop match making thread
exitQueue = Queue.Queue()

#Queue from match making thread to game play thread
outputQueue = Queue.Queue()

matchMakingThread = Thread(target=mmThread,args=(queueToMatchMaking,exitQueue,outputQueue))
matchMakingThread.start()

#Queue to stop game play thread
exitQueue2 = Queue.Queue()

gThread = Thread(target=gameThread,args=(outputQueue,queueToDatabase,exitQueue2,queueToMatchMaking))
gThread.start()

example = raw_input('Press enter when you want to stop simulation')

#Kill threads
#Note that the listener thread will only stop after the socket is listens on
#gets a time out exception, so it will no be killed instantly.
#Dummy Queue for database thread
answerQueue = Queue.Queue()
queueToDatabase.put({'operation':11,'answer':answerQueue})

#Wait for database thread to end first
time.sleep(1)

#matchMakingThread
exitQueue.put(True)
exitQueue2.put(True)
queueToStop.put(True)
