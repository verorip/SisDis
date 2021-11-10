package beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;

public class NodeServer extends Thread {

    public SharedVars vars;
    private String id;
    public int port;
    public ArrayList<Statistics> bufferSender;
    public ArrayList<Statistics> bufferReciver;
    private ArrayList<ServerSocket> x;
    private ObjectInputStream inStream = null;
    public Object syncLock, ArrayLock, ElectionLock;
    private boolean listen;

    public NodeServer(String id, int port_in, SharedVars vars, ArrayList<Statistics> bufferSender, ArrayList<Statistics> bufferReciver, Object syncLock, Object ArrayLock, Object ElectionLock) {
        this.id=id;
        this.port=port_in;
        this.bufferSender=bufferSender;
        this.bufferReciver=bufferReciver;
        this.syncLock=syncLock;
        this.ArrayLock=ArrayLock;
        this.vars = vars;
        this.ElectionLock=ElectionLock;
        this.x=new ArrayList<ServerSocket>();
    }

    @Override
    public void run(){
        ServerSocket serverSocket = null;
        Socket connectionsocket=null;
        while(!Thread.currentThread().isInterrupted()) {
            try {
                serverSocket = new ServerSocket(port);
                while(!Thread.currentThread().isInterrupted()){
                    System.out.println("NodeServer : avviato in ascolto sulla porta " + port + "...\n");
                    connectionsocket = serverSocket.accept();
                    if (Thread.currentThread().isInterrupted()){
                        //macchinoso ma serve per poter gestire il fatto che la accept è una chiamata bloccante. La interrupt non interrompe l'accept
                        //il client farà almeno un tentativo per riconnettersi al server che sis ta chiudendo, in questo modo sblocca l'accept e la connessione viene chiusa prima che il
                        //client possa spedire
                        connectionsocket.close();
                        System.out.println("Node Server exiting...");
                        break;
                    }
                    x.add(serverSocket);
                    ServerElaboration s=new ServerElaboration(id, port, vars, connectionsocket, bufferSender, bufferReciver, syncLock, ArrayLock, ElectionLock, Thread.currentThread());
                    vars.AddServer(s);
                    s.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(!serverSocket.isClosed())
                    serverSocket.close();
                if (Thread.currentThread().isInterrupted()){
                    //System.out.println("sono nodeserver, sono nel secondo breack");
                    break;
                }
            } catch (IOException e1) {
                if (Thread.currentThread().isInterrupted()){
                    //System.out.println("sono nodeserver, sono nel secondo breack ma nel catch");
                    break;
                }
            }
        }
        System.out.println("sono nodeserverchiudo ufficialmente");
    }


}
