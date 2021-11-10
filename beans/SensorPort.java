package beans;

public class SensorPort {

    private volatile int Port;

    public SensorPort(){
        this.Port=0;
    }

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }
}
