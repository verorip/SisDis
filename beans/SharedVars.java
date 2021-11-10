package beans;

import java.util.ArrayList;

public class SharedVars {

    private volatile int ParentPort;
    private volatile int ParentId;
    private volatile String ParentType;
    private volatile boolean Voting;
    private volatile Message SendVotes;
    private volatile ArrayList<Node_Edge> AllNodes;
    private volatile Node_Edge NewCordinator;
    private volatile ArrayList<ServerElaboration> AllServers;
    private volatile ArrayList<SensorListener> Sensors;
    private volatile NodeServer ServerListen;
    private volatile ServerSensor ServerSensor;
    private volatile NodeClient ClientSend;
    private volatile boolean Respond;

    public SharedVars(){
        ParentPort=0;
        ParentId=0;
        ParentType=null;
        AllServers=new ArrayList<ServerElaboration>();
        Sensors=new ArrayList<>();
    }

    public synchronized void setParentPort(int port){
        this.ParentPort=port;
    }

    public synchronized int getParentPort(){
        return ParentPort;
    }

    public synchronized int getParentId() {
        return ParentId;
    }

    public synchronized void setParentId(int parentId) {
        ParentId = parentId;
    }

    public synchronized String getParentType() {
        return ParentType;
    }

    public synchronized void setParentType(String parentType) {
        ParentType = parentType;
    }

    public synchronized boolean getVoting() {
        return Voting;
    }

    public synchronized void setVoting(boolean voting) {
        Voting = voting;
    }

    public synchronized Message getSendVotes() {
        return SendVotes;
    }

    public synchronized void setSendVotes(Message sendVotes) {
        SendVotes = sendVotes;
    }

    public synchronized ArrayList<Node_Edge> getAllNodes() {
        return AllNodes;
    }

    public synchronized void setAllNodes(ArrayList<Node_Edge> allNodes) {
        AllNodes = allNodes;
    }

    public synchronized void AddServer(ServerElaboration s){
        AllServers.add(s);
    }

    public synchronized Node_Edge getNewCordinator() {
        return NewCordinator;
    }

    public synchronized void setNewCordinator(Node_Edge newCordinator) {
        NewCordinator = newCordinator;
    }

    public synchronized ArrayList<ServerElaboration> getAllServers() {
        return AllServers;
    }

    public synchronized void setAllServers(ArrayList<ServerElaboration> allServers) {
        AllServers = allServers;
    }

    public synchronized NodeServer getServerListen() {
        return ServerListen;
    }

    public synchronized void setServerListen(NodeServer serverListen) {
        ServerListen = serverListen;
    }

    public synchronized NodeClient getClientSend() {
        return ClientSend;
    }

    public synchronized void setClientSend(NodeClient clientSend) {
        ClientSend = clientSend;
    }

    public synchronized boolean isRespond() {
        return Respond;
    }

    public synchronized void setRespond(boolean respond) {
        Respond = respond;
    }

    public synchronized ServerSensor getServerSensor() {
        return ServerSensor;
    }

    public synchronized void setServerSensor(ServerSensor serverSensor) {
        ServerSensor = serverSensor;
    }

    public synchronized ArrayList<SensorListener> getSensors() {
        return Sensors;
    }

    public synchronized void setSensors(ArrayList<SensorListener> sensors) {
        Sensors = sensors;
    }

    public synchronized void AddServerS(SensorListener s){
        Sensors.add(s);
    }
}
