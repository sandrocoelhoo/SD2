package Handlers;

import Grafo.Aresta;
import Grafo.Chord;
import Grafo.Finger;
import Grafo.KeyNotFound;
import Grafo.Node;
import Grafo.Thrift;
import Grafo.Vertice;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class Handler implements Thrift.Iface {

    private ConcurrentHashMap<Integer, Vertice> HashVertice;
    private int id;
    Node node, root;

    public Handler(String args[]) throws TException {
        this.HashVertice = new ConcurrentHashMap<Integer, Vertice>();
        int port = Integer.parseInt(args[1]); //Porta do nó que quer entrar
        int rootPort = Integer.parseInt(args[3]); //Porta do nó raiz

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        
        node = new Node();
        node.setFt(new ArrayList<Finger>());
        node.setIp(args[0]);
        node.setPort(port);

        if (args[2].equals(node.getIp()) && (port == rootPort)) {
            int a = (int) (Math.random() * Math.pow(2, 5));
            node.setId(a);
            join(node);
            System.out.println("Nó raiz estabelecido com sucesso - ID: " + a);
        } else {
            TTransport transport = new TSocket(args[2], Integer.parseInt(args[3]));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            Chord.Client client = new Chord.Client(protocol);
            root = client.sendSelf();
            transport.close();

            //node.setId(getValidID(root)); pra que isso não entendi.
            System.out.println("Nó estabelecido com sucesso - ID: " + root.getId());
            join(root);
        }

        id = (int) (Math.random() * Math.pow(2, 5));

    }

    @Override
    public boolean addVertice(Vertice v) throws TException {
        System.out.println("id: " + id);

        if (this.HashVertice.putIfAbsent(v.nome, v) == null) {
            return true;
        }

        return false;
    }

    @Override
    public Vertice readVertice(int nome) throws TException, KeyNotFound {

        Vertice v = HashVertice.computeIfPresent(nome, (a, b) -> {
            return b;
        });

        if (v != null) {
            return v;
        }

        throw new KeyNotFound();

    }

    @Override
    public boolean updateVertice(Vertice v) throws KeyNotFound, TException {
        try {
            Vertice vertice = readVertice(v.getNome());

            synchronized (vertice) {
                vertice.setCor(v.getCor());
                vertice.setDescricao(v.getDescricao());
                vertice.setPeso(v.getPeso());
                return true;
            }

        } catch (KeyNotFound e) {
            return false;
        }
    }

    @Override
    public boolean deleteVertice(Vertice v) throws KeyNotFound, TException {
        Vertice vertice;
        Aresta a;
        synchronized (v) {
            for (Integer key : v.HashAresta.keySet()) {
                a = this.readAresta(v.HashAresta.get(key).getV1(), v.HashAresta.get(key).getV2());
                this.deleteAresta(a);
            }
            if (HashVertice.remove(v.getNome()) != null) {
                return true;
            }
            return false;
        }
    }

    @Override
    public List<Vertice> readAllVertice() throws TException {
        ArrayList<Vertice> Vertices = new ArrayList<>();

        for (Integer key : HashVertice.keySet()) {
            Vertices.add(this.readVertice(key));
        }

        return Vertices;
    }

    @Override
    public List<Vertice> readVerticeNeighboors(Vertice v) throws TException {
        ArrayList<Vertice> Vertices = new ArrayList<>();

        for (Integer key : v.HashAresta.keySet()) {
            Vertices.add(this.readVertice(v.HashAresta.get(key).v2));
        }

        return Vertices;
    }

    @Override
    public boolean addAresta(Aresta a) throws TException {
        Vertice v;
        v = this.readVertice(a.getV1());

        if (v.HashAresta.putIfAbsent(a.getV2(), a) == null) {
            return true;
        }

        return false;
    }

    @Override
    public Aresta readAresta(int nomeV1, int nomeV2) throws TException {
        Vertice vertice;
        vertice = this.readVertice(nomeV1);

        Aresta aresta;
        aresta = vertice.HashAresta.computeIfPresent(nomeV2, (a, b) -> {
            return b;
        });

        if (aresta != null) {
            return aresta;
        }

        throw new KeyNotFound();

    }

    @Override
    public List<Aresta> readAllAresta() throws TException { // Tratar concorrência
        ArrayList<Aresta> Arestas = new ArrayList<>();

        for (Integer keyVertice : HashVertice.keySet()) {
            synchronized (keyVertice) {
                for (Integer keyAresta : HashVertice.get(keyVertice).HashAresta.keySet()) {
                    Arestas.add(HashVertice.get(keyVertice).HashAresta.get(keyAresta));
                }
            }
        }
        return Arestas;
    }

    @Override
    public List<Aresta> readAllArestaOfVertice(Vertice v) throws TException { // Tratar concorrência
        ArrayList<Aresta> Arestas = new ArrayList<>();
        Vertice vertice;

        for (Integer key : v.HashAresta.keySet()) {
            vertice = this.readVertice(v.HashAresta.get(key).getV2());
            Arestas.add(this.readAresta(v.getNome(), vertice.getNome()));
        }

        return Arestas;
    }

    @Override
    public boolean updateAresta(Aresta a) throws KeyNotFound, TException {
        try {
            Aresta aresta = this.readAresta(a.v1, a.v2);

            synchronized (aresta) {
                aresta.setDescricao(a.descricao);
                aresta.setDirect(a.isDirect());
                aresta.setPeso(a.getPeso());
                return true;
            }

        } catch (KeyNotFound e) {
            return false;
        }
    }

    @Override
    public boolean deleteAresta(Aresta a) throws KeyNotFound, TException {
        synchronized (a) {
            Vertice v1 = this.readVertice(a.getV1());
            Vertice v2 = this.readVertice(a.getV2());

            v1.HashAresta.remove(v2.getNome());
            v2.HashAresta.remove(v1.getNome());

            return true;
        }
    }

    @Override
    public void join(Node n) throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node getSucessor(int id) throws TException {
        Node node = getPredecessor(id);
        return node;
    }

    @Override
    public Node getPredecessor(int id) throws TException {
        node N = 
    }

    @Override
    public Node closestPrecedingFinger(int id) throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transferKeys(Node n) throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stabilize() throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notify(Node n) throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fixFingers() throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node sendSelf() throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPredecessor(Node n) throws TException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void verifyID(Node node) throws TException {

        int a = (int) (Math.random() * Math.pow(2, 5));
        node.setId(a);
        
        Node compare = getSucessor(a);
        
        if (node.getId() == getSucessor(node))

    }
}
