package Handlers;

import Grafo.Aresta;
import Grafo.KeyNotFound;
import Grafo.MetodosGrafo;
import Grafo.Vertice;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.thrift.TException;

public class GrafoHandler implements MetodosGrafo.Iface {

    private ConcurrentHashMap<Integer, Vertice> HashVertice;

    public GrafoHandler() {
        this.HashVertice = new ConcurrentHashMap<Integer, Vertice>();
    }

    @Override
    public boolean addVertice(Vertice v) throws TException {
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
                //vertice = this.readVertice(v.HashAresta.get(key).getV2());
                //System.out.println(vertice);
                //vertice.HashAresta.remove(key);
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

        //hashvertice.values(); retorna uma lista. Cast de arraylist.
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
            synchronized(keyVertice){
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
}
