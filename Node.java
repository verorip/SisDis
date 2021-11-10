import beans.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Node {

    private static Random rnd=new Random();
    private static ArrayList<Statistics> BufferSender = new ArrayList<Statistics>();
    private static ArrayList<Statistics> BufferReciver = new ArrayList<Statistics>();
    public static final Object SyncLock=new Object(), ArrayLock=new Object(), ElectionLock=new Object();
    public static SharedVars vars;
    public static int port_in;
    public static int port_out;
    public static Point position;
    public static String id;

    public static void main(String[] args) throws IOException{
        vars =new SharedVars();
        int x=-1,y=-1;
        //imposto l'id del nodo scegliendolo randoicamente, prendendo un qualsiasi numero a 4 cifre
        id=Integer.toString(rnd.nextInt(9999-1000+1)+1000);
        boolean check=false;
        Scanner keyboard = new Scanner(System.in);
        do{
            System.out.println("scrivere 'random' per una posizione casaule o inserire un numero per la posizione x (tra 0 e 100): ");
            String input = keyboard.nextLine();
            try {
                if (Integer.parseInt(input) < 0 || Integer.parseInt(input) > 100) {
                    System.out.println("deve essere tra 0 e 100\n ");
                } else {
                    x=Integer.parseInt(input);
                    System.out.println("inserire la coordinata y ( compresa tra 0 e 100): ");
                    input = keyboard.nextLine();
                    if (Integer.parseInt(input) < 0 || Integer.parseInt(input) > 100) {
                        System.out.println("deve essere tra 0 e 100\n ");
                    }
                    else{
                        y=Integer.parseInt(input);
                        check=true;
                    }
                }
            }
            catch (Exception e){
                if(input.equals("random")){
                    x=rnd.nextInt(101);
                    y=rnd.nextInt(101);
                    //position=new Point(x,y);
                    check=true;
                }
                else{
                    System.out.println("input non riconosciuto");
                }
            }
        }while(!check);
        check=false;
        do{
            System.out.println("inserire la porta di ascolto");
            String input = keyboard.nextLine();
            try {
                if (Integer.parseInt(input) < 1000 || Integer.parseInt(input) > 65535) {
                    System.out.println("deve essere tra 1000 e 65535\n ");
                } else {
                    port_in=Integer.parseInt(input);
                    port_out=port_in+1;
                   // System.out.println(port_out);
                    //System.out.println("porta---->"+port_in);
                    check=true;
                }
            }
            catch (Exception e){
                System.out.println("input non riconosciuto");
            }
        }while(!check);
        position=new Point(x,y);
        System.out.println("\n il nodo sarà in posizione:  "+position);

        ClientResponse response=CloudInsertRequest(position);
        JSONObject parent =response.getEntity(JSONObject.class);
        //System.out.println(parent.toString());
        try {
            vars.setParentPort(parent.getInt("port_in"));
            if(!parent.getString("id").equals(id))
                vars.setParentType(parent.getString("type"));
            else
                vars.setParentType("cloud");
            vars.setParentId(parent.getInt("id"));
           // System.out.println(vars.getParentPort());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(response.getStatus()==200){
            startSend();
            startRecive();
        }
        else if(response.getStatus()==400)
            System.out.println("error 400 bad request");
        else
            System.out.println("Internal error");

    }

    private static void startSend() {
        if (port_in == vars.getParentPort()){
            vars.setParentPort(1337);
            //System.out.println("setto porta a 1337");
        }
        NodeClient sender = new NodeClient(id, port_in, vars, BufferSender, BufferReciver, SyncLock, ArrayLock, ElectionLock);
        vars.setClientSend(sender);
        sender.start();
    }

    private static void startRecive() {
        NodeServer reciver = new NodeServer(id, port_in, vars, BufferSender,BufferReciver, SyncLock, ArrayLock, ElectionLock);
        vars.setServerListen(reciver);
        reciver.start();
        ServerSensor sensor_reciver=new ServerSensor(id, port_out, vars, BufferSender,BufferReciver, SyncLock, ArrayLock, ElectionLock);
        vars.setServerSensor(sensor_reciver);
        sensor_reciver.start();
    }

    public static ClientResponse CloudInsertRequest(Point position) throws MalformedURLException {
        String ip="localhost";
        JSONObject obj=new JSONObject();
        JSONObject pos=new JSONObject();
        try {
            /*if(port_in==1111){
                id="1";
            }
            else if(port_in==2222)
                id="4";
            else if(port_in==3333)
                id="3";
            else id="2";*/
            obj.put("id", id);
            obj.put("ipadress", ip);
            obj.put("portIn", port_in);
            obj.put("portOut", (port_in+1));
            pos.put("x", position.getX());
            pos.put("y", position.getY());
            obj.put("position", pos);
            //System.out.println(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        Client client = Client.create(clientConfig);

        WebResource webResource = client.resource("http://localhost:1337/node/add");
        ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, obj.toString());
        //System.out.println("risposta:  "+response);
        return response;

    }
}
