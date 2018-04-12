
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mathe
 */
public class Client2 {
        static public void main(String args[]) throws Exception
    {    	
//        if (args.length < 1 ) {
//            throw new IllegalArgumentException("Parameter: <port>");
//        }
//    	
//        Client client=new Client( Integer.parseInt(args[0]));
        System.out.print("Enter port to connect to: ");
        Scanner in = new Scanner(System.in);
        
    	Client client=new Client(in.nextInt());
     }
}
