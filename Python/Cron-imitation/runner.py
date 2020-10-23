import sys, re, os, datetime, signal, time

class Program:
    def __init__(self):
        self.everytime = False
        self.runtimes = []
        self.path = ""
        self.args = []
        self.lastruntime = ""
        self.nextruntime = []

    def date_comp(self, diff, now, time):
        if (now.day + diff) > num_of_days(now.year, now.month): # does the difference in days run into a new mth?
            if not(now.month + 1 in range(13)): # goes into new yr?
                added = datetime.datetime(now.year + 1, 1, (now.day + diff)%num_of_days(now.year, 1),
                int(time[1][:2]), int(time[1][2:]))
            else: # doesnt go into new year, but goes into new mth
                added = datetime.datetime(now.year, now.month + 1, (now.day + diff)%num_of_days(now.year, now.month + 1),
                int(time[1][:2]), int(time[1][2:]))
        
        else: # not a new mth - just regular difference in days
            added = datetime.datetime(now.year, now.month, (now.day + diff)%num_of_days(now.year, now.month),
            int(time[1][:2]), int(time[1][2:]))
        
        return added

    def calcnextruntime(self):
        self.nextruntime.clear()
        now = datetime.datetime.now()
        for time in self.runtimes:
            daydiff = 0
            # prog everyday running
            if time[0] == "a":
                if not(int(now.strftime("%H%M")) < int(time[1])): 
                    # if the next runtime isn't within the current time, then it'll have to be executed on the next day
                    daydiff = 1
            else: # not everyday running 
                # exec day not a sunday
                if time[0] != 0:
                    if time[0] < int(now.strftime("%w")):
                        daydiff = 7 - (int(now.strftime("%w")) - time[0])
                    elif time[0] > int(now.strftime("%w")) and int(now.strftime("%w")) != 0:
                        daydiff = time[0] - int(now.strftime("%w"))
                    elif time[0] > int(now.strftime("%w")) and int(now.strftime("%w")) == 0:
                        daydiff = time[0]
                    elif time[0] == int(now.strftime("%w")) and int(now.strftime("%H%M")) > int(time[1]): # same day & miss time window = next wk
                        daydiff = 7
                # exec day issa sunday
                else:
                    if int(now.strftime("%w")) == 0 and \
                        int(now.strftime("%H%M")) > int(time[1]): # already past exec time but is on sunday
                        daydiff = 7
                    else:
                        daydiff = 7 - int(now.strftime("%w"))
            added = self.date_comp(daydiff, now, time)
            if not(added in self.nextruntime):
                self.nextruntime.append(added)

pidfile = "~/.runner.pid"
statfile = "~/.runner.status"

def startup(statfile, pidfile):
    if (os.path.isfile(statfile)) and (os.path.isfile(pidfile)):
            with open(pidfile, "w") as f:
                f.write(str(os.getpid()))
    else:
        if not(os.path.isfile(statfile)): # create statfile where non-existent
            try:
                f = open(statfile, "w")
                f.close()
            except OSError:
                print("file {} could not be created".format(statfile)) # error msg during status file creation
        if not(os.path.isfile(pidfile)):
            try:
                f = open(pidfile, "w")
                f.write(str(os.getpid()))
                f.close()
            except OSError:
                print("file {} could not be created".format(pidfile))

def fileparser(cfgfile):
    try:
        f = open(cfgfile)
    except FileNotFoundError:
        print("configuration file not found.")
        exit()
    
    ls = f.readlines()
    if len(ls) == 0: # empty file
        print("configuration file empty."); exit()
    
    frequencies = {"on":False, "every":True, "at":True}
    daydict = {"Sunday":0, "Monday":1, "Tuesday":2, "Wednesday":3, "Thursday":4, "Friday":5, "Saturday":6}
    result = []

    for line in ls:
        added = Program()
        cline = line.split()
        if len(line.strip()) == 0:
            print("empty line"); exit()
        days = []; times = []

        # starts with frequency keyword
        if re.search("^(at){1}|^(every){1}|^(on){1}", line) is None:
            print("error in configuration: {}".format(line)); exit()

        elif not("at") in line:
            print("error in configuration: {}".format(line)); exit()

        # check time formatting
        elif not(cline[cline.index("at") + 1].translate({ord(","): None}).isnumeric()):
            print("error in configuration: {}".format(line)); exit()

        # presence of run keyword and program path
        elif re.search("run", line) is None or len(re.findall("/", line)) <= 1:
            print("error in configuration: {}".format(line)); exit()

        elif "on " in line and "every" in line: 
            print("error in configuration: {}".format(line)); exit()

        pathend_idx = cline.index("run") + 1
        added.everytime = frequencies[cline[0]] # add frequency
        added.path = cline[pathend_idx] # prog path

        # parsing times
        for num in (cline[cline.index("at") + 1].split(",")):
            if re.search("^([0-2][0-9])[0-5][0-9]", num) is None:
                print("error in configuration: {}".format(line)); exit()
            elif not(int(re.search("^([0-2][0-9])[0-5][0-9]", num).group()) in range(2400)):
                print("error in configuration: {}".format(line)); exit()
            times.append(num)
            if times.count(num) > 1:
                print("error in configuration: {}".format(line)); exit()

        # parsing days 
        if cline[0] != "at": # this would indicate running at varying days 
            for day in (cline[cline.index("at") - 1].split(",")):
                if not(day in daydict):
                    print("error in configuration: {}".format(line)); exit()
                days.append(day)
                if days.count(day) > 1:
                    print("error in configuration: {}".format(line)); exit()
        # args
        for i in range(pathend_idx + 1, len(cline)):
            if "/" in cline[i]: # multiple prog paths
                print("error in configuration: {}".format(line)); exit()
            else:
                added.args.append(cline[i])
        
        # gen run records
        if len(days) == 0: # just times, no days
            for t in times:
                added.runtimes.append(("a", t))
        
        for d in days:
            for t in times:
                added.runtimes.append((daydict[d], t))
        result.append(added)
        
        # duplicate time-checking
        for r in result:
            id = result.index(r)
            for t in r.runtimes: # take each time tuple in the runtimes list 
                for r2 in result: # iterate thru result list for comparison 
                    if result.index(r2) == id: # if comparing the same result, then bypass
                        continue
                    for t2 in r2.runtimes: # iterate thru 2nd Program object and compare runtimes to object "r"
                        if t2[1] == t[1]: # matching times
                            if t2[0] == "a" or t[0] == "a": # either one is everyday
                                print("error in configuration: {}".format(line)); exit()
                            elif t2[0] == t[0]: # matching days AND matching times
                                print("error in configuration: {}".format(line)); exit()
    return result

