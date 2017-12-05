import usocket as socket

class Test():
    """docstring for Test"""
    def __init__(self):
        self.sock = socket.socket()
        ai = socket.getaddrinfo("0.0.0.0", 8080)
        addr = ai[0][-1]
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind(addr)
        self.sock.listen(5)

    def receive(self):
        res = self.sock.accept()
        client_s = res[0]
        client_addr = res[1]
        req = client_s.recv(4096)
        client_s.close()
        return (req, client_addr)
