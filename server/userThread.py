#This thread handles user operations of only 1 user
#, and is connected to the matchmaking thread and to the database thread
#The list of operations is as follows:
#               ID                  |           ARGUMENTS
# 0 --- User signup                 | userType(fb or normal),id,name,email,password
# 1 --- User login                  | userType,id,name,email,password
# 2 --- Change password             | email,name,newPassword
# 3 --- Forgot password             | email,name
# 4 --- Confirm password change code| email,name,code
# 5 --- Start game                  | -
#The separator in the messages can be a space
import socket
from threading import *
PORT = 5501

#This function-thread listens on a port for connections
def listener(queueToDatabase,queueToMatchMaking):
    setupSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM,0)
    setupSocket.bind(('localhost',PORT))
    setupSocket.listen(1)
    while True:
        replySocket,address = setupSocket.accept()
        #now create a new userThread
        uThread = Thread(target=userThread,args=(replySocket,address,queueToDatabase,queueToMatchMaking))
        uThread.start()
        print 'Created new user thread'
        #break is temporary
        break
    setupSocket.close()



#dbQueue is for communicating with database thread
#matchQueue is for communicating with matchmaking thread
def userThread(replySocket,address,dbQueue,matchQueue):
    message = replySocket.recv(512)
    print message
    replySocket.shutdown(socket.SHUT_RDWR)
    replySocket.close()