queue = [] # queue holds the queue of programs to run for the day
succ = []; fail = []

def write_to_status(filename, successful, failed, programlist):
    with open(filename, "w") as f:
        # log programs running successfully, when they last ran, and when they will next run
        if len(successful) != 0 or len(failed) != 0:
            for p in successful:
                p.calcnextruntime()
                f.write("ran {}, {} {}\n".format(p.lastruntime, p.path, " ".join(p.args)))
                for t in p.nextruntime:
                    f.write("will run at {}, {} {}\n".format(t.ctime(), p.path, " ".join(p.args)))
            for p in failed:
                p.calcnextruntime()
                f.write("error {}, {} {}\n".format(p.lastruntime, p.path, " ".join(p.args)))
                for t in p.nextruntime:
                    f.write("will run at {}, {} {}\n".format(t.ctime(), p.path, " ".join(p.args)))
        else:
            calcruns(programlist)
            for p in programlist:
                for t in p.nextruntime:
                    f.write("will run at {}, {} {}\n".format(t.ctime(), p.path, " ".join(p.args)))

def execprog(program, success, fail):
    if not(os.path.exists(program.path)): # program path doesn't even exist - don't bother
        print("program path: {} does not exist".format(program.path))
        fail.append(program)
    else: # program path does exist? try execution then.
        pid = os.fork()
        if pid == 0: # child
            try:
                os.execv(program.path, [sys.argv[0]] + program.args) # execute
            except OSError: # failed to execute
                fail.append(program)
                return -1
        elif pid == -1: # smth went wrong
            fail.append(program)
        else: # parent
            os.wait()
            success.append(program)

def queue_add(proglist, queuelist):
    now = datetime.datetime.now()
    for program in proglist:
        # pick up the list of runtimes in the program
        for t in program.nextruntime:
            checktup = (program, t)
            if float((t - now).total_seconds()) <= 86400 and float((t - now).total_seconds()) > 0 and \
                not(checktup in queuelist):
                queuelist.append(checktup)

    queuelist.sort(key=lambda checktup: checktup[1]) # sort by time 
    return queuelist

def handler(signum, d):
    if signum == signal.SIGUSR1:
        write_to_status(statfile, succ, fail, program_ls)

def num_of_days(y, m):
    leap = 0
    if y%400 == 0 or y%4 == 0:
        leap = 1
    elif y%100 == 0:
        leap = 0
    if m == 2:
        return 28 + leap
    ls = [1, 3, 5, 7, 8, 10, 12]
    if m in ls:
        return 31
    return 30

def seconds_until_eod():
    dt = datetime.datetime.now()
    return ((24 - dt.hour - 1) * 60 * 60) + ((60 - dt.minute - 1) * 60) + (60 - dt.second)

def calcruns(programlist):
    for p in programlist:
        p.calcnextruntime()
        p.nextruntime.sort()

def run(programlist, queuelist):
    signal.signal(signal.SIGUSR1, handler) # catch SIGUSR1
    queue_add(programlist, queuelist) # initial program queueing process - load up programs for the day
    while len(queuelist) != 0 or len(programlist) != 0:
        if len(queuelist) == 0 and len(programlist) != 0: # program list not empty but finished running for the day? 
            time.sleep((seconds_until_eod() + 1)) # sleep until next day
            calcruns(programlist) # calculate new running times for each program
            queue_add(programlist, queuelist) # another day, another set of programs
        else:
            cp = queue.pop(0) # pop tuple 1 at a time. Holds program, and datetime object of the program's runtime for today
            time.sleep((cp[1] - datetime.datetime.now()).total_seconds()) # sleep until next process
            execprog(cp[0], succ, fail) # execute program
            if not(cp[0].everytime): # remove runtime tuple if program isn't running constantly
                cp[0].runtimes.remove( (int(cp[1].strftime("%w")), cp[1].strftime("%H%M")) )
            if len(cp[0].runtimes) == 0: # remove program if there's no times left in the program runtime
                programlist.remove(cp[0])
            cp[0].lastruntime = cp[1].ctime() # set last runtime as the datetime object in tuple "cp"
    print("no more programs")
    return 0

startup(statfile, pidfile) # startup functionality
program_ls = fileparser(sys.argv[1]) # parsing
calcruns(program_ls) # calculate next run times
run(program_ls, queue) # main run function
