package Handlers;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class Server {

    public static Handler handler;
    public static Grafo.Thrift.Processor processor;
    
    public static void main(String [] args) {
        try {
            handler = new Handler(args);
            processor = new Grafo.Thrift.Processor(handler);

            TServerTransport servertransport = new TServerSocket(Integer.parseInt(args[1]));

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(servertransport).processor(processor));

            System.out.println("Server started and running at port " + args[1] + "...");
            server.serve();

        } catch (TException x) {
            x.printStackTrace();
        }
    }
}
