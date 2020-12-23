import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientReceiver {// class that receieves the file from other machines and manages the connection with th servers.
    List<Socket> sockets;//an arraylist which will hold all the sockets
    Socket s;
    BufferedInputStream inputStream;
    BufferedOutputStream outputStream;//input output streams for sockets
    Scanner scanner;// scanner to get user inpputs
    File downloadDir = null;// directory to store the downloaded files
    List<List<String>> files;//list of list of strings which hold the names of the files that being shared from each servser
    public ClientReceiver(String IP, int port) {
        try {
            sockets = new ArrayList<>();
            sockets.add(new Socket(IP, port));//make a new socket using the param and it to the list
            System.out.println("Connected to "+IP+" at port: "+port);//confirm to the user if the connection was succesfull
            files = new ArrayList<>();
            listFiles();//update the List<List<String>> file
            scanner = new Scanner(System.in);
            System.out.println("--------------------------------------------------------------------------------------");
            System.out.println("Set of Commands for server:\n" +
                    "+ Set your download Dir: set <pathForDirectory> eg:- set /Users/user/dir/dir1/downloadDir/\n" +
                    "+ list shared files: list files\n" +
                    "+ list the computers that are sharing: list servers\n" +
                    "+ for seeing the list of servers available to connect to: find servers\n" +
                    "+ search for specific file: search <filename>\n" +
                    "+ search for specific file by extension: search <.format>\n" +
                    "+ download a specific file: get <filename.format> from machine <machine no.> eg:- get test.txt from directory 2\n" +
                    "+ connect to another machine: connect to <ip> at <port>\n" +
                    "+ for sharing a directory: share <pathForDirectory> at <port>\n" +
                    "+ close your connection: exit");//inform the user about the set of commands availbal in this class
            String command = "";
            boolean end = false;
            while (!end) {// a while loop which runs until the user use the command exit to exit the loop and return to the FileShareMain class
                System.out.print("Your command for server: ");
                command = scanner.nextLine();//scans the users input into the string command
                if (command.equals("exit")) {// if the users tell it to exit
                    end = true;// set to true
                    inputStream.close();
                    outputStream.close();//close the input output streams
                    for (Socket s : sockets) {
                        s.close();
                        System.out.println("connection closed with the machine at IP: " + s.getInetAddress() + " at port: " + s.getPort());
                    }//close all the active socket connections
                }
                else {
                    try {
                        handle(command);//handle the command given by the user
                    } catch (Exception ee) {
                        sockets.remove(s);
                        //if there was an abnormal server side termination for a specific server remove it from the list
                    }
                }
            }
        } catch (ConnectException e) {
           System.out.println("Connection refused: Incorrect arguments");//inform the user about the exception.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handle(String command) throws IOException {//handle the user command
        if (command.length()>=3 && command.substring(0,3).equals("set")) {//if the user wants to setup a download dir
            String[] commands = command.split(" ");
            File temp = downloadDir;//pointer to set the download directory back to its orignal position if the dir is not valid
            downloadDir = new File(commands[1]);//get the argument
            if (!downloadDir.exists()) {
                System.out.println("The download directory path you entered does not exist.");
                downloadDir = temp;
            }
            else if (!downloadDir.isDirectory()) {
                System.out.println("The download directory path you entered is a file.");
                downloadDir = temp;
            }//check if the directory path is valid and is a directory
            else {
                System.out.println("The Download Directory is set.");
            }
        }
        else if (command.equals("list files")) {// if the user tells the prgram to list all the files in the program
            listFiles();//update the list
            System.out.println("There are " + files.size() + " Shared Directories: ");//get how many direcotries are shared
            for (int i = 1; i <= files.size(); i++) {// for each machine i.e. directoreis being shared
                List<String> dirFile = files.get(i-1);
                System.out.println("In machine number: "+i+"'s shared directory, there are: ");
                for (String f: dirFile) {
                    System.out.println("> "+f);//output to the user the file in that directories name.
                }
            }
        }
        else if (command.equals("list servers")) {//if the user tells the app to list all the macgines its connected to
            System.out.println("The Machines that we are accessing the shared directories: ");
            for (int i = 1; i <= sockets.size(); i++) {
                System.out.println("Machine "+i+": IP: "+sockets.get(i-1).getInetAddress()+" at port: "+sockets.get(i-1).getPort());
            }//print out the ip add and the port its running on
        }
        else if (command.equals("find servers")) {//this is th command that will output to the user of all the available machines that are sharing
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
                    System.out.println("Available servers at: " + msg);//output the reply to the user.
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Above are the available systems a user can connect to.");
                //once the timeout exception is thrown when all the machine that's sharing has replied.
            }
        }
        else if (command.length()>=6 && command.substring(0,6).equals("search")) {//the user wants to search for a name or filetype
            String[] commands = command.split(" ");
            listFiles();//update the list of files
            try {
                boolean found = false;
                String fName = commands[1];//get the param
                for (int i = 1; i <= files.size(); i++) {//for each directory being shared
                    List<String> dirFile = files.get(i-1);
                    for (String f: dirFile) {//in each file in those directory
                        if (f.contains(fName)) {//if the file contains the sequence of chars
                            System.out.println("The File: "+f+" is shared from machine i.e. shared directory "+i);
                            //it prints to the screen its full name and which machine its being shared from
                            found = true;
                        }
                    }
                }
                if (!found) System.out.println("no file matches your parameter");
            } catch (Exception e) {
                System.out.println("Incorrect arguments");//catches an exception
            }
        }
        else if (command.length()>=3 && command.substring(0,3).equals("get")) {//user wants to download a file
            try {
                listFiles();
                String[] commands = command.split(" ");
                if (downloadDir != null) {//if a download dir has been setup
                    boolean found = false;
                    int p = Integer.parseInt(commands[commands.length-1])-1;//get the machine number
                    List<String> dirFile = files.get(p);//get the list of files for that machine
                    for (int j = 0; j < dirFile.size(); j++) {//for each file in that machine shared dir
                        if (dirFile.get(j).equals(commands[1])) {//if the filename macthes
                            found = true;//set found to true
                            s = sockets.get(p);//get the socket connection
                            outputStream = new BufferedOutputStream(s.getOutputStream());
                            inputStream = new BufferedInputStream(s.getInputStream());
                            //assign the input and output streams to send the data over
                            write(command);//write the command to the specified server
                            int preTransLength = inputStream.read(); // according to the protocol the server will 1st send the length of the string
                            String preTrans = "";
                            for (int k = 0; k < preTransLength; k++) {
                                preTrans += (char)inputStream.read();
                            }//use the length to make the reply sent by the server
                            System.out.println(preTrans);//print to the the user
                            String len = preTrans.substring(preTrans.lastIndexOf(" ")+1);
                            Long length = Long.parseLong(len);//get the length of the file thats being shared
                            File file = new File(downloadDir.getAbsolutePath()+"/"+commands[1]);//make a new file in the download dir
                            FileOutputStream outputStreamSave = new FileOutputStream(file);// create a new output stream for that file
                            for (long  k = 0; k<length; k++) {//for loop which iterates from 0 to length of the file -1.
                                outputStreamSave.write(inputStream.read());//to read the incoming data and then write it to the file
                                if ((length-1)*0.25==k){
                                    System.out.println("file transfer progress: 25%");
                                }
                                else if ((length-1)*0.5 == k){
                                    System.out.println("file transfer progress: 50%");
                                }
                                else if ((length-1)*0.75 == k){
                                    System.out.println("file transfer progress: 75%");
                                }//output to the user about the progress
                            }
                            outputStreamSave.flush();
                            outputStreamSave.close();//flush and close the file output stream
                            int postTransLength = inputStream.read();
                            String postTrans = "";
                            for (int z = 0; z < postTransLength; z++) {
                                postTrans += (char) inputStream.read();
                            }
                            System.out.println(postTrans);//get data about the file transmission after the transfer is completed to confirm
                            System.out.println("File received.");
                            break;//brek out of the for loop
                        }
                    }
                    if (!found) {
                        System.out.println("No such file exists.");//if fthe file is not found tell the user
                    }
                }
                else {
                    System.out.println("Set a Download Directory before downloading files.");
                }//must set a download dir
            } catch (Exception e) {
                System.out.println("Incorrect arguments");
            }//catch any exceptions
        }
        else if (command.length()>=7 && command.substring(0,7).equals("connect")) {
            try {//connect to more machines
                String[] commands = command.split(" ");
                String IP = commands[2];
                int port = Integer.parseInt(commands[commands.length-1]);//get the paramters to connect to the server
                boolean exists = false;
                for (Socket socket: sockets) {
                    if (socket.getPort() == port && socket.getInetAddress().toString().contains(IP)){
                        exists = true;
                    }
                }
                if (exists) {
                    System.out.println("connection already esists");
                }
                else {
                    sockets.add(new Socket(IP,port));// add it to the list of servers
                    listFiles();//update the list of files
                    System.out.println("Connected to "+IP+" at port: "+port);//print to the user
                }
            } catch (Exception e) {
                System.out.println("Incorrect parameters");
            }
        }
        else if (command.length()>5 && command.substring(0,5).equals("share")) {// at this point if the user wants to share a dir
            String[] commands = command.split(" ");
            String filePath = commands[1];
            int port = Integer.parseInt(commands[commands.length-1]);//get the arguments which are shareing dir path and the port
            ServerSender sending = new ServerSender(port, filePath);
            sending.start();//run the server on another thread
            System.out.println("Sharing the Directory: " + filePath + " at port: "+ port);
            configurations.ports.add(port);
            if (configurations.ports.size() == 1) {
                multicasting multicasting = new multicasting();
                multicasting.start();//start the multicasting class on another thread for the 1st server, which will run on a while loop
            }
        }
        else {
            System.out.println("Unrecognisable command");
        }

    }
    private void write(String sending) throws IOException {// method to make the process of writing to the server easier
        outputStream.write(sending.length());
        outputStream.write(sending.getBytes());
        outputStream.flush();
    }
    private void listFiles() throws IOException {// a method to update the list of files that the the receiever can download
        files = new ArrayList<>();//make a new list
        String sending = "list files";
        for (int i = 0; i < sockets.size(); i++) {// for each socket conectiion
            outputStream = new BufferedOutputStream(sockets.get(i).getOutputStream());
            inputStream = new BufferedInputStream(sockets.get(i).getInputStream());//set the input and output streams
            write(sending);// write to the server with the command "list files"
            List<String> f = new ArrayList<>();//for each dir make a new list of strings
            int numFiles = inputStream.read();// get how many files are in the dir
            for (int j = 0; j < numFiles; j++) {// for each file
                int nameLength = inputStream.read();// get the length of the stirng
                String fileName = "";
                for (int k = 0; k < nameLength; k++) {
                    fileName += (char) inputStream.read();
                }//build the name up from the incoming bytes
                f.add(fileName);// add it to the list
            }
            files.add(f);//add the list to the list of list of strings
        }
    }

}