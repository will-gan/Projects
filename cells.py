class Start:
    def __init__(self):
        self.display = 'X'

    def step(self, game):
        game.message = None

class End:
    def __init__(self):
        self.display = 'Y'

    def step(self, game):
        msg = '\n' + '\n' + 'You conquer the treacherous maze set up by the Fire Nation and reclaim the Honourable Furious Forest Throne,' + ' restoring your hometown back to its former glory of rainbow and sunshine! Peace reigns over the lands.' + '\n'
        msg2 = '\n' + 'You made {} moves.'.format(game.move_ct) + '\n'
        msg3 = 'Your moves: ' + ', '.join(game.moves_made) + '\n'
        msg4 = '\n' + 21*'=' + '\n' + '====== YOU WIN! =====' + '\n' + 21*'='
        
        if game.move_ct == 1:
            msg2 = '\n' + 'You made 1 move.'.format(game.move_ct) + '\n'
            msg3 = msg3 = 'Your move: ' + ', '.join(game.moves_made) + '\n'
        
        game.message = msg + msg2 + msg3 + msg4
        game.win = True

class Air:
    def __init__(self):
        self.display = ' '

    def step(self, game):
        game.message = None


class Wall:
    def __init__(self):
        self.display = '*'

    def step(self, game):
        msg = '\n' + "You walked into a wall. Oof!"
        game.message = msg

class Fire:
    def __init__(self):
        self.display = 'F'

    def step(self, game):
        if game.player.num_water_buckets > 0:
            game.player.num_water_buckets -= 1
            game.map[game.player.row][game.player.col] = Air() 
            msg = '\n' + "With your strong acorn arms, you throw a water bucket at the fire. You acorn roll your way through the extinguished flames!"
            game.message = msg
        
        else:
            msg = '\n' + "You step into the fires and watch your dreams disappear :(." + '\n'
            msg2 = '\n' + 'The Fire Nation triumphs! The Honourable Furious Forest is reduced to a pile of ash and' + ' is scattered to the winds by the next storm... You have been roasted.' + '\n'
            msg3 = '\n' + "You made {} moves.".format(game.move_ct) + '\n' + "Your moves: " + ', '.join(game.moves_made) + '\n'
        
            if game.move_ct == 1:
                msg3 = '\n' + "You made 1 move." + '\n' + "Your move: " + ', '.join(game.moves_made) + '\n'
            
            msg4 = '\n' + 21*'=' + '\n' + 5*'=' + ' GAME OVER ' + 5*'=' + '\n' + 21*'='
            game.message = '\n' + msg + msg2 + msg3 + msg4
            game.death = True

class Water:
    def __init__(self):
        self.display = 'W'

    def step(self, game):
        game.player.num_water_buckets += 1
        game.map[game.player.row][game.player.col] = Air()
        msg = '\n' + "Thank the Honourable Furious Forest, you've found a bucket of water!"
        game.message = msg


class Teleport:
    def __init__(self):
        self.display = '' 

    def step(self, game):
        teleported = False
        curr = game.map[game.player.row][game.player.col].display
        i = 0
        while i < len(game.map) and not teleported:
            j = 0
            while j < len(game.map[i]):
                if j == game.player.col and i == game.player.row:
                    j += 1
                    continue
                if game.map[i][j].display == curr:
                    game.player.row = i
                    game.player.col = j
                    teleported = True
                    break
                j += 1
            i += 1
        msg = '\n' + "Whoosh! The magical gates break Physics as we know it and opens a wormhole through space and time."
        game.message = msg
    