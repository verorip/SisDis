package beans;

import java.io.Serializable;
import java.util.ArrayList;

public class MeasureData implements Serializable {

    private String NodeId;
    private int samples_num;
    private double Standard_Deviation;
    private double Media;
    private ArrayList<Measurement> Data;

    private long Timestamp;

    public MeasureData(String nodeId){
        this.setNodeId(nodeId);
        Data=new ArrayList<Measurement>();
    }

    public MeasureData(String NodeId, double standard_Deviation, double media, ArrayList<Measurement> data, long timestamp) {

        this.setNodeId(NodeId);
        this.Standard_Deviation = standard_Deviation;
        this.Media = media;
        this.Data = data;
        this.Timestamp = timestamp;
    }

    public synchronized double getStandard_Deviation() {
        return Standard_Deviation;
    }

    public synchronized void setStandard_Deviation(double standard_Deviation) {
        Standard_Deviation = standard_Deviation;
    }

    public synchronized double getMedia() {
        return Media;
    }

    public synchronized void setMedia(double media) {
        Media = media;
    }

    public synchronized  ArrayList<Measurement> getData() {
        return Data;
    }

    public synchronized void addData(ArrayList<Measurement> e){   Data.addAll(e);}

    public synchronized void setData( ArrayList<Measurement> data) {
        Data = data;
    }

    public synchronized void addOneData(Measurement m){
            Data.add(m);
    }

    public synchronized long getTimestamp() {
        return Timestamp;
    }

    public synchronized void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }

    public synchronized int getSamples_num() {
        return samples_num;
    }

    public synchronized void setSamples_num(int samples_num) {
        this.samples_num = samples_num;
    }

    public synchronized String getNodeId() {
        return NodeId;
    }

    public void setNodeId(String nodeId) {
        NodeId = nodeId;
    }
}
