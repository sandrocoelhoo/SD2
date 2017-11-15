package Handlers;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class ServerHandler {

    public static GrafoHandler handler;
    public static Grafo.MetodosGrafo.Processor processor;

    static String args1[] = {"4242"};

    public static void main(String[] args) {
        try {
            handler = new GrafoHandler();
            processor = new Grafo.MetodosGrafo.Processor(handler);

            TServerTransport servertransport = new TServerSocket(Integer.parseInt(args1[0]));

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(servertransport).processor(processor));

            System.out.println("Server started and running... ");
            server.serve();

        } catch (TException x) {
            x.printStackTrace();
        }
    }
}
