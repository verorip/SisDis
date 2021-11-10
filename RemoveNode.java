import beans.Message;
import beans.Node_Edge;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class RemoveNode {

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        int elimina;
        System.out.println("inserire l'id del nodo da eliminare: ");
        String input = keyboard.nextLine();
        try {
            if (Integer.parseInt(input) < 999 && Integer.parseInt(input) > 9999) {
                System.out.println("deve essere tra 1000 e 9999\n ");
            } else {
                elimina = Integer.parseInt(input);
                Client client = Client.create();
                WebResource webResource = client.resource("http://localhost:1337/node/find/"+elimina);
                ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                if(response.getStatus()==200){
                    Node_Edge e=response.getEntity(Node_Edge.class);
                    Socket ToSocket = new Socket("localHost", e.getPortIn());
                    ObjectOutputStream outStream = new ObjectOutputStream(ToSocket.getOutputStream());
                    Message m= new Message("Remove");
                    Gson g=new Gson();
                    String JSON=g.toJson(m, Message.class);
                    outStream.writeObject(JSON);
                    Client client1 = Client.create();
                    WebResource webResource1 = client1.resource("http://localhost:1337/node/remove/"+elimina);
                    ClientResponse response1 = webResource1.accept("application/json").get(ClientResponse.class);
                    if(response1.getStatus()==200) {
                        System.out.println("rimosso");
                    }
                    else{
                        System.out.println("2: non trovato o qualcosa è andato storto");
                    }

                }
                else{
                    System.out.println("1: non trovato o qualcosa è andato storto");
                }
            }
        } catch (Exception e) {
            System.out.println("comando non riconosciuto o qualcosa è andato storto");
            e.printStackTrace();
            System.out.println(e);

        }
    }
}
