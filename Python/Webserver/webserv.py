import sys, socket, os, time

# parse file --> set environ var --> retrieve files --> set socket --> fork --> handle requests as child
# environment variables are for CGI programs (the one required can be found upon parsing HTTP request)
def cfgparser(filename):
	try:
		f = open(filename)
	except FileNotFoundError:
		print("Unable To Load Configuration File")
		exit(-1)
	ls = f.readlines()
	if (len(ls) < 4):
		print("Missing Field From Configuration File")
		return []
	else:
		for line in ls:
			for x in line.strip().split("="):
				if len(x) == 0:
					print("Missing Field From configuration File")
					return []

		sf=ls[0].split("=")[1]
		cbin=ls[1].split("=")[1]
		p=int(ls[2].split("=")[1].strip())
		epath=ls[3].split("=")[1]
	f.close()
	return (sf, cbin, p, epath)


def retriever(path):
	all_files = []
	try:
		for root in os.walk(sys.argv[1]):
			for f in root[2]:
				all_files.append(f)
	except OSError:
		return -1

	return all_files
# serv_port = info_tup[2], client_conn = client in serv_socket.accept()
def environ_setter(req_data, client_conn, serv_port):
	ls = req_data.split("\n")
	database = {
		'Accept:':'HTTP_ACCEPT', 'Host:':'HTTP_HOST',
		'User-Agent:':'HTTP_USER_AGENT',
		'Content-Type:':'CONTENT_TYPE',
		'Content-Length:':'CONTENT_LENGTH', 
		'Accept-Encoding:':'HTTP_ACCEPT_ENCODING'
	}
	# static values 
	os.environ['REQUEST_URI'] = "".join(ls[0].split(" ")[1].split("?")[:1])
	os.environ['REMOTE_ADDRESS'] = client_conn[0]
	os.environ['REMOTE_PORT'] = str(client_conn[1])
	os.environ['REQUEST_METHOD'] = ls[0].split(" ")[0]
	os.environ['SERVER_ADDR'] = '127.0.0.1'
	os.environ['SERVER_PORT'] = str(serv_port)
	# query string
	if "?" in ls[0].split(" ")[1]:
		os.environ['QUERY_STRING'] = "".join(ls[0].split(" ")[1].split("?")[1:])
	# rest of headers not covered
	for i in range(1, len(ls)):
		tmp = ls[i].split(" ")
		if tmp[0] in database and len(tmp) == 2:
			os.environ[database[tmp[0]]] = tmp[1]

def not_found(client, f_ext):
	client.send("HTTP/1.1 404 File not found\n".encode())
	client.send(f"Content-Type: {f_ext}\n".encode())
	err_string = """404 Not Found\n\n<html>\n<head>\n\t<title>404 Not Found</title>\n</head>
<body bgcolor="white">\n<center>\n\t<h1>404 Not Found</h1>\n</center>\n</body>\n</html>\n"""
	client.send(err_string.encode())

def cgi_handler(filepath, execpath, client, ftype):
	r, w = os.pipe()
	pid2 = os.fork()
	if pid2 == 0:
		# child
		os.dup2(w, 1)
		if (os.path.isfile(filepath)):
			try:
				os.execv(execpath, (execpath, filepath))
			except FileNotFoundError:
				os.close(w)
				os.close(r)
				client.send("HTTP/1.1 500 Internal Server Error\n".encode())
				err_string = """500 Internal Server Error\n\n<html>\n<head>\n\t<title>500 Internal Server Error</title>\n</head>
	<body bgcolor="white">\n<center>\n\t<h1>500 Internal Server Error</h1>\n</center>\n</body>\n</html>\n"""
				client.send(err_string.encode())
				client.send
				sys.exit()
			finally:
				client.close()
				os._exit(0)
		else:
			client.send("HTTP/1.1 500 Internal Server Error\n".encode())
			err_string = """500 Internal Server Error\n\n<html>\n<head>\n\t<title>500 Internal Server Error</title>\n</head>
<body bgcolor="white">\n<center>\n\t<h1>500 Internal Server Error</h1>\n</center>\n</body>\n</html>\n"""
			client.send(err_string.encode())
			client.close()

	elif pid2 > 0:
		# parent
		wait_id = os.wait()
		data_read = os.read(r, 4096).decode()
		os.close(w)
		os.close(r)
		# if not("Status-Code" in data_read):
		client.send("HTTP/1.1 200 OK\n".encode())
		# if not("Content-Type" in data_read):
		client.send("Content-Type: text/html\n".encode())
		client.send("\n".encode())
		client.sendall(data_read.encode())
		client.close()
	else:
		# error
		client.close()
		sys.exit()

if len(sys.argv) < 2:
	print("Missing Configuration Argument")
	exit()
info_tup = cfgparser(sys.argv[1])

if len(info_tup) == 0:
	exit(-1)

sfiles = retriever(info_tup[0])
cfiles = retriever(info_tup[1])
if sfiles == -1 or cfiles == -1:
	exit(-1)

serv_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serv_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
serv_sock.bind(("127.0.0.1", info_tup[2]))
serv_sock.listen() # listen for connections

while True:
	
	client, addr = serv_sock.accept() # wait until connection is made
	data = client.recv(1024).decode() # decode request in child
	ls = data.split("\n")
	get, file_info, protocol = ls[0].split()
	environ_setter(data, addr, info_tup[2])
	if file_info.strip() == "/":
		file_req = "index.html" ### ON THE DAY, PUT AS "./{}/index.html".format(info_tup[0])
		ext_type = "html"
	else:
		file_req = file_info.lstrip("/")
		ext_type = "".join(file_req.split("/")[-1].split(".")[1:])
	
	pid = os.fork()
	if pid > 0: # parent
		client.close()
	elif pid < 0: # error
		client.close()
		sys.exit(-1)

	elif pid == 0: # child -> client handler
		## static files --> no need to fork & exec & re-pipe data
		mapping = {
			"txt":"text/plain", "html":"text/html", "js":"application/javascript",
			"css":"text/css", "png":"image/png", "jpg":"image/jpeg", "jpeg":"image/jpeg",
			"xml":"text/xml"
		}
		if ext_type in mapping:
			f_ext = mapping[ext_type]
		if not("cgibin" in file_req):
			fname = "./{}/{}".format(info_tup[0], file_req)
			try:
				if f_ext == "image/png" or f_ext == "image/jpeg":
					with open(fname, "rb") as fi:
						client.send("HTTP/1.1 200 OK\n".encode()) # default status msg.
						client.send(f"Content-Type: {f_ext}\n".encode())
						client.send("\n".encode())
						ls = fi.readlines()
						for line in ls:
							client.send(line)
				else:
					f = open(fname, "r")
					lines = f.readlines()
					client.send("HTTP/1.1 200 OK\n".encode()) # default status msg.
					if not("Content-Type" in lines[0]):
						client.send(f'Content-Type: {f_ext}\n'.encode())
					client.send('\n'.encode())
					i = 0
					while i < len(lines):
						client.send(lines[i].encode())
						i += 1
					f.close()
			
			except FileNotFoundError:
				not_found(client, f_ext)

			except BrokenPipeError:
				pass

			finally:
				client.close()

		elif "cgibin" in file_req:
			file_req = "".join(file_req.split("/")[1:]) # remove cgi bin pt. and go onto file requested
			args = "".join(file_req.split("?")[1:]).split("&")
			file_req = "".join(file_req.split("?")[:1])
			f_ext = file_req.split(".")[1]
			file_req = "./{}/{}".format(info_tup[1], file_req)
			cgi_handler(file_req, info_tup[3].strip(), client, f_ext)
