import beans.SensorsSimulator;

import java.io.IOException;
import java.util.Scanner;

public class StartSensors {

    private static Scanner keyboard;

    public static void main(String[] args) throws IOException, InterruptedException {
        String input;
        keyboard=new Scanner(System.in);
        do{
            System.out.println("Inserire quanti thread avviare: ");
            input=keyboard.nextLine();
        }while(!input.matches("[0-9]+"));

        for(int i=0; i<Integer.parseInt(input); i++){
            new SensorsSimulator().start();
            Thread.sleep(3000);
        }

    }
}
