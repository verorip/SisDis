package beans;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class CloudDB {

    @XmlElement(name="node_list")
    private ArrayList<Statistics> DBLocals;
    private ArrayList<Statistics> DBGlobal;
    private static CloudDB instance=null;

    public CloudDB(){
        this.DBLocals = new ArrayList<Statistics>();
        this.DBGlobal = new ArrayList<Statistics>();
    }

    public synchronized static CloudDB getInstance(){
        if(instance==null)
            instance = new CloudDB();
        return instance;
    }


    public synchronized void ReciveData(Statistics sc) {
        if(sc.getType().equals("Local"))
            DBLocals.add(0, sc);
        else
            DBGlobal.add(0, sc);
    }

    public synchronized ArrayList<Statistics> GetNDataFrom(int id, int n){
        ArrayList<Statistics> Datas=new ArrayList<>();
        ArrayList<Statistics> temp=new ArrayList<>();
        temp.addAll(DBLocals);
        temp.addAll(DBGlobal);
        temp=TimeStampComparator(temp);
        for(int i=0; i<n; i++){
            if(temp.size()>i && temp.get(i).getId().equals(Integer.toString(id))){
                Datas.add(temp.get(i));
            }
        }

        return Datas;
    }

    public synchronized ArrayList<Statistics> GetNDatas(int n){
        ArrayList<Statistics> Datas=new ArrayList<>();
        ArrayList<Statistics> temp = new ArrayList<>();
        temp.addAll(DBLocals);
        temp.addAll(DBGlobal);
        temp=TimeStampComparator(temp);
        for(int i=0; i<n; i++){
            if(temp.size()>i){
                Datas.add(temp.get(i));
            }
        }
        return Datas;
    }

    public synchronized Statistics GetMeanAndStdDevNode(int id, int n){
        Statistics Data=new Statistics("Cloud");
        ArrayList<Statistics> Datas=new ArrayList<>();
        ArrayList<Statistics> temp=new ArrayList<>();
        temp.addAll(DBLocals);
        temp=TimeStampComparator(temp);
        for(int i=0; i<n; i++){
            if(temp.size()>i && temp.get(i).getId().equals(Integer.toString(id))){
                Datas.add(temp.get(i));
            }
        }
        Data=StaticsAggregation(Datas, Data);
        return Data;
    }

    public synchronized Statistics GetMeanAndStdDev(int n){
        Statistics Data=new Statistics("Cloud");
        ArrayList<Statistics> Datas=new ArrayList<>();
        ArrayList<Statistics> temp=new ArrayList<>();
        temp.addAll(DBLocals);
        temp.addAll(DBGlobal);
        temp=TimeStampComparator(temp);
        for(int i =0; i<n; i++){
            if(temp.size()>i){
                Datas.add(temp.get(i));
            }
        }
        Data=StaticsAggregation(Datas, Data);
        return Data;
    }

    private Statistics StaticsAggregation(ArrayList<Statistics> Datas, Statistics Data) {
        Data.setMean(getAggregateMean(Datas));
        Data.setStandardDeviation(getAggrStdDev(Datas, Data));
        return Data;
    }

    private double getAggrStdDev(ArrayList<Statistics> Datas , Statistics Data) {
        return Math.sqrt(getAggregateVariance(Datas, Data));
    }

    private double getAggregateVariance(ArrayList<Statistics> Datas , Statistics Data){
        double num = 0.00;
        double denum = 0.00;
        double total;
        for(Statistics md : Datas){
            num+=md.getPopulation_Number()*(md.getStandardDeviation()+((md.getMean()-Data.getMean())*(md.getMean()-Data.getMean())));
            denum+=md.getPopulation_Number();
        }
        total=num/denum;
        return total;
    }

    public synchronized ArrayList<Statistics> TimeStampComparator(ArrayList<Statistics> temp){

        Collections.sort(temp, new Comparator<Statistics>() {
            @Override
            public int compare(Statistics o1, Statistics o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
        return temp;
    }

    @SuppressWarnings("Duplicates")
    private double getAggregateMean(ArrayList<Statistics> Datas) {
        double denum = 0.00;
        double num = 0.00;
        double sum;
        for(Statistics md : Datas){
            num+=md.getMean()*md.getPopulation_Number();
            denum+=md.getPopulation_Number();
        }
        sum=num/denum;
        return sum;
    }
}
