package services;

import beans.CloudDB;
import beans.Edge_Positions;
import beans.Statistics;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("Cloud")
public class CloudStorage {

    @Path("send_to_cloud")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response ReciveDatas(Statistics s){
        //System.out.println("\nrEcive data: ");
        CloudDB.getInstance().ReciveData(s);
        return Response.ok().build();
    }

    @Path("get_n_data_from/{id}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetNDataFrom(@PathParam("id")int id, int n){
        //System.out.println("\nGetNDataFrom: "+id+" "+n);
        ArrayList<Statistics> resp=CloudDB.getInstance().GetNDataFrom(id, n);
        if(resp!=null && resp.size()>0){
            return Response.ok(resp).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("get_n_datas")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetNDatas(int n){
        //System.out.println("\nGetNDatas: "+n);
        ArrayList<Statistics> resp=CloudDB.getInstance().GetNDatas(n);
        if(resp!=null && resp.size()>0){
            return Response.ok(resp).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("get_stat_node/{id}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetMandSNode(@PathParam("id") int id, int n){
        //System.out.println("\nGetNDataFrom: "+id+" "+n);
        Statistics s=CloudDB.getInstance().GetMeanAndStdDevNode(id,n);
        //System.out.println("m: "+s.getMean()+" s: "+s.getStandardDeviation());
        if(s.getMean()>0.0 && s.getStandardDeviation()>0.0)
        {
            return Response.ok(s).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }

    @Path("get_stat")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetMeanAndStdDev(int n){
        //System.out.println("GetMeanAndStdDev: "+n);
        Statistics s=CloudDB.getInstance().GetMeanAndStdDev(n);
        //System.out.println("m: "+s.getMean()+" s: "+s.getStandardDeviation());
        if(s.getMean()>0.0 && s.getStandardDeviation()>0.0)
        {
            return Response.ok(s).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAllNodes(){
        return Response.ok(Edge_Positions.getInstance()).build();
    }


}
