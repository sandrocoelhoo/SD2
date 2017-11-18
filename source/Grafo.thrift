namespace java Grafo

struct Aresta {
	1:i32 v1,
	2:i32 v2,
	3:double peso,
	4:bool direct,
	5:string descricao
}

struct Vertice {
	1:i32 nome,
	2:i32 cor,
	3:string descricao,
	4:double peso,
	5:map<i32,Aresta> HashAresta

}

exception KeyNotFound {
}

struct Node{
    1:i32 id,
    2:list<Finger> ft,
    3:Finger pred,
    4:string ip,
    5:i32 port
}

struct Finger{
    1:i32 id,
    2:string ip,
    3:i32 port
}


service Chord {
    void join(1:Node n),
    Node getSucessor(1:i32 id),
    Node getPredecessor(1:i32 id),
    Node closestPrecedingFinger(1:i32 id),
    void transferKeys(1:Node n),
    void stabilize(),
    void notify(1:Node n),
    void fixFingers(),
    Node sendSelf(),
    void setPredecessor(1:Node n)
}

service Thrift extends Chord{
	bool addVertice(1:Vertice v) throws (1:KeyNotFound knf),
	Vertice readVertice(1:i32 nome) throws (1:KeyNotFound knf),
	bool updateVertice(1:Vertice v) throws (1:KeyNotFound knf),
	bool deleteVertice(1:Vertice v) throws (1:KeyNotFound knf),
	list<Vertice> readAllVertice() throws (1:KeyNotFound knf),
	list<Vertice> readVerticeNeighboors(1:Vertice v) throws (1:KeyNotFound knf),

	bool addAresta(1:Aresta a) throws (1:KeyNotFound knf),
	Aresta readAresta(1:i32 nomeV1, 2:i32 nomeV2) throws (1:KeyNotFound knf),
	list<Aresta> readAllAresta() throws (1:KeyNotFound knf),
	list<Aresta> readAllArestaOfVertice(1:Vertice v) throws (1:KeyNotFound knf),
	bool updateAresta(1:Aresta a) throws (1:KeyNotFound knf),
	bool deleteAresta(1:Aresta a) throws (1:KeyNotFound knf),	
}
