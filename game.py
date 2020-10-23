from game_parser import read_lines
from grid import grid_to_string
from player import Player

class Game:
    def __init__(self, filename):
        self.filename = filename
        self.moves_made = []
        self.move_ct = 0
        self.player = None
        self.map = read_lines(filename)
        self.message = None
        self.death = False
        self.win = False
    
    def set_player(self):
        for x in self.map:
            for i in x:
                if i.display == 'X':
                    row = self.map.index(x)
                    col = x.index(i)
        self.player = Player(row, col)
    
    def map_load(self):
        print(grid_to_string(self.map, self.player))
    
    def wall_move(self, move): # response to moving into a wall
        if move == 'a':
            self.player.col += 1
        elif move == 'd':
            self.player.col -= 1
        elif move == 'w':
            self.player.row += 1
        elif move == 's':
            self.player.row -= 1
        self.message = '\n' + "You walked into a wall. Oof!"

    def game_move(self, move):
        moves = ['w', 'a', 's', 'd', 'e', 'q'] # list of moves 
        if not(move in moves):
            self.message = '\n' + "Please enter a valid move (w, a, s, d, e, q)."
        
        else:
            self.player.move(move)
            row = self.player.row
            col = self.player.col
            if row >= len(self.map) or row < 0 or col >= len(self.map[row]) or col < 0: # exit map
                self.wall_move(move)
            
            elif self.map[row][col].display == '*':
                self.wall_move(move)
                self.map[row][col].step(self)
                
            else:
                self.moves_made.append(move)
                self.move_ct += 1
                self.map[row][col].step(self)