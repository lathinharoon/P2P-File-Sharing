import java.net.*;
//the link below is the place where i got the code for some aspects of the class
//https://stackoverflow.com/questions/3258959/network-discovery-in-java-using-multicasting
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html accessed at 29th oct at 2 am.
public class multicasting extends Thread{//code which will run on another thread.

    public void run() {
        try {
            MulticastSocket s = new MulticastSocket(4446);//create new multicast socket at a specifc port
            InetAddress group = InetAddress.getByName("224.0.0.10");//
            s.joinGroup(group);//the joinGroup() method, with the multicast IP as an argument. This is necessary to be able to receive the packets published to this group.
            byte[] buf =new byte[256];// create a new buffer
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                s.receive(packet);//receive a request from the user.
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String rep = "IP:- " + InetAddress.getLocalHost() + " on ports: " + configurations.ports;
                buf = rep.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                //once the user sends in a request, get the ip address and ports its conncetd to if its sharing multiple dir's.
                s.send(packet);//send the packet to other sockets
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
