package beans;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;

public class VoteClient extends Thread{

    private String id;
    private Node_Edge Contact;
    private SharedVars vars;
    private Message m;
    private int Port;
    private Gson g;

    //usato per i messaggi "election"
    public VoteClient(String id, Node_Edge Contact, SharedVars vars, String Header){
        this.id=id;
        this.Contact=Contact;
        this.vars=vars;
        this.m= new Message(Header, vars.getAllNodes(), 0);
        this.g=new Gson();
    }

    //usato per i messaggi "NewCoordinator"
    public VoteClient(String id, int Port, Node_Edge Contact, SharedVars vars, String Header){
        //this.id=id;
        this.Contact=Contact;
        this.vars=vars;
        //this.Port=Port;
        this.m= new Message(Header, "localhost", Port, Integer.parseInt(id), 0);
        this.g=new Gson();
    }

    @Override
    public void run(){
        Socket Sock=null;
        int tries=0;
        ObjectOutputStream outStream;
        ObjectInputStream InStream;
        Message res;
        /*
            questo if serve per impedire che si stia spedendo un messaggio mentre è
            stato nominato un coordinantore (elezione avviata da un altro nodo che si
            era accorto che il coordinatore non rispondeva più)
            così non si rischia di indire una nuova elezione
        */

        if(vars.getNewCordinator()==null){
            do{
                try {
                    if(m.getHeader().equals("NewCoordinator")){
                        //nel caso che un nodo risponde all'elezione dicendo che può concorrere
                        //System.out.println("Sto spedendo new coordinator a: "+Contact.getPortIn());
                    }
                    //System.out.println("Spedisco a: "+Contact.getId());
                    Sock=new Socket(Contact.getIpadress(), Contact.getPortIn());
                    //onde evitare che non si riceva risposta e il thread non temrini mai è stato impostato un timeout per la read
                    Sock.setSoTimeout(10000);
                    //System.out.println("Messaggio"+m.getHeader()+" mi sono connesso\n");
                    outStream = new ObjectOutputStream(Sock.getOutputStream());
                    //m.setTimestamp(Calendar.getInstance().get(Calendar.MILLISECOND));
                    //System.out.println("ho impostato il timestamp e mi accingo a spedire il pacchetto");
                    String JSON= g.toJson(m, Message.class);
                    outStream.writeObject(JSON);
                    //System.out.println("spedito");
                    InStream = new ObjectInputStream(Sock.getInputStream());
                    Object input;
                    input=InStream.readObject();
                    /*try{

                    }catch(SocketTimeoutException el){
                        el.printStackTrace();
                        //se scatta il timeout il tread esce e si chiude
                        break;
                    }*/

                    res= g.fromJson(((String)input), Message.class );
                    //System.out.println("--------->>>>>>L'header del messaggio ricevuto da "+Sock.getPort()+ ":"+res.getHeader());
                    if(m.getHeader().equals("Election")){
                        //nel caso che un nodo risponde all'elezione dicendo che può concorrere
                        if(res.getHeader().equals("Alive")){
                            vars.setRespond(true);
                            break;
                        }
                    }
                    //se si spedisce un messaggio dicendo il nuovo coordinatore semplicemente il nodo risponderà con un ACK
                    else {
                        break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    //System.out.println("Spendendo a: " + Contact.getId()+"sulla porta "+ Contact.getPortIn()+" non sono riuscito a colelgamri o c'è staot un errore");
                    tries++;
                }
            }while(tries<3);
            try {
                Sock.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}
