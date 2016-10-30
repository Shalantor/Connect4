import sqlite3

#create database
connection = sqlite3.connect('connect4.db')
cursor = connection.cursor()

#Create a table for users logging in from connect4 game
cursor.execute('''CREATE TABLE IF NOT EXISTS Users (username text,
                                email text UNIQUE,
                                salt text,
                                password text,
                                wins integer ,
                                losses integer,
                                elo integer,
                                resetCode integer,
                                PRIMARY KEY(username))''')

#Create a table for users logging in from facebook
cursor.execute('''CREATE TABLE IF NOT EXISTS UsersFacebook (facebookid text,
                                name text,
                                email text,
                                wins integer,
                                losses integer,
                                elo integer,
                                PRIMARY KEY(facebookid))''')

connection.commit()

cursor.close()
