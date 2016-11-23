#This file contains various functions to be used by the server for saving
#data, processing passwords and calculating a players elo
#TODO:open authentication 2 Android
import string,random,smtplib
import uuid,hashlib,sqlite3,re
from email.mime.text import MIMEText

#Insert users in database that decided to log in with game client
def insertUser(database,name,email,password):

    #See if username is available
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    cursor.execute('SELECT username,email FROM Users')

    for a,b in cursor:
        if a == name or b == email:
            return False

    #now check email validity
    if not re.match('[^@]+@[^@]+\.[^@]+',email):
        return False

    #first set variables that will have a fixed value for a new player
    wins = 0
    losses = 0
    elo = 0

    #Generate hashed password
    salt = uuid.uuid4().hex
    hashed_password = hashlib.sha512(password + salt).hexdigest()

    #organize data
    data = (name,email,salt,hashed_password,wins,losses,elo,0)

    #now store in database
    cursor.execute('INSERT INTO Users VALUES (?,?,?,?,?,?,?,?)',data)
    connection.commit()
    connection.close()

    return True


#insert users that decided to login with facebook
def insertUserFacebook(database,facebookID,name,email):

    #Set variables that are fixed for new users
    wins = 0
    losses = 0
    elo = 0

    #Organize data
    data = (facebookID,name,email,wins,losses,elo)

    #now store in database
    connection = sqlite3.connect(database)
    cursor = connection.cursor()

    #Check for existing userId
    cursor.execute('SELECT facebookid FROM UsersFacebook')
    for a in cursor:
        if a == facebookID:
            return False

    cursor.execute('INSERT INTO UsersFacebook VALUES (?,?,?,?,?,?)',data)
    connection.commit()
    connection.close()
    return True

#Validate user that logged in with account form game client
def userLogin(database,name,email,password):

    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    if email == None:
        nameTuple = (name,)
        cursor.execute('SELECT salt,password FROM Users WHERE username =?',nameTuple)
    elif name == None or (name != None and email!=None):
        nameTuple = (email,)
        cursor.execute('SELECT salt,password FROM Users WHERE email =?',nameTuple)

    for a,b in cursor:
        if hashlib.sha512(password + a).hexdigest() == b:
            return True

    connection.close()
    return False


#Validate user that logged in with facebook
def userLoginFacebook(database,ID):

    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    cursor.execute('SELECT facebookid,email FROM UsersFacebook')

    for a,b in cursor:
        if ID == a:
            #User found
            connection.close()
            return True

    #No such user
    connection.close()
    return False

#TODO:fix elo calculation for a user with only a few games
#Update wins,losses,elo of user logged in with client
def updateUser(database,name,winDiff,loseDiff):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    nameTuple = (name,)
    cursor.execute('SELECT wins,losses FROM Users WHERE username =?',nameTuple)

    #Calculate elo/rank
    for a,b in cursor:
        newWins = a + winDiff
        newLosses = b + loseDiff
        newElo = int(10*newWins / (newWins + newLosses) )

    #now store into database
    data = (newWins,newLosses,newElo,name)
    cursor.execute('UPDATE Users SET wins=?,losses=?,elo=? WHERE username=?',data)
    connection.commit()
    connection.close()


#Update wins,losses,elo of facebook user
def updateUserFacebook(database,facebookID,winDiff,loseDiff):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    nameTuple = (facebookID,)
    cursor.execute('SELECT wins,losses FROM UsersFacebook WHERE facebookid =?',nameTuple)

    #Calculate elo/rank
    for a,b in cursor:
        newWins = a + winDiff
        newLosses = b + loseDiff
        newElo = int(10*newWins / (newWins + newLosses) )

    #now store into database
    data = (newWins,newLosses,newElo,facebookID)
    cursor.execute('UPDATE UsersFacebook SET wins=?,losses=?,elo=? WHERE facebookid=?',data)
    connection.commit()
    connection.close()


