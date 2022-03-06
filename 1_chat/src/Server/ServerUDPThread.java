package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerUDPThread implements Runnable {
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<ClientUDPInfo> UDPClientsInfo;

    public ServerUDPThread(DatagramSocket socket, ConcurrentLinkedQueue<ClientUDPInfo> userWriters) {
        this.socket = socket;
        this.UDPClientsInfo = userWriters;
        System.out.println("Start UDP Server");
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[2048];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        String messageToOtherUsers;
        while(true) {
            try {
                this.socket.receive(receivePacket);
                messageToOtherUsers = new String(receivePacket.getData(), 0, receivePacket.getLength());

                if(messageToOtherUsers.equals("connect")){
                    this.UDPClientsInfo.add(new ClientUDPInfo(receivePacket.getAddress(),receivePacket.getPort()));
                } else {
                    ClientUDPInfo sender = new ClientUDPInfo(receivePacket.getAddress(), receivePacket.getPort());
                    System.out.println("UDP: " + messageToOtherUsers);

                    for(ClientUDPInfo udpClient: this.UDPClientsInfo){
                        if(!(udpClient.getAddress().equals(sender.getAddress()) && udpClient.getPort() == sender.getPort())) {
                            byte[] sendBuffer = messageToOtherUsers.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, udpClient.getAddress(), udpClient.getPort());
                            socket.send(sendPacket);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
