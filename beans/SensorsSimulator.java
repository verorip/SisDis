package beans;

import beans.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class SensorsSimulator extends Thread implements SensorStream{



    private Random rnd =new Random();
    public SensorPort SendTo;
    private SensorDaemon Daemon;
    private Socket sensorSocket=null;
    private ObjectOutputStream os;
    private int i, k;
    private final int WINDOW_SIZE=8;
    private ArrayList<Measurement> temp, send;
    private Gson g;
    private final Object Start=new Object();
    private final Object WakeUp=new Object();
    private final Object UpdateReceiver=new Object();

    public SensorsSimulator(){
        i=0;
        k=4;
        temp=new ArrayList<Measurement>();
        send=new ArrayList<Measurement>();
        g=new Gson();
        SendTo=new SensorPort();
    }

    @Override
    public void run(){
        //System.out.println("Sono Thread con SenTo: "+SendTo.getPort());
        Point location = new Point();
        location.setX(rnd.nextInt(101));
        location.setY(rnd.nextInt(101));
        //System.out.println("simulatore in posizione:  " + location );
        SensorDaemon Daemon=new SensorDaemon(SendTo, Start, WakeUp, UpdateReceiver, location);
        Daemon.start();
        synchronized (Start){
            try {
                Start.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        PM10Simulator simulator = new PM10Simulator(this);
        simulator.start();
    }

    @Override
    public void sendMeasurement(Measurement m) {
        /*System.out.println("\nmisura:   "+m);
        System.out.println(SendTo.getPosition());
        System.out.println("size: "+temp.size());*/
        if(i==0 && temp.size()>7){
            for(int j=0; j<4; j++){
                temp.set(j, temp.get(k));
                k++;
            }
            k=4;
            i=4;
        }
        if(temp.size()!=8){
            temp.add(m);
        }
        else{
            temp.set(i, m);
        }

        i++;
        //System.out.println("i:  "+i);
        if(i==WINDOW_SIZE){
            //System.out.println("temp: " + temp);
            send= (ArrayList<Measurement>) temp.clone();
            System.out.println("Thread: "+Thread.currentThread().getId()+" Send: "+send);
            i=0;
            TypeToken<ArrayList<Measurement>> token = new TypeToken<ArrayList<Measurement>>(){};
            String JSON=g.toJson(send, token.getType());
            try {
                if (sensorSocket == null) {
                    //System.out.println("porta: "+ SendTo);
                    sensorSocket = new Socket("localhost", SendTo.getPort());
                    System.out.println("creo connessione e spedisco a " + SendTo);
                    os = new ObjectOutputStream(sensorSocket.getOutputStream());
                    os.writeObject(JSON);
                }
                else {
                    os.writeObject(JSON);
                    }
                    os.flush();
                    send.clear();
                    synchronized (UpdateReceiver){
                        //System.out.println("porte: "+SendTo.getPort()+" "+sensorSocket.getPort());
                        if(SendTo.getPort()!=sensorSocket.getPort()){
                            sensorSocket.close();
                            sensorSocket=null;
                        }
                    }
            }catch(IOException e){
                //e.printStackTrace();
                if(!sensorSocket.isClosed()){
                    try {
                        sensorSocket.close();

                    } catch (IOException e1) {
                        //e1.printStackTrace();
                    }
                    sensorSocket=null;
                    synchronized (WakeUp){
                        WakeUp.notify();
                    }
                }
            }
        }
    }
}
