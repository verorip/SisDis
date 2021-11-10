package beans;


import javax.xml.bind.annotation.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Edge_Positions {

    private Node_Edge root;
    @XmlElement(name="node_list")
    private ArrayList<Node_Edge> City_Nodes;
    private static Edge_Positions instance=null;


    public Edge_Positions(){
        City_Nodes= new ArrayList<>();
        root=null;
    }

    public synchronized static Edge_Positions getInstance(){
        if(instance==null)
            instance = new Edge_Positions();
        return instance;
    }

    public synchronized Node_Edge add_Node(Node_Edge e){
        boolean checked=false;
        int attempts=0;
        int dist=0;
        while(!checked){
            checked=true;
            inner: for (Node_Edge existing : City_Nodes) {
                dist = Calculate_Distance(existing,e);
                if(dist<20) {
                    if (attempts < 10) {
                        boolean success = Change_Position(e, existing, dist);
                        if (!success)
                            return null;
                        attempts++;
                        checked = false;
                        break inner;
                    } else return null;
                }
            }
        }
        City_Nodes.add(e);
        System.out.println("aggiungo in posizione "+e.getPosition() + ",  con portaa: "+ e.getPortIn());
        Node_Edge parent = Update_Tree(e);
        return parent;
    }

    private boolean Change_Position(Node_Edge e, Node_Edge existing, int distance) {
        if(e.getPosition().getX()<=(100-20+distance) && e.getPosition().getX()>=existing.getPosition().getX()){
            e.getPosition().setX(e.getPosition().getX()+20-distance);
            return true;
        }
        else if(e.getPosition().getX()>=(0+20-distance) && e.getPosition().getX()<=existing.getPosition().getX()){
            e.getPosition().setX(e.getPosition().getX()-20+distance);
            return true;
        }
        else if(e.getPosition().getY()<=(100-20+distance) && e.getPosition().getY()>=existing.getPosition().getY()){
            e.getPosition().setY(e.getPosition().getY()+distance);
            return true;
        }
        else if(e.getPosition().getY()>=(0+20-distance) && e.getPosition().getY()<=existing.getPosition().getY()){
            e.getPosition().setY(e.getPosition().getY()-distance);
            return true;
        }
        return false;
    }

    public synchronized Node_Edge Update_Tree(Node_Edge e){
        if(root==null){
            root=e;
            e.setType("root");
        }
        else {
            e.setType("data");
            Node_Edge Parent=null;
            Node_Edge current;
            ArrayList<Node_Edge> Unvisited = new ArrayList<Node_Edge>();
            Unvisited.add(root);
            do{
                current=Unvisited.get(0);
                Unvisited.remove(0);
                if(current.getSons()!=null && current.getSons().size()<2){
                    current.addSon(e);
                    e.setType("data");
                    e.setPadre(Integer.toString(current.getId()));
                    return current;
                }
                else{
                    if(current.getSons()!=null)
                        Unvisited.addAll(current.getSons());
                }
            }while(!Unvisited.isEmpty());
        }
        return e;
    }

    public synchronized boolean New_Root(int id){
        boolean finded=false;
        ArrayList<Node_Edge> Orphans = new ArrayList<Node_Edge>();
        for (Node_Edge e : City_Nodes) {
            if (e.getId() == id) {
                e.setType("root");
                finded=true;
                //caso nel quale il nuovo nodo root sia un figlio del vecchio root
                //in quel caso basta aggiungere i figli del veccio root al nuovo
                if(root.getSons()!=null && root.getSons().size()>0 && root.getSons().contains(e)){
                    for(Node_Edge son : root.getSons()){
                        if(son.getId()!=e.getId()){
                            son.setPadre(Integer.toString(e.getId()));
                            e.addSon(son);
                        }
                    }
                    root=e;
                }
                //caso nel quale sia un nodo non figlio di root in quel caso bisogna ggiornare i padri e i figli
                else{
                    if(e.getSons()!=null && e.getSons().size()>0)
                        Orphans.addAll(e.getSons());
                    e.setSons(new ArrayList<Node_Edge>());
                   // System.out.println("figli di root---> "+root.getSons().size());
                    e.setSons(root.getSons());
                   // System.out.println("figli qw: "+e.getSons().size());
                    //aggiusto i padri dei figli del nuovo root
                    for(Node_Edge p : e.getSons()){
                        p.setPadre(Integer.toString(e.getId()));
                    }
                    //collego gli orfani al nodo di cui prima era padre, eliminando il nuovo coordinatore dai figli
                    for (Node_Edge ex : City_Nodes){
                        if(ex.getId()==Integer.parseInt(e.getPadre())){
                           // System.out.println("nodo ex");
                            if(ex.getSons().contains(e))
                               // System.out.println("a: "+ex.getId() +"  b: "+e.getPadre() + "dentro");
                            ex.removeSon(e);
                            if(Orphans!=null && Orphans.size()>0){
                                for(Node_Edge adopted : Orphans){
                                   // System.out.println("Aggiungo orfano: "+adopted.getId());
                                    adopted.setPadre(Integer.toString(ex.getId()));
                                }
                                ex.addSons(Orphans);
                            }
                        }
                    }

                    root=e;
                }
                e.setPadre("0");
            }
        }
        return finded;
    }

    public synchronized boolean remove_Node(int id){
        int i=1;
        for (Node_Edge e : City_Nodes){
            if(e.getId()==id){
                //caso nel quale sia il nodo root ad uscire non lo rimuovo dall'albero (mi serve poi quando sarà scelto il nuovo coordinatore)
                if(e.getType().equals("root")){
                   // System.out.println("è root");
                    e.setType("dead_root");
                    e.setId(-1);
                    e.setPortIn(0);
                }
                //caso in cui sia il nodo non root ad uscire
                else{
                   // System.out.println("non è root");
                    String padre = e.getPadre();
                    Node_Edge Parent=null;
                    for(Node_Edge n: City_Nodes){
                        if(n.getId()==Integer.parseInt(padre)){
                            Parent = n;
                            Parent.removeSon(e);
                            ArrayList<Node_Edge> tempSons = e.getSons();
                            //controllo che non sia una foglia
                            if(tempSons!=null || !tempSons.isEmpty()){
                                for( Node_Edge s : tempSons){
                                    s.setPadre(Integer.toString(Parent.getId()));
                                    Parent.addSon(s);
                                }
                            }
                            //break inner;
                        }
                    }
                }
                City_Nodes.remove(e);
                i=1;
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<Node_Edge> getNodeList(){
        return this.City_Nodes;
    }

    public synchronized int Calculate_Distance(Node_Edge a, Node_Edge b){
        int distance=0;
        distance=(Math.abs(a.getPosition().getX()-b.getPosition().getX()))+(Math.abs(a.getPosition().getY()-b.getPosition().getY()));
        return distance;
    }

    public synchronized Node_Edge getParent(int id){
        for (Node_Edge e : City_Nodes){
            if(e.getId()==id)
                for(Node_Edge n : City_Nodes){
                    if(e.getPadre()!=null && n.getId()== Integer.parseInt(e.getPadre())){
                        return n;
                    }
                }
        }
        return null;
    }

    public synchronized Node_Edge getNodeById(int id){
        for (Node_Edge e : City_Nodes){
            if(e.getId()==id){
                return e;
            }
        }
        return null;
    }

    public synchronized Node_Edge getNearestNode(Point position) {
        Node_Edge target = null;
        int temp, mindistance = 1000;
        Node_Edge current;
        ArrayList<Node_Edge> Unvisited = new ArrayList<Node_Edge>();
        Unvisited.add(root);
        do {
            current = Unvisited.get(0);
            Unvisited.remove(0);
            //System.out.println("estraggo nodo dalla coda e guardo al distanza che è: ");
            if(current!=null && current.getSons()!=null && current.getSons().isEmpty()){
                temp=Math.abs(current.getPosition().getX()-position.getX())+Math.abs(current.getPosition().getY()-position.getY());
                //System.out.println("Nodo in posizione : "+ current.getPosition()+"  a distanza: "+temp);
                if(temp<mindistance){
                    target=current;
                    mindistance=temp;
                }
            }
            else if (current!=null && current.getSons() != null && !current.getSons().isEmpty())
                Unvisited.addAll(current.getSons());
        } while (!Unvisited.isEmpty());
        return target;
    }

    public synchronized int getRootID(){
        return this.root.getId();
    }
}
