package beans;

import java.io.Serializable;

public class Point implements Serializable {
   private int x;
   private int y;

    public Point() {
    }

   public Point(int x, int y){
       this.x=x;
       this.y=y;
   }

   public Point(Point p){
       this.x=p.getX();
       this.y=p.getY();
   }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString(){
        return "{x: " + getX() +", y: " + getY() + "}\n";
    }
}
