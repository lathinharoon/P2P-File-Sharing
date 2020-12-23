
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.file.FileSystemNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class FileShareMain {

    static Scanner scanner;//a scanner to get users command

    public static void main(String[] args) {
        configurations.ports = new ArrayList<>();
        scanner = new Scanner(System.in);
        System.out.println("P2P file sharing!");
        System.out.println("Available Commands:\n" +
                "+ for sharing a directory: share <pathForDirectory> at <port> eg:- share user/dir1/dir2/Sharing/ at 1234\n" +
                "+ for connecting to another Machine: connect to <ip> at <port> eg:- connect to localhost at 1234\n" +
                "+ for seeing the list of servers available to connect to: find servers\n" +
                "+ for exit: exit");// set of commands for the user to use
        String command = null;
        boolean end = false;
        while (!end) {//a while loop which runs until the user use the command exit to exit the loop and stop the program
            System.out.print("Your command: ");
            if (scanner.hasNext()) command = scanner.nextLine();//scans the users input into the string command
            if (command.equals("exit")) {
                end = true;
            }//if the command is exit, the program exits
            else {
                handle(command);//calls the handle method to deal with the user input
            }
        }
        System.exit(0);// ends the application if the program escapes the while loop.
    }

    public static void handle(String command) {
        try {
            String[] commands = command.split(" ");//splits the command into array to deal with the parameters.
            if (commands[0].equals("share")) {//if the command is share
                String filePath = commands[1];
                int port = Integer.parseInt(commands[commands.length-1]);//get the arguments which are shareing dir path and the port
                ServerSender sending = new ServerSender(port, filePath);
                sending.start();//run the server on another thread
                System.out.println("Sharing the Directory: " + filePath + " at port: "+ port);//update the user
                configurations.ports.add(port);
                if (configurations.ports.size() == 1) {
                    multicasting multicasting = new multicasting();
                    multicasting.start();
                }
            }
            else if (commands[0].equals("connect")) {//if the command was coonect
                String IP = commands[2];
                int port = Integer.parseInt(commands[commands.length-1]);//get the paramters to connect to the server
                ClientReceiver clientReceiver = new ClientReceiver(IP, port);// create a new receiver object
            }
            else if (command.equals("find servers")) {
                try {
                    InetAddress address = InetAddress.getByName("224.0.0.10");
                    MulticastSocket clientSocket = new MulticastSocket(4446);
                    clientSocket.joinGroup(address);
                    //One would join a multicast group by first creating a MulticastSocket with the desired port, then invoking the joinGroup method
                    String rep = "found servers: ";
                    DatagramPacket sender = new DatagramPacket(rep.getBytes(), rep.getBytes().length, address, 4446);
                    clientSocket.send(sender);// send a command to the other multicast sockets to get a reply from the applications that are sharing a dir.
                    byte[] buf = new byte[256];
                    int i = 0 ;
                    while (true) {
                        DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                        clientSocket.setSoTimeout(1000);//if it takes too long for other sockets to reply throw the exception.
                        //this means that all the machines have repsonded and the loop can be exited.
                        clientSocket.receive(msgPacket);//get the message from the applcation tht was sent in response to the request.
                        String msg = new String(buf, 0, buf.length);
                        if (!msg.equals(rep)){//ignore the request that was sent.
                            System.out.println("Available servers at: " + msg);//output the reply to the user.

                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Above are the available systems a user can connect to.");
                    //once the timeout exception is thrown when all the machine that's sharing has replied.
                }
            }
        } catch (FileSystemNotFoundException e) {//dealing with exception thrown by the server for incorrect arguments
            System.out.println("Directory does not exist");
        } catch (Exception e) {
            System.out.println("Incorrect Arguments");
        }
    }

}
