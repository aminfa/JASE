
from flask import Flask
from flask import request
from flask import Response
from multiprocessing import Process, Manager
import time
application = Flask("test server")
from werkzeug.serving import WSGIRequestHandler
WSGIRequestHandler.protocol_version = "HTTP/1.1"

@application.route('/', defaults={'path': ''}, methods = ['POST', 'GET'])
@application.route('/<path:path>', methods=['POST', 'GET'])
def catch_all(path):
    # catches all request
    request.environ['wsgi.input_terminated'] = True
    # print("PATH: ", path)
    print("---> Headers: \n", request.headers)
    # body = request.get_json(force = True, silent = True) 
    data = bytearray()
    chunk_size = 2 << 20
    # stream = request.environ['input']
    while True:
        chunk = request.stream.read(chunk_size)
        if len(chunk) == 0:
            break
        else :
            data.extend(chunk)


    print("---> Sream length: \n", len(data)) 
    decodeddata = data.decode()
    print("---> Sream content: \n", decodeddata)

    # start worker:
    manager = Manager()
    out_dict = manager.dict()
    out_dict["result"] = None # shared container that will hold the result:
    p = Process(target = worker_function, args = (out_dict, ))
    p.start()


    def generate_result():
        time.sleep(0.01) # sleep 10 milliseconds to yield process and allow the worker to generate the result
        while out_dict["result"] is None: # poll every 100 millisecond if the result are generated?
            try:
                yield "" # try to send back nothing
                print("Connection alive.")
            except:
                print("Connection dead. terminating..")
                # preempt process.
                p.terminate()
                print("terminated.")
                return 
            time.sleep(0.1) 

        print("Process finished, returning result")
        yield out_dict["result"]
        print("finished returning result")

    return Response(generate_result()) 


def worker_function(out): # function that generates the response.
    time.sleep(4) # sleep 4 seconds
    out["result"] = "done"

if __name__ == '__main__':
    application.run("localhost", port = 5000, debug = True)