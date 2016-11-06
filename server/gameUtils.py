#This file contains utility functions for the games

#Check if given user has won
def hasWon(grid,chip):
    #check same row
    for row in range(0,6):
        sameChip = 0
        for col in range(0,7):
            if grid[row][col] == chip:
                sameChip += 1
                if sameChip == 4:
                    return True
            else:
                sameChip = 0

    #Check same column
    for col in range(0,7):
        sameChip = 0
        for row in range(0,6):
            if grid[row][col] == chip:
                sameChip += 1
                if sameChip == 4:
                    return True
            else:
                sameChip = 0
