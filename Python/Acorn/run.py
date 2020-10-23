from game import Game
import os
import sys
import msvcrt

if len(sys.argv) == 1:
    print("Usage: python3 run.py <filename> [play]")

elif len(sys.argv) == 2:
    game = Game(sys.argv[1])
    game.set_player() 
    game.map_load() 
    while True:
        print("\n" + "Input a move: ")
        move = msvcrt.getch().decode("ASCII")
        move = move.lower()
        if move == 'q':
            print('\n' + 'Bye!')
            sys.exit()
        else:
            os.system("cls")
            game.game_move(move)
            game.map_load()
            if game.message == None:
                #os.system("cls")
                continue
            print(game.message)
            if game.death == True or game.win == True:
                sys.exit()
