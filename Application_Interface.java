import beans.Node_Edge;
import beans.Statistics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.glassfish.external.statistics.Statistic;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Application_Interface {

    private static Scanner keyboard;

    public static void main(String[] args) throws IOException, JSONException {
        boolean flag=false;
        Client client;
        WebResource webResource;
        ClientResponse response;
        keyboard = new Scanner(System.in);
        do {
            System.out.println("\n\nScegliere che dati si vogliono visualizzare:\n ");
            System.out.println("Digitare 1 Per Ottenere la Lista Dei Nodi e delle loro posizioni nella griglia \n" +
                    "Digitare 2 Per ottenere La lista delle ultime n misurazioni di uno specifico nodo\n" +
                    "Digitare 3 Per Ottenere La lista delle ultime n statistiche locali e globali\n" +
                    "Digitare 4 Per ottenere La media e la varianza delle ultime n statistiche di uno specifico nodo\n" +
                    "Digitare 5 per ottenere la media e la deviazione standard delle ultime n statistiche locali e globali\n" +
                    "Digitare exit per uscire");
            String input = keyboard.nextLine();
            if(input.equals(Integer.toString(1))){
                client = Client.create();
                webResource = client.resource("http://localhost:1337/Cloud");
                response = webResource.accept("application/json").get(ClientResponse.class);
                if(response.getStatus()!=200){
                    System.out.println("qualcosa nell richiesta è andata storta"+ response);
                    System.out.println(response.getEntity(String.class));
                }
                else if(response.getStatus()==200){
                    JSONObject JSONResponse =response.getEntity(JSONObject.class);
                    JSONArray NodeList= JSONResponse.getJSONArray("node_list");
                    for(int j=0; j<NodeList.length(); j++){
                        JSONObject obj = (JSONObject) NodeList.get(j);
                        Node_Edge temp= new Gson().fromJson(obj.toString(), Node_Edge.class);
                        System.out.println("Nodo con ID: "+temp.getId()+" In Posizione: "+temp.getPosition());
                    }
                }
            }
            else if(input.equals(Integer.toString(2))){
                String id=getID();
                String n=getN();
                client = Client.create();
                webResource = client.resource("http://localhost:1337/Cloud/get_n_data_from/"+id);
                response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, n);
                if(response.getStatus()==404){
                    System.out.println("Non è stato trovaot nessun nodo con quell'id o non ci sono misurazioni");
                }
                else if(response.getStatus()==200){
                    ArrayList<Statistics> Datas=new ArrayList<>();
                    Gson g=new Gson();
                    TypeToken<ArrayList<Statistics>> token = new TypeToken<ArrayList<Statistics>>(){};
                    Datas=g.fromJson(response.getEntity(String.class), token.getType());
                    System.out.println("Statistiche del nodo "+id+": ");
                    for(Statistics s:Datas){
                        String time=String.format("%02d:%02d:%02d\n\n", TimeUnit.MILLISECONDS.toHours(s.getTimestamp()),(TimeUnit.MILLISECONDS.toMinutes(s.getTimestamp())-
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(s.getTimestamp()))), (TimeUnit.MILLISECONDS.toSeconds(s.getTimestamp()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(s.getTimestamp()))));
                        System.out.printf("Media: " + s.getMean() +", Deviazione Standard:" + s.getStandardDeviation()+" delle "+time);
                    }
                }
                else{
                    System.out.println(" Errore: "+response.getStatus()+" -->"+response.getEntity(String.class));
                }
            }
            else if(input.equals(Integer.toString(3))){

                String n=getN();
                client = Client.create();
                webResource = client.resource("http://localhost:1337/Cloud/get_n_datas");
                response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, n);
                if(response.getStatus()==404){
                    System.out.println("Non sono state trovate misurazioni");
                }
                else if(response.getStatus()==200){
                    ArrayList<Statistics> Datas=new ArrayList<>();
                    Gson g= new Gson();
                    TypeToken<ArrayList<Statistics>> token = new TypeToken<ArrayList<Statistics>>(){};
                    Datas=g.fromJson(response.getEntity(String.class), token.getType());
                    for(Statistics s : Datas){
                        String time=String.format("%02d:%02d:%02d\n\n", TimeUnit.MILLISECONDS.toHours(s.getTimestamp()),(TimeUnit.MILLISECONDS.toMinutes(s.getTimestamp())-
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(s.getTimestamp()))), (TimeUnit.MILLISECONDS.toSeconds(s.getTimestamp()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(s.getTimestamp()))));
                        System.out.printf("Media: " + s.getMean() +", Deviazione Standard:" + s.getStandardDeviation()+" delle "+time);
                    }
                }
            }
            else if(input.equals(Integer.toString(4))){
                String id=getID();
                String n=getN();
                client = Client.create();
                webResource = client.resource("http://localhost:1337/Cloud/get_stat_node/"+id);
                response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, n);
                if(response.getStatus()==404){
                    System.out.println("Non è stato trovaot nessun nodo con quell'id o non ci sono misurazioni");
                }
                else if(response.getStatus()==200){
                    Statistics s;
                    Gson g= new Gson();
                    s=g.fromJson(response.getEntity(String.class), Statistics.class);
                    System.out.println("Media delle ultime "+n+" misurazioni: "+s.getMean()+ "\nDeviazione Standard: delle ultime "+n+" misurazioni: " +s.getStandardDeviation());
                }
            }
            else if(input.equals(Integer.toString(5))){
                String n=getN();
                client = Client.create();
                webResource = client.resource("http://localhost:1337/Cloud/get_stat");
                response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, n);
                if(response.getStatus()==404){
                    System.out.println("Non sono state trovate misurazioni");
                }
                else if(response.getStatus()==200){
                    Statistics s;
                    Gson g= new Gson();
                    s=g.fromJson(response.getEntity(String.class), Statistics.class);
                    System.out.println("Media delle ultime "+n+" misurazioni Locali e Globali: "+s.getMean()+ "\nDeviazione Standard: delle ultime "+n+" misurazioni Locali e Globali: " +s.getStandardDeviation());
                }
            }
            else if(input.equals("exit")){
                System.out.println("Terminazione...");
                flag=true;
            }
            else{
                System.out.println("Input non riconosciuto\n");
            }
        }while(!flag);
    }

    public static String getID(){
        System.out.println("Digitare l'ID del nodo: ");
        String id=keyboard.nextLine();
        while(!id.matches("[0-9]+")){
            System.out.println("Inserire un numero: ");
            id=keyboard.nextLine();
        }
        return id;
    }

    public static String getN(){
        System.out.println("Digitare il numero di di misurazioni, se saranno di meno verranno restituite solo quelle rpesenti: ");
        String n=keyboard.nextLine();
        while(!n.matches("[0-9]+")){
            System.out.println("Inserire un numero: ");
            n=keyboard.nextLine();
        }
        return n;
    }
}
