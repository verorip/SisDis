package beans;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.ws.Response;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NodeClient extends Thread {

    private String id;
    private SharedVars vars;
    private ArrayList<Statistics> bufferSender;
    private ArrayList<Statistics> bufferReciver, temp;
    private Object syncLock, ArrayLock, ElectionLock;
    private int Port;
    private boolean empty;
    private int Timeout;
    private boolean term=false;
    private Gson g;

    public NodeClient(String id, int Port, SharedVars vars, ArrayList<Statistics> bufferSender, ArrayList<Statistics> bufferReciver, Object syncLock, Object ArrayLock, Object ElectionLock) {
        this.id=id;
        this.vars = vars;
        this.bufferSender=bufferSender;
        this.bufferReciver=bufferReciver;
        this.syncLock=syncLock;
        this.ArrayLock=ArrayLock;
        this.ElectionLock=ElectionLock;
        this.temp=new ArrayList<Statistics>();
        this.Port=Port;
        this.Timeout=10000;
        g=new Gson();
    }

    public void run(){
        Socket ToSocket = null;
        int tries=0;
        String KeepAlive="KeepAlive";
        boolean NotRespond=false;
        ObjectOutputStream outStream = null;
        //System.out.println("\nsono client, invio sulla porta "+ vars.getParentPort());
        while(true){
            if(vars.getParentPort()==1337){
                if(ToSocket!=null && ToSocket.isConnected()) {
                    try {
                        ToSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Timeout=5000;
                SendToCloud();
            }
            if(Thread.currentThread().isInterrupted() || term){
                if(ToSocket!=null) {
                    try {
                        ToSocket.close();
                    } catch (IOException e) {
                        /*System.out.println("client già chiuso");
                        e.printStackTrace();*/
                    }
                    break;
                }
            }
            //nel caso che il nodo a cui vengono spediti i dati non risponde più ci sono due casi:
            if(NotRespond){
                //se il padre era la radice ovveor il cordinatore bisogna indire un'elezione
                if(vars.getParentType().equals("root")){
                    vars.setVoting(true);
                    vars.setRespond(false);
                    vars.setNewCordinator(null);
                    Client client = Client.create();
                    WebResource webResource = client.resource("http://localhost:1337/node");
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    if (response.getStatus() != 200) {
                        System.out.println("Errore:  "+response.getStatus());
                    }
                    else{
                        try {
                            //ottengo la lista di tutti i nodi presenti nella rete
                            JSONObject JSONResponse =response.getEntity(JSONObject.class);
                            JSONArray NodeList= JSONResponse.getJSONArray("node_list");
                            ArrayList<Node_Edge> Nodes=new ArrayList<Node_Edge>();
                            for(int j=0; j<NodeList.length(); j++){
                                JSONObject obj = (JSONObject) NodeList.get(j);
                                Node_Edge temp= new Gson().fromJson(obj.toString(), Node_Edge.class);
                                //System.out.println(temp.getIpadress()+" "+temp.getPortIn()+" "+temp.getSons()+" "+temp.getType());
                                //if(temp.getId()!=Integer.parseInt(this.id)){
                                    Nodes.add(temp);
                                //}
                            }
                            vars.setAllNodes(Nodes);
                            ElectionThread Election= new ElectionThread(id,vars, ElectionLock, Port, true);
                            Election.start();
                            //System.out.println("sono il client che ha avviato l'elezione e mi metto in attesa che temrini");
                            //metto il thread in attesa che venga scelto il nuovo coordinatore, altrimenti continuerebbe a ciclare non potendo spedire
                            try {
                                Election.join();
                            } catch (InterruptedException e) {
                                //System.out.println("Join interrotta");
                                e.printStackTrace();
                            }
                            //System.out.println("Sono client e ho terminato la join");
                            NotRespond=false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //altrimenti bisogna semplicemente chiedere al cloud la porta del padre perchè eventualemnte è cambiato (un nodo è uscito)
                else{
                    //System.out.println("cerco il nuovo padre");
                    Client client = Client.create();
                    WebResource webResource = client.resource("http://localhost:1337/node/get/"+id);
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    if(response.getStatus()==200){
                        Node_Edge NewParent=response.getEntity(Node_Edge.class);
                        vars.setParentId(NewParent.getId());
                        vars.setParentPort(NewParent.getPortIn());
                        vars.setParentType(NewParent.getType());
                        /*System.out.println(vars.getParentId()+" "+ vars.getParentPort()+" "+ vars.getParentType());
                        System.out.println("\nnuovo padre impostato");*/
                        NotRespond=false;
                        tries=0;
                    }
                    else{
                        System.out.println("non sono stati trovati nuovi nodi padre");
                    }
                }
            }
            try {
                if(ToSocket!=null && vars.getParentPort()!=ToSocket.getPort()){
                    ToSocket.close();
                    ToSocket=null;
                }
                if(ToSocket==null && vars.getParentPort()!=1337){
                    ToSocket = new Socket("localHost", vars.getParentPort());
                    System.out.println("Client Thread, spedisco a: "+ vars.getParentPort());
                    tries=0;
                    outStream = new ObjectOutputStream(ToSocket.getOutputStream());
                }
                synchronized (syncLock){
                    syncLock.wait(Timeout);
                }
                synchronized (ArrayLock){
                    temp.clear();
                    if (bufferSender.isEmpty())
                        empty=true;
                    else{
                        temp.addAll(bufferSender);
                        bufferSender.clear();
                    }
                }
                if(empty){
                    //System.out.println("mando un Keep Alive");
                    outStream.writeObject(KeepAlive);
                    empty=false;
                }
                else{
                    //System.out.println("mando i dati");
                    for (Statistics sc : temp) {
                        if(sc!=null && sc.getPopulation_Number()!=0 && !Double.isNaN(sc.getMean()) && !Double.isNaN(sc.getStandardDeviation())){
                            //System.out.print("spedisco: "+sc+"    "+sc.getId()+" "+sc.getMean()+" "+sc.getPopulation_Number()+" "+sc.getStandardDeviation());
                            /*System.out.println("Nodo: "+id+" Media: "+ sc.getMean()+"  devstand: "+sc.getStandardDeviation()+"alle "+
                                    TimeUnit.MILLISECONDS.toHours(sc.getTimestamp())+":"+(TimeUnit.MILLISECONDS.toMinutes(sc.getTimestamp())-
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sc.getTimestamp())))+":"+(TimeUnit.MILLISECONDS.toSeconds(sc.getTimestamp()) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sc.getTimestamp())))+"\n");*/
                            String JSON=g.toJson(sc, Statistics.class);
                            outStream.writeObject(JSON);
                        }

                    }
                    temp.clear();
                }
            } catch (IOException | InterruptedException e) {
                //System.out.println("\nsono nel catch\n");
                //e.printStackTrace();
                try {
                    /*if(outStream!=null)
                        outStream.close();*/
                    if(ToSocket!=null && ToSocket.isConnected())
                        ToSocket.close();
                    ToSocket=null;
                    tries++;
                    if(tries>=3)
                        NotRespond=true;
                    else
                        if(!Thread.currentThread().isInterrupted())
                            Thread.sleep(1000);//faccio aspettare il thread in modo da non saturare le richieste
                    System.out.println("\nIl nodo non rispode...");

                } catch (IOException | NullPointerException | InterruptedException e1) {
                    //System.out.println("sono nel catch sbagliato");
                    //e1.printStackTrace();
                }
            }
        }
    }

    public void SendToCloud(){
        Gson g=new Gson();
        Client client;
        WebResource webResource;
        ClientResponse response;
        String obj;
        System.out.println("Client Thread, spedisco a: "+ vars.getParentPort());
        while(!Thread.interrupted()){
            try {
                Thread.sleep(Timeout);
            } catch (InterruptedException e) {
                //System.out.println("timeout to cloud interruted");
                term=true;
                return;
            }
            if(bufferSender.size()>0){
                synchronized (ArrayLock){
                    temp.clear();
                    temp.addAll(bufferSender);
                    bufferSender.clear();
                }
                client = Client.create();
                for (Statistics sc:temp) {
                    if (sc.getMean()>0.0 && sc.getStandardDeviation()>0.0){
                        if(sc.getId().equals(id)){
                            sc.setType("Global");
                        }
                        String time=String.format("%02d:%02d:%02d\n\n", TimeUnit.MILLISECONDS.toHours(sc.getTimestamp()),(TimeUnit.MILLISECONDS.toMinutes(sc.getTimestamp())-
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sc.getTimestamp()))), (TimeUnit.MILLISECONDS.toSeconds(sc.getTimestamp()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sc.getTimestamp()))));
                        //System.out.printf("Al cloud dal Nodo: "+sc.getId()+" Media: "+ sc.getMean()+"  devstand: "+sc.getStandardDeviation()+"alle "+time);
                        obj=g.toJson(sc);
                        //System.out.println("sc: "+obj);
                        webResource = client.resource("http://localhost:1337/Cloud/send_to_cloud");
                        response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, obj);
                        if(response.getStatus()!=200){
                            System.out.println("Errore: "+ response);
                            System.out.println(response.getEntity(String.class));
                        }
                        else if(response.getStatus()==200){
                            //System.out.println("Spedito al cloud con successo");
                        }
                    }
                }
            }
            else{
                //System.out.println("buffer vuoto");
            }

        }
    }
}
