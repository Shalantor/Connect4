#This file contains various functions to be used by the server for saving
#data, processing passwords and calculating a players elo
import string,random
import uuid,hashlib,sqlite3

def insertUser(database,name,email,password):

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
    connection = sqlite3.connect(database)
    cursor = connection.cursor()
    cursor.execute('INSERT INTO Users VALUES (?,?,?,?,?,?,?)',data)
    connection.commit()
    connection.close()
