#This file contains various functions to be used by the server for saving
#data, processing passwords and calculating a players elo
import string,random
import uuid,hashlib,sqlite3

#Insert users in database that decided to log in with game client
def insertUser(database,name,email,password):

    #See if username is available
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    cursor.execute('SELECT username,email FROM Users')

    for a,b in cursor:
        if a == name or b == email:
            return False

    #first set variables that will have a fixed value for a new player
    wins = 0
    losses = 0
    elo = 0

    #Generate hashed password
    salt = uuid.uuid4().hex
    hashed_password = hashlib.sha512(password + salt).hexdigest()

    #organize data
    data = (name,email,salt,hashed_password,wins,losses,elo)

    #now store in database
    cursor.execute('INSERT INTO Users VALUES (?,?,?,?,?,?,?)',data)
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
    cursor.execute('INSERT INTO UsersFacebook VALUES (?,?,?,?,?,?)',data)
    connection.commit()
    connection.close()
    return True

#Validate user that logged in with account form game client
def userLogin(database,name,password):

    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    nameTuple = (name,)
    cursor.execute('SELECT salt,password FROM Users WHERE username =?',nameTuple)

    if cursor == None:#no such user
        return False
    else:
        for a,b in cursor:
            if hashlib.sha512(password + a).hexdigest() == b:
                return True
    connection.close()


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

action = insertUser('connect4.db','George1234','mail2','123')
if action:
    print 'success'
action = userLogin('connect4.db','George123412321321','123')
if action:
    print 'User is stored in database'
