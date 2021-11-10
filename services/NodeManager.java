package services;


import beans.Edge_Positions;
import beans.Node_Edge;
import beans.Point;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;


@Path("node")
public class NodeManager {

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAllNodes(){
        return Response.ok(Edge_Positions.getInstance()).build();
    }

    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add_Node(Node_Edge e){

        Node_Edge result =Edge_Positions.getInstance().add_Node(e);
        JSONObject respo=new JSONObject();
        try {
            respo.put("id", result.getId());
            respo.put("ip_adress", result.getIpadress());
            respo.put("port_in", result.getPortIn());
            respo.put("port_out", result.getPortOut());
            respo.put("type", result.getType());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if(e!=null){
            //System.out.println("nodo padre: "+respo.toString());
            return Response.ok(respo.toString(), MediaType.APPLICATION_JSON).build();
        }
        else{
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @Path("remove/{id}")
    @GET
    @Consumes({"application/json", "application/xml"})
    public Response remove_Node(@PathParam("id") int id){
        System.out.println("remove: "+id);
        boolean success=Edge_Positions.getInstance().remove_Node(id);
        if(success)
            return Response.ok().build();
        else return Response.status(Response.Status.NOT_FOUND).build();
    }


    @Path("get/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getParent(@PathParam("id") int id){
        //System.out.println("getparent: "+id);
        Node_Edge e=Edge_Positions.getInstance().getParent(id);
        Node_Edge resp=new Node_Edge();
        if(e!=null) {
            resp.setId(e.getId());
            resp.setPadre(e.getPadre());
            resp.setType(e.getType());
            resp.setPortIn(e.getPortIn());
            resp.setIpadress(e.getIpadress());
            resp.setPosition(e.getPosition());
            resp.setPortOut(e.getPortOut());
            resp.setSons(new ArrayList<Node_Edge>());
            //System.out.println("padre trovato");
            return Response.ok(resp).build();
        }
        else{
            //System.out.println("padre non trovato");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("find/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodeById(@PathParam("id") int id){
        //System.out.println("getbyid:");
        Node_Edge e=Edge_Positions.getInstance().getNodeById(id);
        if(e!=null) {
            //System.out.println("trovato");
            return Response.ok(e).build();
        }
        else{
            //System.out.println("nope");

            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("get/near")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response getNearest(Node_Edge position) {
        //System.out.println("nearest:");
        //System.out.println(position.getPosition());
        Node_Edge e = Edge_Positions.getInstance().getNearestNode(position.getPosition());
        if (e != null) {
            return Response.ok(e).build();
        } else return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("newCoordinator/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response new_Root(@PathParam("id") int id){
        //System.out.println("new Root:");
        boolean result =Edge_Positions.getInstance().New_Root(id);
        if(result)
            return Response.ok().build();
        else return Response.status(Response.Status.NOT_FOUND).build();
    }
}
