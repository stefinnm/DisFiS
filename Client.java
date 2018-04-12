import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.nio.file.*;


public class Client
{
    DFS dfs;
    public Client(int p) throws Exception {
        dfs = new DFS(p);
        Scanner in = new Scanner(System.in);
        
        while(true){
            System.out.println("1.  Join");
            System.out.println("2.  ls");
            System.out.println("3.  Rename(mv)");
            System.out.println("4.  Add File");
            System.out.println("5.  Delete File");
            System.out.println("6.  Read");
            System.out.println("7.  Tail");
            System.out.println("8.  Head");
            System.out.println("9.  Append");
            System.out.println("10. Quit");
            System.out.print("$ ");
            
            int choice = in.nextInt();
            in.nextLine();
            if(choice == 1){
                System.out.print("Enter port: ");
                int port = in.nextInt();
                in.nextLine();
                dfs.join(InetAddress.getLocalHost().toString(), port);
            }
            else if(choice == 2){
                System.out.println(dfs.ls());
            }
            else if(choice == 3){
                System.out.print("Enter file to rename: ");
                String oldName = in.nextLine();
                System.out.print("Enter new name for file " + oldName + ": ");
                String newName = in.nextLine();
                dfs.mv(oldName, newName);
            }
            else if(choice == 4){
                System.out.print("Enter file to add: ");
                String newFile = in.nextLine();
                dfs.touch(newFile); 
            }
            else if(choice == 5){
                System.out.print("Enter file to delete: ");
                String deleteFile = in.nextLine();
                dfs.delete(deleteFile);
            }
            else if(choice == 6){
                System.out.println("Please enter file name");
                String fileName = in.nextLine();
                dfs.touch(fileName);

            }
            else if(choice == 7){
                System.out.println("Please enter file name");
                String fileName = in.nextLine();
                byte[] tail = dfs.tail(fileName);
                System.out.println(new String(tail).replace("/n","\n"));
            }
            else if(choice == 8){
                System.out.println("Please enter file name");
                String fileName = in.nextLine();
                byte[] head = dfs.head(fileName);
                System.out.println(new String(head).replace("/n","\n"));
            }
            else if(choice == 9){
                System.out.println("Enter the name of file to append to:");
                String fileName = in.nextLine();
                byte[] b = Files.readAllBytes(Paths.get("test.txt"));
                dfs.append(fileName, b);
            }
            else if(choice == 10){
                break;

            }
        }
        in.close();
        System.exit(0);
    }

    static public void main(String args[]) throws Exception {
        System.out.print("Enter port to connect to: ");
        Scanner in = new Scanner(System.in);
        
    	Client client=new Client(in.nextInt());

     } 
}
