#This file contains various functions to be used by the server for saving
#data, processing passwords and calculating a players elo
import string,random

SALT_LENGTH = 100

#Generate a string that will be used for password storing
def generateSaltString():
    salt = ""
    for i in range(SALT_LENGTH):
        randomCharacter = random.choice(string.ascii_letters)
        salt = salt + randomCharacter
    print salt
    return salt

def insertUser(database,name,email,password):

    #first set variables that will have a fixed value for a new player
    wins = 0
    losses = 0
    elo = 0
    salt = generateSaltString()
