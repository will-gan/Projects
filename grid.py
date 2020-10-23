from game_parser import read_lines
from player import Player

def grid_to_string(grid, player):
    """Turns a grid and player into a string

    Arguments:
        grid -- list of list of Cells
        player -- a Player with water buckets

    Returns:
        string: A string representation of the grid and player.
    """ 
    string = ''
    ls = []
    
    for x in grid: 
        result = '' 
        for i in x:
            if grid.index(x) == player.row and x.index(i) == player.col: 
                result += player.display 
                continue
            result += i.display 
        ls.append(result) 
    
    for x in ls:
        string += x + '\n' 
    
    if player.num_water_buckets != 1:
        string += '\n' + 'You have {} water buckets.'.format(player.num_water_buckets)
    
    else:
        string += '\n' + 'You have 1 water bucket.'
    
    return string # returns whole grid as single string