#Just for testing
#SUCCESSFULLY TESTED OPERATIONS: 0,1,2,3,4,5,6,7,8,9
#CURRENLTY TESTING OPERATION 8
import Queue
from databaseThread import *
from threading import *

requestQueue = Queue.Queue()
answerQueue = Queue.Queue()
myThread = Thread(target=dbThread,args=(requestQueue,))
myThread.start()
#First create a user
data = {'operation':8,'answer':answerQueue,'name':'SherlockHolmes1','email':None,'code':8038379981}
requestQueue.put(data)
print 'waiting for answer'
result = answerQueue.get()
if result:
    print 'Success with this user'
else:
    print 'No success with this user'
data = {'operation':9,'answer':answerQueue}
requestQueue.put(data)
result = answerQueue.get()
if result:
    print 'thread exited normally'
