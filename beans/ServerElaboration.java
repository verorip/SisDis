package beans;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class ServerElaboration extends Thread{

    private String id;
    private int Port;
    private Socket connectionsocket;
    private ArrayList<Statistics> bufferSender;
    private ArrayList<Statistics> bufferReciver;
    private Object syncLock, ArrayLock, ElectionLock;
    private ObjectInputStream inStream;
    private MeasureData NewDate;
    private SharedVars vars;
    private Statistics s;
    private boolean reciveFromNode=false;
    private Gson g;

    public ServerElaboration(String id, int port, SharedVars vars, Socket connectionsocket, ArrayList<Statistics> bufferSender, ArrayList<Statistics> bufferReciver, Object syncLock, Object ElectionLock, Object ArrayLock, Thread thread) {
        this.id=id;
        this.Port=port;
        this.connectionsocket=connectionsocket;
        this.bufferSender=bufferSender;
        this.bufferReciver=new ArrayList<>();
        this.syncLock=syncLock;
        this.ArrayLock=ArrayLock;
        this.NewDate=new MeasureData(id);
        this.s=new Statistics(id);
        this.vars = vars;
        //this.thread=thread;
        this.ElectionLock=ElectionLock;
        this.g=new Gson();
    }

    @Override
    public void run(){
        System.out.println(("NodeServer: Un Nodo si è collegato"));
        int i=0, total;
        boolean interruptor=false;
        try {
            inStream = new ObjectInputStream(connectionsocket.getInputStream());
            do {
                if(Thread.currentThread().isInterrupted()){
                    //System.out.println("\nsono nell'interrupt\n");
                    if (s!=null && !interruptor && bufferReciver.size()>0){
                        StaticsAggregation();
                        s.setTimestamp(deltaTime());
                    }
                    synchronized (ArrayLock) {
                        bufferSender.add(s);
                    }
                    synchronized (syncLock){
                        syncLock.notify();
                    }
                    if(!interruptor)
                        vars.getClientSend().interrupt();
                    //System.out.println("interrompo il thread");
                    connectionsocket.close();
                    break;
                }
                Object input;
                input= inStream.readObject();
                if(((String)input).contains("Mean")){
                    Statistics temp = g.fromJson(((String)input), Statistics.class);
                     reciveFromNode=true;
                     i++;
                     if(vars.getParentPort()==1337){
                         System.out.print(i+": Statistica Locale da nodo");
                         System.out.print("---->>>> " + temp.getId()+" Media:"+temp.getMean()+" StdDev:"+temp.getStandardDeviation());
                         System.out.println(" alle "+
                                 TimeUnit.MILLISECONDS.toHours(temp.getTimestamp())+":"+(TimeUnit.MILLISECONDS.toMinutes(temp.getTimestamp())-
                                 TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(temp.getTimestamp()))+":"+(TimeUnit.MILLISECONDS.toSeconds(temp.getTimestamp()) -
                                 TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(temp.getTimestamp())))+"\n"));

                     }
                     bufferReciver.add(temp);
                     synchronized (ArrayLock){
                         bufferSender.add(temp);
                         synchronized (syncLock){
                             syncLock.notify();
                         }
                     }
                    if (i >= 40) {
                        i=0;
                        total=0;
                        for (Statistics sd: bufferReciver) {
                            total+=sd.getPopulation_Number();
                        }
                        //System.out.println("\nstmapo il totale: "+total);
                        s.setPopulation_Number(total);
                        StaticsAggregation();
                        s.setType("local");
                        s.setTimestamp(deltaTime());
                        System.out.println("Statistica Generata--> Nodo: "+ s.getId()+" Media aggr: "+s.getMean()+"  devstand aggr: "+s.getStandardDeviation());
                        synchronized (ArrayLock){
                            bufferSender.add(s);
                            synchronized (syncLock){
                                syncLock.notify();
                            }
                        }
                        bufferReciver.clear();
                        s=null;
                        s=new Statistics(id);
                        /*if(NewDate.getNodeData()!=null){
                            for (MeasureData md: NewDate.getNodeData()) {
                                total+=md.getSamples_num();
                            }
                            System.out.println("\nstmapo il totale: "+total);
                            NewDate.setSamples_num(total);
                            StaticsAggregation();
                            System.out.println("Media aggr: "+NewDate.getMedia()+"  devstand aggr: "+NewDate.getStandard_Deviation());
                            NewDate.setTimestamp(deltaTime());
                            synchronized (ArrayLock){
                                bufferSender.add(NewDate);
                                synchronized (syncLock){
                                    syncLock.notify();
                                }
                            }
                            NewDate=null;
                            NewDate=new MeasureData(id);
                        }*/
                    }
                }
                else if(((String)input).contains("Header")){
                    Message m=g.fromJson(((String)input), Message.class);
                    //System.out.println("ho ricevuto un message");
                    //Nel caso un nodo si sia proclamato coordinatore
                    if(m.getHeader().equals("NewCoordinator")){
                        //System.out.println("di NuovoCoordinatore!! da: "+ connectionsocket.getPort());
                        synchronized (ElectionLock) {
                            if (!vars.getVoting()) {
                                //se non stava votando significa che entra ora in elezione e resetto i campi
                                vars.setRespond(false);
                                vars.setVoting(true);
                            }
                            vars.setNewCordinator(new Node_Edge(m.getId(), "localhost", m.getPort(), 0, null, null, null));
                            //aggiorno l'id e la porta del nuovo coordinatore solo se il nodo corrente è un figlio di root (altrimenti è inutile)
                            if (vars.getParentType().equals("root")) {
                                synchronized (ElectionLock) {
                                    //System.out.println("notifico a tutti");
                                    ElectionLock.notifyAll();
                                }
                                vars.setParentPort(m.getPort());
                                vars.setParentId(m.getId());
                            }
                            //tutti gli altri nodi chiedono il proprio genitore nel caso sia cambiato
                            else{
                                Client client = Client.create();
                                WebResource webResource = client.resource("http://localhost:1337/node/get/"+id);
                                ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                                if(response.getStatus()==200){
                                    Node_Edge NewParent=response.getEntity(Node_Edge.class);
                                    if(vars.getParentId()!=NewParent.getId()) {
                                        vars.setParentId(NewParent.getId());
                                        vars.setParentPort(NewParent.getPortIn());
                                        vars.setParentType(NewParent.getType());
                                       /* System.out.println(vars.getParentId() + " " + vars.getParentPort() + " " + vars.getParentType());
                                        System.out.println("\nnuovo padre impostato dal ServerElaboration");*/
                                    }
                                }
                                /*else{
                                    System.out.println("non sono stati trovati nuovi nodi padre");
                                }*/
                            }
                            vars.setRespond(false);
                            vars.setVoting(false);
                            ObjectOutputStream out = new ObjectOutputStream(connectionsocket.getOutputStream());
                            Message resp = new Message("Ok");
                            String JSON= g.toJson(resp, Message.class);
                            out.writeObject(JSON);
                        }
                    }
                     else if(m.getHeader().equals("Election")){
                        //System.out.println("di elezione!!");
                        //necessario acnhe se i metodi sono syncronized perchè mi server che sia garantita l'atomicità del controllo e assegnazione
                        synchronized (ElectionLock){
                            if(!vars.getVoting()){
                                //System.out.println("setto il voting");
                                vars.setVoting(true);
                                //se non stava votando significa che entra ora in elezione e resetto i campi
                                vars.setRespond(false);
                                vars.setNewCordinator(null);
                                vars.setAllNodes(m.getAllNodes());
                                new ElectionThread(id,vars, ElectionLock, Port, true).start();
                            }
                        }
                        //System.out.println("ho creaot i threads");
                        //la risposta viene spedita dopo onde evitare che il thread attuale si entri nel catch dato che il nodo che l'ha contattato chiude la connessione quando riceve la risposta
                        ObjectOutputStream out = new ObjectOutputStream(connectionsocket.getOutputStream());
                        Message resp=new Message("Alive");
                        String JSON=g.toJson(resp, Message.class);
                        out.writeObject(JSON);
                    }
                    else if(m.getHeader().equals("Remove")){
                        if(vars.getAllServers().size()>1)
                            interruptor=true;
                        //System.out.println("che mi dice di rimuovermi");
                        vars.getServerListen().interrupt();
                        vars.getServerSensor().interrupt();
                        for(ServerElaboration s : vars.getAllServers()){
                            //System.out.println("sono nel for each");
                            s.interrupt();
                        }
                        for(SensorListener sl: vars.getSensors()){
                            //System.out.println("sono nel for each2");
                            sl.interrupt();
                        }
                        //apro una connessione tmeporanea solo per sbloccare l'accept
                        Socket closing=new Socket("localhost", connectionsocket.getLocalPort());
                        closing.close();
                        int port1=connectionsocket.getLocalPort()+1;
                        closing=new Socket("localhost", port1);
                        closing.close();
                        /*connectionsocket.close();
                        inStream.close();
                        Thread.currentThread().interrupt();//preserve the message*/
                    }
                }
                /*else{
                    System.out.println("---------->>non riconosciuto<<<<<------- " + ((String)input));
                }*/
            }while (true);
            //con il break che viene eseguito all'inizio del ciclo se il thread è staot interrotto chiudo la connessione
            //connectionsocket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            //System.out.println("EOF\n");
            try {
                //se si perde la connesione spedisco gli ultimi dati ricevuti (tipicamente perchè il nodo figlio ha chiuso la connessione
                if(!reciveFromNode){
                    if(s!=null && s.getPopulation_Number()>0)
                    {
                        StaticsAggregation();
                        s.setTimestamp(deltaTime());
                        synchronized (ArrayLock){
                            bufferSender.add(s);
                            synchronized (syncLock){
                                syncLock.notify();
                            }
                        }
                    }
                }
                connectionsocket.close();
                //thread.interrupt();
                //Thread.currentThread().interrupt();//preserve the message
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Server exiting...");
    }

    private void StaticsAggregation() {
        s.setMean(getAggregateMean());
        s.setStandardDeviation(getAggrStdDev());
    }

    private double getAggrStdDev() {
        return Math.sqrt(getAggregateVariance());
    }

    private double getAggregateVariance(){
        double num = 0.00;
        double denum = 0.00;
        double total;
        for(Statistics md : bufferReciver){
            num+=md.getPopulation_Number()*(md.getStandardDeviation()+((md.getMean()-s.getMean())*(md.getMean()-s.getMean())));
            denum+=md.getPopulation_Number();
        }
        total=num/denum;
        return total;
    }

    private double getAggregateMean() {
        double num = 0.00;
        double denum = 0.00;
        double sum;
        for(Statistics md : bufferReciver){
            num+=md.getMean()*md.getPopulation_Number();
            denum+=md.getPopulation_Number();
        }
        sum=num/denum;
        return sum;
    }


    private long computeMidnightMilliseconds(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        //System.out.println("aggiungo timestamp");
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long deltaTime(){
        return System.currentTimeMillis()-computeMidnightMilliseconds();
    }
}
