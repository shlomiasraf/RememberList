_author__ = 'Shlomi'
import socket
import threading
class ThreadedServer(object):
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind((self.host, self.port))
    def listen(self):
        self.sock.listen(5)
        while True:
            client, address = self.sock.accept()
            print (client)
            client.settimeout(60)
            threading.Thread(target = self.listenToClient,args = (client,address)).start()
    def clientHandle(self,client,data):
        pass
    def listenToClient(self, client, address):
        size = 1024
        while True:
            try:
                data = client.recv(size)
                data = str(data, 'utf-8')
                if data:
                    self.clientHandle(client,data)
                else:
                    raise Exception('Client disconnected')
                if data == "lists\n":
                    my_file = open("lists.txt", "r")
                    s = my_file.read()
                    client.sendall((s + "\n").encode('utf-8'))
                elif data[:-2] == "values":
                    my_file = open("values.txt", "r")
                    s = my_file.read()
                    lists = s.split("#")
                    i = data[-2]
                    s = lists[int(i)]
                    client.sendall((s + "\n").encode('utf-8'))
            except:
                client.close()
                return False

if __name__ == "__main__":
    host = "192.168.43.70"
    while True:
        port_num = 8889
        try:
            port_num = int(port_num)
            break
        except ValueError:
            pass
    print ("started")
    ThreadedServer(host,port_num).listen()