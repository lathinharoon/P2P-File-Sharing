import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystemNotFoundException;

public class ServerSender extends Thread{//subclass of threadd
    ServerSocket ss;
    File file;
    public ServerSender(int port, String filePath) {
        try {
            ss = new ServerSocket(port);//make a server socket at the port given by the user
            file = new File(filePath);
            if (!file.exists() || !file.isDirectory()) {
                throw new FileSystemNotFoundException();
            }// check if the directory is valid
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {//subroutine which runs in an alternate thred
        try {
            while (true) {// infinte loop to deal with connection requests from clients.
                Socket connection = ss.accept();//accept the socket
                ServerSenderThread newThread = new ServerSenderThread(connection,file);
                newThread.start();//deal with the conection between the server and the client on another thread
                // so a single machine can have multiple clients.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
