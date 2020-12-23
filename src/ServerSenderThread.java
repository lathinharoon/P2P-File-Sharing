import java.io.*;
import java.net.Socket;

public class ServerSenderThread extends Thread{

    BufferedInputStream inputStream;
    BufferedOutputStream outputStream;//buffered output streams for the server to deal with the communication
    Socket connection;
    File SharedDir;//the directory that being shared to the clients
    File chosenFile;// the file in the shred dir thats chosen by the user to download
    File[] files;//list of files in the shared dir
    public ServerSenderThread(Socket connection, File file) {
        this.connection = connection;
        this.SharedDir = file;
    }//constructor that assigns the parameters to the properties.

    public synchronized void run() {//the part that runs on an alternate thread.
        try {
            outputStream = new BufferedOutputStream(connection.getOutputStream());//wrp the servers output stream in buffered output stream
            BufferedInputStream clientReply = new BufferedInputStream(connection.getInputStream());//wrp the servers input stream in buffered output stream
            String[] replies;
            while (true) {//infinite loop that awaits for communication from the client
                int length = clientReply.read();// according to the protocol the client will 1st send the length of the string
                String reply = "";
                for (int i = 0; i < length; i++) {
                    reply += (char)clientReply.read();
                }//use the length to make the command sent by the client
                replies = reply.split(" ");// split into an array
                if (reply.equals("list files")) {//if the command is to list the files
                    listFiles();
                }
                else if (replies[0].equals("get")) {// if the command is to get get a specific file
                    for (File i : files) {
                        if (i.getName().equals(replies[1])) {
                            chosenFile = i;
                        }
                    }// iterate through the list of files and choose the file that the client requested
                    String preTrans = "The File " + chosenFile.getName() + " is ready to be downloaded. The size of the file is: " + chosenFile.length();
                    write(preTrans);//send a pre transmission message confirming the name of the file and its length.
                    inputStream = new BufferedInputStream(new FileInputStream(chosenFile));
                    //buffered input stream which wraps a file input stream of the chosen file.
                    for (long  k = 0; k < chosenFile.length(); k++) {
                        outputStream.write(inputStream.read());
                    }//for loop which iterates from 0 to length of the file -1. i am aware of the method using a buffer to copy which is more efficient,
                    //i just dont completly understand how it works so i decied to use a way tht i understand.
                    outputStream.flush();// flush the data through
                    inputStream.close();// close the input stream
                    write("File has been successfully shared.");//sends a confirmaion to the client
                }
            }

        } catch (Exception e) {
            clean();//if there was abnormal client side activity close the connection cleanly
        }
    }
    private void listFiles() throws IOException {//method to write a list of all the files to the client
        files = SharedDir.listFiles();
        outputStream.write(files.length);//write to the client how many files are in the dir
        for (int i = 0; i < files.length; i++) {//for each file
            write(files[i].getName());// write the name of the file to the client
        }
        outputStream.flush();// flush the data through.
    }
    private void write(String Sending) throws IOException {// method to make the process of writing to the client easier
        outputStream.write(Sending.length());//1st write how long the stirng is
        outputStream.write(Sending.getBytes());// then write the converted byte array so the cleint side can conver the bytes into a string and understand
        outputStream.flush();// flush the data through
    }
    private void clean() {
        try {
            if (connection != null) connection.close();//close the connection if the connection is not null
        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }

}
