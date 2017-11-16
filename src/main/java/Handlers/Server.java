package Handlers;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class Server {

    public static ServerHandler handler;
    public static Grafo.Handler.Processor processor;
    
    public static void main(String [] args) {
        try {
            handler = new ServerHandler(args);
            processor = new Grafo.Handler.Processor(handler);

            TServerTransport servertransport = new TServerSocket(Integer.parseInt(args[1]));

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(servertransport).processor(processor));

            System.out.println("Server started and running at port " + args[1] + "...");
            server.serve();

        } catch (TException x) {
            x.printStackTrace();
        }
    }
}
