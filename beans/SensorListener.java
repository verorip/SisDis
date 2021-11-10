package beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class SensorListener extends Thread{

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

    public SensorListener(String id, int port, SharedVars vars, Socket connectionsocket, ArrayList<Statistics> bufferSender, ArrayList<Statistics> bufferReciver, Object syncLock, Object ElectionLock, Object ArrayLock, Thread thread) {
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
        g=new Gson();
    }

    @Override
    public void run(){
        System.out.println(("SensorServer: Sensore in comunicazione\n"));
        int i=0, total;
        try {
            inStream = new ObjectInputStream(connectionsocket.getInputStream());
            do {
                if(Thread.currentThread().isInterrupted()){
                    //System.out.println("\nsono nell'interrupt\n");
                    if (NewDate!=null && NewDate.getData().size()>0){
                            CalculateStatics();
                            s.setMean(NewDate.getMedia());
                            s.setStandardDeviation(NewDate.getStandard_Deviation());
                            s.setTimestamp(deltaTime());
                    }
                    //System.out.println("Media: "+NewDate.getMedia()+"  devstand: "+NewDate.getStandard_Deviation());

                    synchronized (ArrayLock) {
                        bufferSender.add(s);
                    }
                    synchronized (syncLock){
                        syncLock.notify();
                    }
                    //System.out.println("interrompo il thread");
                    connectionsocket.close();
                    break;
                }
                Object input;
                input= inStream.readObject();
                //System.out.println("tipo: "+input.getClass().getName());
                TypeToken<ArrayList<Measurement>> token = new TypeToken<ArrayList<Measurement>>(){};
                ArrayList<Measurement> temp=g.fromJson(((String)input), token.getType());
                //System.out.println(temp);
                if(NewDate.getData().isEmpty()){
                    NewDate.addData(temp);
                    i+=8;
                }
                else
                {
                    List<Measurement> temp1=NewDate.getData().subList(NewDate.getData().size()-4, NewDate.getData().size());
                    List<Measurement> temp2=temp.subList(0, temp.size()-4);
                    if(CheckList(temp1, temp2)){
                        for(int j=4; j<8; j++){
                            NewDate.addOneData(temp.get(j));
                            i++;
                        }
                    }
                    else{
                        //System.out.println("sono nell'else\n\n");
                        NewDate.addData(temp);
                        i+=8;
                    }
                }
                //System.out.print(i+": ricevuto da sensore");
                //System.out.println("---->>>>" + temp+"\n");
                if (i >= 40) {
                    i=0;
                    total=NewDate.getData().size();
                   // System.out.println("stmapo totale: "+total);
                    NewDate.setSamples_num(NewDate.getData().size());
                    CalculateStatics();
                    NewDate.setTimestamp(deltaTime());
                    System.out.println("Statistica Locale-->Nodo: "+id+" Media: "+NewDate.getMedia()+"  devstand: "+NewDate.getStandard_Deviation()+"alle "+
                            TimeUnit.MILLISECONDS.toHours(NewDate.getTimestamp())+":"+(TimeUnit.MILLISECONDS.toMinutes(NewDate.getTimestamp())-
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(NewDate.getTimestamp())))+":"+(TimeUnit.MILLISECONDS.toSeconds(NewDate.getTimestamp()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(NewDate.getTimestamp())))+"\n");
                    s.setMean(NewDate.getMedia());
                    s.setPopulation_Number(NewDate.getSamples_num());
                    s.setType("Local");
                    s.setStandardDeviation(NewDate.getStandard_Deviation());
                    s.setTimestamp(deltaTime());
                    synchronized (ArrayLock) {
                        bufferSender.add(s);
                    }
                    synchronized (syncLock){
                        syncLock.notify();
                    }
                    s=null;
                    s=new Statistics(id);
                    NewDate=null;
                    NewDate=new MeasureData(id);
                }

            }while (!Thread.currentThread().isInterrupted());
            //con il break che viene eseguito all'inizio del ciclo se il thread è staot interrotto chiudo la connessione
            //connectionsocket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            //System.out.println("EOF\n");
            try {
                //se si perde la connesione spedisco gli ultimi dati ricevuti (tipicamente perchè il nodo figlio ha chiuso la connessione
                connectionsocket.close();
                //thread.interrupt();
                //Thread.currentThread().interrupt();//preserve the message
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Server Exiting...");
    }


    private void CalculateStatics() {
        NewDate.setMedia(getMean());
        NewDate.setStandard_Deviation(getStdDev());
    }

    private double getMean() {
        double sum = 0.00;
        for(Measurement a : NewDate.getData())
            sum += a.getValue();
        return sum/NewDate.getData().size();
    }

    private double getVariance() {
        double mean = getMean();
        double temp = 0;
        for(Measurement a : NewDate.getData())
            temp += (a.getValue()-mean)*(a.getValue()-mean);
        return temp/(NewDate.getData().size()-1);
    }

    private double getStdDev() {
        return Math.sqrt(getVariance());
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

    private boolean CheckList(List<Measurement> list1, List<Measurement> list2){
        for(int k=0; k<list1.size(); k++){
            if(list1.get(k).getValue()!=list2.get(k).getValue() || list1.get(k).getTimestamp()!=list2.get(k).getTimestamp()){
                return false;
            }
        }
        return true;
    }
}