#Function to change the password of a user
def changePassword(database,email,name,newPassword):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()

    if name == None:
        data = (email,)
        cursor.execute('SELECT salt FROM Users WHERE email=?',data)
        hashed_password = hashlib.sha512(newPassword + cursor.fetchone()[0]).hexdigest()
        data = (hashed_password,email)
        cursor.execute('UPDATE Users SET password=? WHERE email=?',data)
    elif email == None or (name != None and email!=None):
        data = (name,)
        cursor.execute('SELECT salt FROM Users WHERE username=?',data)
        hashed_password = hashlib.sha512(newPassword + cursor.fetchone()[0]).hexdigest()
        data = (hashed_password,name)
        cursor.execute('UPDATE Users SET password=? WHERE username=?',data)

    connection.commit()
    connection.close()


#Function to send email to user with code to change password
def forgotPassword(database,email,name):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    #Generate random 10 digit integer as a reset code and store it
    code = random.randint(1000000000,9999999999)
    print 'Code is %d \n' % code

    if name == None:
        data = (email,)
        cursor.execute('SELECT * FROM Users WHERE email=?',data)
        #False email
        if cursor.fetchone() == None:
            return False
        data = (code,email)
        cursor.execute('UPDATE Users SET resetCode=? WHERE email=?',data)

    elif email == None or (name != None and email!=None):
        data = (name,)
        cursor.execute('SELECT * FROM Users WHERE username=?',data)
        #False name
        if cursor.fetchone() == None:
            return False
        data = (code,name)
        cursor.execute('UPDATE Users SET resetCode=? WHERE username=?',data)

    connection.commit()
    #Commented out for start, but it is tested and works, it just isnt safe to keep it
    """msg = MIMEText('Dear User, we send you this reset code : %d' % code)
    msg['Subject'] = 'Reset code for connect4'
    me = 'georgkaraolanis@gmail.com'
    msg['From'] = me
    msg['To'] = email
    s = smtplib.SMTP('smtp.gmail.com:587')
    s.ehlo()
    s.starttls()
    s.login(me,'')
    s.sendmail(me,[email],msg.as_string())
    s.quit()"""
    connection.close()
    return True

#TODO:after verification maybe place 0 again in reset code field
#To confirm code for changing password
def confirmPasswordChangeCode(database,email,name,code):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()

    if name == None:
        data = (email,)
        cursor.execute('SELECT resetCode FROM Users WHERE email=?',data)
        result = cursor.fetchone()
        if result == None:
            print 'Result was none 1'
            return False
        else:
            resetCode = result[0]
    elif email == None or (name != None and email!=None):
        data = (name,)
        cursor.execute('SELECT resetCode FROM Users WHERE username=?',data)
        result = cursor.fetchone()
        if result == None:
            return False
        else:
            resetCode = result[0]
    connection.close()
    return resetCode == code

#To get normal user data
def getUserData(database,name,email):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    if name == None:
        data = (email,)
        cursor.execute('SELECT username,email,elo FROM Users WHERE email=?',data)
    elif email == None or (name != None and email!=None):
        data=(name,)
        cursor.execute('SELECT username,email,elo FROM Users WHERE username=?',data)
    result = cursor.fetchone()
    userToken = {'name':result[0],'email':result[1],'rank':result[2]}
    connection.close()
    return userToken

#To get facebook user data
def getFbUserData(database,facebookid):
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    data = (facebookid,)
    cursor.execute('SELECT facebookid,name,email,elo FROM UsersFacebook WHERE facebookid=?',data)
    result = cursor.fetchone()
    userToken = {'id':result[0],'name':result[1],'email':result[2],'rank':result[3]}
    connection.close()
    return userToken


#TODO:REMOVE EVERYTHING BELOW AFTER TESTING
def showAllEntries():
    connection = sqlite3.connect('connect4.db')
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM Users')
    results = cursor.fetchall()
    for r in results:
        print r
    connection.close()
