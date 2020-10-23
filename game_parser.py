from cells import (
    Start,
    End,
    Air,
    Wall,
    Fire,
    Water,
    Teleport
)

def read_lines(filename):
    try: 
        with open(filename) as f: 
            ls = f.readlines()
            return parse(ls) 
    
    except FileNotFoundError: 
        exit("{} does not exist!".format(filename))

def parse(lines):
    dic = {'*':Wall, ' ':Air, 'F':Fire, 'W':Water, 'X':Start, 'Y':End}
    for i in range(1, 10): # add telepad disp. attr. --> [1, 10)
        dic[str(i)] = Teleport
    ls = []  
    ltp = [] # holds disp. attr.
    cx = 0  
    cy = 0 
    for x in lines:
        ls2 = []  
        for e in x: 
            a = '' 
            if e in dic: 
                if e == 'X':
                    cx += 1
                elif e == 'Y':
                    cy += 1
                a = dic[e]() # create new object
                a.display = e 
                ltp.append(e)
                ls2.append(a)
            if not(e in dic):
                if e == '\n': # skip newlines. do not add to list of cells
                    continue
                else:
                    raise ValueError("Bad letter in configuration file: {}".format(str(e))) 
        
        ls.append(ls2)
    
    if cx != 1:
        raise ValueError("Expected 1 starting position, got {}.".format(cx)) 
    if cy != 1:
        raise ValueError("Expected 1 ending position, got {}.".format(cy))
    for i in ltp: # check list of disp. attr.
        if i.isnumeric() and ltp.count(i) != 2: # filter out telepad attributes ONLY
            raise ValueError("Teleport pad number {} does not have an exclusively matching teleport pad.".format(i))
    
    return ls 