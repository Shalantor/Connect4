#This thread handles user operations of only 1 user
#, and is connected to the matchmaking thread and to the database thread
#The list of operations is as follows:
#userType: 0 for normal, 1 for facebook
#               ID                  |           ARGUMENTS
# 0 --- User signup                 | userType(fb or normal),id,name,email,password
# 1 --- User login                  | userType,id,name,email,password
# 2 --- Change password             | newPassword
# 3 --- Forgot password             | email,name
# 4 --- Confirm password change code| email,name,code
# 5 --- Start game                  | -
#The separator in the messages can be a space and messages are terminated with \n
#so the final form of the messages is:
# 0         0 userType id name email password
# 1         1 userType id name email password
# 2         2 newPassword
# 3         3 email,name
# 4         4 email,name,code
# 5         5
import socket,Queue
from threading import *
PORT = 11337

#This function-thread listens on a port for connections
def listener(queueToDatabase,queueToMatchMaking,setupSocket):

    #Configure server Socket
    setupSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    #Listen on all interfaces
    setupSocket.bind(('0.0.0.0',PORT))
    setupSocket.setblocking(True)

    while True:
        setupSocket.settimeout(None)
        setupSocket.listen(1)

        print 'LISTENING'
        replySocket,address = setupSocket.accept()
        #now create a new userThread
        uThread = Thread(target=userThread,args=(replySocket,queueToDatabase,queueToMatchMaking))
        uThread.start()
        replySocket.send('0\n')
        print 'Created new user thread'

    print('Listener Thread ends now')
    setupSocket.close()

#dbQueue is for communicating with database thread
#matchQueue is for communicating with matchmaking thread
def userThread(replySocket,dbQueue,matchQueue,userType = None,userId = None,name = None,email = None):
    answerQueue = Queue.Queue()
    replySocket.settimeout(None)
    while True:
        message = replySocket.recv(512)
        #Connection shut down on other side
        if len(message) == 0:
            print 'CLIENT SOCKET SHUT DOWN'
            break

        print "MESSAGE IS " + message
        args = message.split()

        #After game message
        if (len(args) == 1 and args[0] != '5'):
            continue

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
            name = None if args[3] == '0' else args[3]
            email = None if args[4] == '0' else args[4]
            password = args[5]
            if userType == '0':#normal user
                data = {'operation':2,'answer':answerQueue,'name':name,'email':email,'password':password}
            elif userType == '1':#Facebook user
                data = {'operation':3,'answer':answerQueue,'id':userId}
        elif args[0] == '2':
            password = args[1]
            data = {'operation':6,'answer':answerQueue,'name':name,'email':email,'newPass':password}
        elif args[0] == '3':
            email = None if args[1] == '0' else args[1]
            name = None if args[2] == '0' else args[2]
            data = {'operation':7,'answer':answerQueue,'name':name,'email':email}
        elif args[0] == '4':
            email = None if args[1] == '0' else args[1]
            name = None if args[2] == '0' else args[2]
            code = int(args[3])
            data = {'operation':8,'answer':answerQueue,'name':name,'email':email,'code':code}
        elif args[0] == '5':
            if userType == '0':
                data = {'operation':9,'answer':answerQueue,'name':name,'email':email}
            elif userType == '1':
                data = {'operation':10,'answer':answerQueue,'id':userId}
            #get user data
            dbQueue.put(data)
            playerToken = answerQueue.get()
            playerToken['type'] = userType
            playerToken['socket'] = replySocket
            #now send to matchmaking thread
            print 'Send data to %s' % name
            replySocket.send('0\n')
            matchQueue.put(playerToken)
            print 'Send data to match making thread'
            break

        #now send data
        dbQueue.put(data)
        result = answerQueue.get()
        print 'result of operation is %r' % result
        if result:
            replySocket.send('0\n')
        else:
            replySocket.send('1\n')

    #Terminate thread
    print 'User Thread out'
