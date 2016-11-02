#This thread handles all the database operations
#It is connected with the user account handling threads with one queue and gets requests from there
#It is also connected with the match handling thread, to update a users rank/elo
#Messages send are evaluated as follows:
#Each functionality(function) in the serverUtils.py file gets a unique id
#           ID                      |             ARGUMENTS
# 0 --- insertUser                  |       name,email,password
# 1 --- insertUserFacebook          |       facebookID,name,email
# 2 --- userLogin                   |       name,email,password
# 3 --- userLoginFacebook           |       facebookID
# 4 --- updateUser                  |       name,wins difference,losses difference
# 5 --- updateUserFacebook          |       facebookID, wins difference, losses difference
# 6 --- changePassword              |       email,name,newPassword
# 7 --- forgotPassword              |       email,name
# 8 --- confirmPasswordChangeCode   |       email,name,code
# 9 --- getUserData                 |       name,email
#10 --- getFbUserData               |       facebookID,
#11 --- Terminate                   |           -
#Data is sent in form of dictionaries, which also contain the queue that
#will be used to answering to the thread that sent the request
import Queue
from serverUtils import *

DATABASE = 'connect4.db'

def dbThread(requestQueue):
    while True:
        data = requestQueue.get()
        op = data.get('operation')
        answerQueue = data.get('answer')
        result = True
        print 'operation is %d' %op
        #now check what to do with the data
        if op == 0:
            result = insertUser(DATABASE,data.get('name'),data.get('email'),data.get('password'))
        elif op == 1:
            result = insertUserFacebook(DATABASE,data.get('id'),data.get('name'),data.get('email'))
        elif op == 2:
            result = userLogin(DATABASE,data.get('name'),data.get('email'),data.get('password'))
        elif op == 3:
            result = userLoginFacebook(DATABASE,data.get('id'))
        elif op == 4:
            updateUser(DATABASE,data.get('name'),data.get('win'),data.get('loss'))
        elif op == 5:
            updateUserFacebook(DATABASE,data.get('id'),data.get('win'),data.get('loss'))
        elif op == 6:
            changePassword(DATABASE,data.get('email'),data.get('name'),data.get('newPass'))
        elif op == 7:
            result = forgotPassword(DATABASE,data.get('email'),data.get('name'))
        elif op == 8:
            result = confirmPasswordChangeCode(DATABASE,data.get('email'),data.get('name'),data.get('code'))
        elif op == 9:
            result = getUserData(DATABASE,data.get('name'),data.get('email'))
        elif op == 10:
            result = getFbUserData(DATABASE,data.get('id'))
        elif op == 11:
            answerQueue.put(True)
            break
        answerQueue.put(result)
