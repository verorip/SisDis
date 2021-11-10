package beans;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SensorDaemon extends Thread{

    private SensorPort sendTo;
    private Object Start, WakeUp, UpdateReciver;
    private Point location;

    public SensorDaemon(SensorPort e, Object s, Object w, Object u, Point p){
        this.sendTo=e;
        this.Start=s;
        this.location=p;
        this.WakeUp=w;
        this.UpdateReciver=u;
    }

    @Override
    public void run(){
        JSONObject obj=new JSONObject(), temp=new JSONObject();
        try {
            temp.put("x", location.getX());
            temp.put("y", location.getY());
            obj.put("position", temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println(obj.toString());
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        Client client = Client.create(clientConfig);

        WebResource webResource = client.resource("http://localhost:1337/node/get/near");
        while(true){
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, obj.toString());
            //System.out.print("risposta "+response);
            Node_Edge x=response.getEntity(Node_Edge.class);
            //System.out.println("--->>>>>" +x.getPosition());
            if(sendTo.getPort()==0 || sendTo.getPort()!=x.getPortOut()){
                synchronized (UpdateReciver){
                    sendTo.setPort(x.getPortOut());
                    //System.out.println("Sensor Daemon- Nuovo nodo: "+x.getPortOut());
                }
            }
            synchronized (Start){
                Start.notify();
            }
            synchronized (WakeUp){
                try {
                    WakeUp.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void NearNodeRequest() {


    }
}
