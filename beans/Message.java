package beans;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;

public class Message implements Serializable {

    private String Header;
    private String Ip;
    private int Port;
    private int Id;
    private ArrayList<Node_Edge> AllNodes;
    private long Timestamp;

    //costruttore usato per messaggio per notificare il nuovo coordinatore
    public Message(String Header, String ip, int Port, int Id, long Timestamp){
        this.Header=Header;
        this.Ip=ip;
        this.Port=Port;
        this.Id=Id;
        this.Timestamp=Timestamp;
        this.AllNodes=new ArrayList<Node_Edge>();
    }

    //costruttore usato per l'elezione
    public Message(String Header, ArrayList<Node_Edge> AllNodes, long Timestamp){
        this.Header=Header;
        this.Timestamp=Timestamp;
        this.AllNodes=AllNodes;
    }

    //costruttore usato per messaggi "semplici" come ACK o Alive
    public Message(String Header){
        this.Header=Header;
    }


    /*
    Getter and setters
     */

    public String getHeader() {
        return Header;
    }

    public void setHeader(String header) {
        Header = header;
    }

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.Timestamp= timestamp;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public ArrayList<Node_Edge> getAllNodes() {
        return AllNodes;
    }

    public void setAllNodes(ArrayList<Node_Edge> allNodes) {
        AllNodes = allNodes;
    }
}
