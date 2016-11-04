#This thread handles user operations of only 1 user
#, and is connected to the matchmaking thread and to the database thread
#The list of operations is as follows:
#userType: 0 for normal, 1 for facebook
#               ID                  |           ARGUMENTS
# 0 --- User signup                 | userType(fb or normal),id,name,email,password
# 1 --- User login                  | userType,id,name,email,password
# 2 --- Change password             | email,name,newPassword
# 3 --- Forgot password             | email,name
# 4 --- Confirm password change code| email,name,code
# 5 --- Start game                  | -
#The separator in the messages can be a space, and an empty field can be a \n
#so the final form of the messages is:
# 0         0 userType id name email password
# 1         1 userType id name email password
# 2         2 name email newPassword
# 3         3 name email
# 4         4 name email code
# 5         5
import socket,Queue
from threading import *
PORT = 5501

"""TODO:CHANGE MESSAGES TO NOT SEND EMALI AND USERNAME AGAIN BECAUSE IT IS POINTLESS"""
#TODO:also send back result to user for operations
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
    setupSocket.close()

#dbQueue is for communicating with database thread
#matchQueue is for communicating with matchmaking thread
def userThread(replySocket,address,dbQueue,matchQueue):
    answerQueue = Queue.Queue()
    userType = None
    userId = None
    name = None
    email = None
    while True:
        message = replySocket.recv(512)
        print message
        args = message.split()
        #Now check operation type
        if args[0] == '0':
            userType = args[1]
            userId = args[2]
            name = args[3]
            email = args[4]
            password = args[5]
            #Check user type
            if userType == '0':#normal user
                data = {'operation':0,'answer':answerQueue,'name':name,'email':email,'password':password}
            elif userType == '1':#Facebook user
                data = {'operation':1,'answer':answerQueue,'id':userId,'name':name,'email':email}
        elif args[0] == '1':
            userType = args[1]
            userId = args[2]
            name = args[3]
            email = args[4]
            password = args[5]
            if userType == '0':#normal user
                data = {'operation':2,'answer':answerQueue,'name':name,'email':email,'password':password}
            elif userType == '1':#Facebook user
                data = {'operation':3,'answer':answerQueue,'id':userId}
        elif args[0] == '2':
            name = args[1]
            email = args[2]
            password = args[3]
            data = {'operation':6,'answer':answerQueue,'name':name,'email':email,'newPass':password}
        elif args[0] == '3':
            name = args[1]
            email = args[2]
            data = {'operation':7,'answer':answerQueue,'name':name,'email':email}
        elif args[0] == '4':
            name = args[1]
            email = args[2]
            code = args[3]
            data = {'operation':8,'answer':answerQueue,'name':name,'email':email,'code':code}
        elif args[0] == '5':
            if userType == '0':
                data = {'operation':9,'answer':answerQueue,'name':name,'email':email}
            elif userType == '1':
                data = {'operation':10,'answer':answerQueue,'id':userId}
            #get user data
            dbQueue.put(data)
            playerToken = answerQueue.get()
            #now send to matchmaking thread
            queueToMatchMaking.put(playerToken)
            print 'Send data to match making thread'
            break

        #now send data
        dbQueue.put(data)
        result = answerQueue.get()
        print 'result of operation is %b' % result

    #Terminate thread
    print 'Terminating myself'
    replySocket.shutdown(socket.SHUT_RDWR)
    replySocket.close()
