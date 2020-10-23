import signal, sys, os, time

pidfile = "~/.runner.pid"
statfile = "~/.runner.status"

# pidfile has the pid of runner.py process

with open(pidfile) as f:
    line = f.readline().strip()
    if line.isnumeric():
        pid = int(line)

# signal - get runner.py to write status
os.kill(pid, signal.SIGUSR1)

# read status file 
try:
    f = open(statfile)
except FileNotFoundError:
    print("file {} does not exist.".format(statfile))
    exit()
else:
    arrived = False
    i = 0
    while i <= 5:
        ls = f.readlines()
        if len(ls) != 0:
            arrived = True
            break
        i += 1
        time.sleep(1)
    if not(arrived):
        print("status timeout")
    f.close()
    ls.sort()
    for line in ls: # print lines in status file to stdout
        print(line)
    # truncate length to zero 
    f = open(statfile, "w")
    f.close()
