package beans;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.sound.sampled.Port;
import java.util.ArrayList;

public class ElectionThread extends Thread{

    private SharedVars vars;
    private String id;
    private Object ElectionLock;
    private int Port;
    //piccolo booleano usato per dare stampe capibili
    private boolean Starter;

    public ElectionThread(String id, SharedVars vars, Object ElectionLock, int Port, boolean Starter){
        this.id=id;
        this.vars=vars;
        this.ElectionLock=ElectionLock;
        this.Port=Port;
        this.Starter=Starter;
    }

    @Override
    public void run() {
        //System.out.println("avviaot electionthread");
        //uso questa variabile per vedere se ci sono nodi più grandi altirmenti si nomina coordinatore e una variabile per contare i tentativi prima di autoproclamarsi coordinatore
        int HighestNodes = 0, Attempts = 0;
        ArrayList<Node_Edge> Contacts = new ArrayList<Node_Edge>(), temp;
        temp = vars.getAllNodes();
        for (Node_Edge e : temp) {
            if (e.getId() > Integer.parseInt(id)) {
                HighestNodes++;
                Contacts.add(e);
            }
        }
        //System.out.println("Nodi con id più alto: " + HighestNodes);
        if (HighestNodes > 0) {
            elector: do {
                for (Node_Edge e : Contacts) {
                    if(e.getPortIn()!=Port) {
                        new VoteClient(id, e, vars, "Election").start();
                    }
                }
                //do il tempo di connettersi e rspondere
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    /*if (Starter)
                        System.out.println("Client con Thread id:" + Thread.currentThread().getId() + " interrotto la sleeeeeeep\"");
                    else
                        System.out.println("Server con Thread id:" + Thread.currentThread().getId() + " interrotto la sleeeeeeep\"");*/
                }
                if (!vars.isRespond()) {
                    //ha ricevuto almeno una risposta, il thread viene messo in attesa che qualcuno si proclami il nuovo coordinatore, per un masismo di 10 secondi prima di ritentare;
                    synchronized (ElectionLock) {
                        try {
                            ElectionLock.wait(5000);
                        } catch (InterruptedException e) {
                            //if (vars.getNewCordinator() != null) {
                                /*if (Starter)
                                    System.out.println("Client con Thread id: " + Thread.currentThread().getId() + " interrotto la wait ");
                                else
                                    System.out.println("Server con Thread id: " + Thread.currentThread().getId() + " interrotto la wait ");
                           // }*/
                        }
                        //System.out.println("ho temrinato i secondi di attesa");
                    }
                    if (vars.getNewCordinator() != null) {
                        /*if (Starter)
                            System.out.println("Client con Thread id: " + Thread.currentThread().getId() + "c'è un nuovo coordinatore!! " + vars.getNewCordinator().getId());
                        else
                            System.out.println("Server con Thread id: " + Thread.currentThread().getId() + " c'è un nuovo coordinatore!! " + vars.getNewCordinator().getId());
                        *///se il nuovo coordinatore si è eletto esco dal do while e il client riprenderà con collegadosi al nuovo nodo
                        break elector;
                    }
                }
                Attempts++;
                /*if (Starter)
                    System.out.println("Client con Thread id: " + Thread.currentThread().getId() + "    Tentativi di elezione: " + Attempts);
                else
                    System.out.println("Server con Thread id: " + Thread.currentThread().getId() + " Tentativi di elezione: " + Attempts);*/
            } while (Attempts<10);
            //se nessuno si proclama coordinatore (i nodi non rispondono/sono occupati) oppure non ci sono altri nodi con l'id più grande, allora si autoproclama coordinatore e lo notifica a tutti
            if (vars.getNewCordinator() == null) {
                ElectMySelf();
            }
        }
        else ElectMySelf();

    }

    public void ElectMySelf(){
        //System.out.println("La lista contiene: "+vars.getAllNodes().size());
        for (Node_Edge e : vars.getAllNodes()) {
            if(e.getPortIn()!=Port){
                //System.out.println("foreach: "+e.getPortIn());
                new VoteClient(id, Port, e, vars, "NewCoordinator").start();
            }
        }
        //l'if serve nel qual caso sia appena stato eletto un nuovo coordinatore non si rischia di eleggerne 2
        if(vars.getNewCordinator()==null){
            //vars.setNewCordinator(new Node);
            vars.setParentType("Cloud");
            vars.setParentPort(1337);
            vars.setParentId(1);
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:1337/node/newCoordinator/"+id);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            if(response.getStatus()==200){
                    System.out.println("\nIl nodo Corrente è stato eletto coordinatore");
                }
            }
            /*else {
            System.out.println("\nnon sono riuscito ad inserire il nuovo root\n");
        }*/
    }
}
