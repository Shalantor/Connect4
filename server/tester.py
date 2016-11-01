#Just for testing
#SUCCESSFULL TESTINGS: 0
#CURRENLTY TESTING OPERATION 1
import Queue
from databaseThread import *
from threading import *

requestQueue = Queue.Queue()
answerQueue = Queue.Queue()
myThread = Thread(target=dbThread,args=(requestQueue,))
myThread.start()
#First create a user
data = {'operation':0,'answer':answerQueue,'name':'Shermes1','email':'bak@gmail.com','password':'1412'}
requestQueue.put(data)
print 'waiting for answer'
result = answerQueue.get()
if result:
    print 'User was entered successfully'
else:
    print 'No success with this user'
data = {'operation':9,'answer':answerQueue}
requestQueue.put(data)
result = answerQueue.get()
if result:
    print 'thread exited normally'
