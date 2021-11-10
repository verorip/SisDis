package beans;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Statistics implements Serializable {

    @JsonProperty("id")
    private String id;
    @JsonProperty("Mean")
    private Double Mean;
    @JsonProperty("StandardDeviation")
    private Double StandardDeviation;
    @JsonProperty("Population_Number")
    private int Population_Number;
    @JsonProperty("Type")
    private String Type;
    //private ArrayList<Statistics> s;
    @JsonProperty("Timestamp")
    private long Timestamp;

    public Statistics(String id, Double Mean, Double StandardDeviation, int Population, String Type, long Timestamp){
        this.id=id;
        this.Mean=Mean;
        this.StandardDeviation=StandardDeviation;
        this.Population_Number=Population;
        this.Type=Type;
        //this.s=s;
        this.Timestamp=Timestamp;
    }

    public Statistics(){}



    public Statistics(String id){
        this.id=id;
        this.Mean=0.0;
        this.Population_Number=0;
        this.StandardDeviation=0.0;
        this.Type="empty";
        this.Timestamp=0;
        //this.s=new ArrayList<Statistics>();
    }


    public Double getMean() {
        return Mean;
    }

    public void setMean(Double mean) {
        Mean = mean;
    }

    public Double getStandardDeviation() {
        return StandardDeviation;
    }

    public  void setStandardDeviation(Double standardDeviation) {
        StandardDeviation = standardDeviation;
    }

    public int getPopulation_Number() {
        return Population_Number;
    }

    public void setPopulation_Number(int population_Number) {
        Population_Number = population_Number;
    }

   /* public ArrayList<Statistics> getS() {
        return s;
    }

    public void setS(ArrayList<Statistics> s) {
        this.s = s;
    }

    public void AddOneS(Statistics s) {
        this.s.add(s);
    }*/

    public String getId() {
        return id;
    }


    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
