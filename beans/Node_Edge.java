package beans;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;


@XmlRootElement
public class Node_Edge implements Serializable {

    private int id;
    private String ipadress;
    private int portIn;
    private int portOut;
    private Point position;
    private String type; //per indicare se è un nodo generico (data) o il nodo che comunica con il server cloud (root)
    private String padre;
    private ArrayList<Node_Edge> Sons=new ArrayList<Node_Edge>();



    public Node_Edge(){}

    public Node_Edge(int id, String IP, int port_in, int port_out, Point position, String type, String Padre) {
        this.id=id;
        this.ipadress =IP;
        this.portIn =port_in;
        this.portOut =port_out;
        this.position=position;
        this.type=type;
        this.padre=Padre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpadress() {
        return ipadress;
    }

    public void setIpadress(String ipadress) {
        this.ipadress = ipadress;
    }

    public int getPortIn() {
        return portIn;
    }

    public void setPortIn(int portIn) {
        this.portIn = portIn;
    }

    public int getPortOut() {
        return portOut;
    }

    public void setPortOut(int portOut) {
        this.portOut = portOut;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getPadre() {
        return padre;
    }

    public void setPadre(String padre) {
        this.padre = padre;
    }

    public ArrayList<Node_Edge> getSons() {
        return Sons;
    }

    public void setSons(ArrayList<Node_Edge> sons) {
        Sons = sons;
    }

    public void addSons(ArrayList<Node_Edge> sons){ Sons.addAll(sons);}

    public void addSon(Node_Edge new_son){
        Sons.add(new_son);
    }

    public void removeSon(Node_Edge old_son){   Sons.remove(old_son);

    }
}
