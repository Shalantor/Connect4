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
DATABASE = 'connect4.db'

#dbQueue is for communicating with database thread
#matchQueue is for communicating with matchmaking thread
def userThread(dbQueue,matchQueue,userData):
