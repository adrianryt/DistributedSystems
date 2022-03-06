package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    int serverPort;
    String name;
    Socket clientSocket = null;
    BufferedReader clientReader;
    PrintWriter clientWriter;

    DatagramSocket updSocket;
    InetAddress serverAddress;

    MulticastSocket multicastSocket;
    int multicastPort = 7888;
    String multicastAddressName = null;
    InetAddress multicastInetAddress;

    public Client(int serverPort) {
        this.serverPort = serverPort;
    }

    private void askToJoinMulticastGroup() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to join Multicast? [Yes/No]");
        String answer = scanner.nextLine();
        if(answer.equals("Yes")){
            this.multicastAddressName = "239.1.1.1";
        }
    }

    private void startTcpConnectionAndSendName() {
        try {
            this.clientSocket = new Socket("localhost", this.serverPort);
            this.clientWriter = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.clientReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Write your name: ");
            String name = scanner.nextLine();
            this.name = name;
            clientWriter.println(name);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void startUDPConnection() {
        try {
            this.updSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            byte[] sendBuffer = "connect".getBytes();
            DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, sendBuffer.length, this.serverAddress, this.serverPort);
            try {
                this.updSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMulticastConnection() {
        if(this.multicastAddressName == null) return;
        try {
            this.multicastSocket = new MulticastSocket(this.multicastPort);
            this.multicastInetAddress = InetAddress.getByName(this.multicastAddressName);
            this.multicastSocket.joinGroup(this.multicastInetAddress);
            System.out.println("Multicast connection started");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Multicast Problems");
        }
    }

    private void listenForTCPMessage() {
        Runnable runnableTask = () -> {
            while (true) {
                try {
                    String messageFromOtherUser = this.clientReader.readLine();
                    System.out.println(messageFromOtherUser);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        this.executorService.execute(runnableTask);
    }

    private void listenForUPDMessage() {
        Runnable runnableTask = () -> {
            byte[] receiveBuffer = new byte[2048];
            while (true) {
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    this.updSocket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("UDP: \n" + new String(receivePacket.getData(),0,receivePacket.getLength()));
            }
        };
        this.executorService.execute(runnableTask);
    }

    private void listenForMulticast() {
        if(this.multicastAddressName == null) return;
        Runnable runnableTask = () -> {
            byte[] receiveBuffer = new byte[1024];
            while (true) {
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    this.multicastSocket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Multicast: \n" + new String(receivePacket.getData(),0,receivePacket.getLength()));
            }
        };
        this.executorService.execute(runnableTask);
    }

    public void startClient() {
        this.askToJoinMulticastGroup();
        this.startTcpConnectionAndSendName();
        this.startUDPConnection();
        this.startMulticastConnection();
        this.listenForTCPMessage();
        this.listenForUPDMessage();
        this.listenForMulticast();
        this.listenForMessageToSend();
    }

    private void listenForMessageToSend() {
        Scanner scanner = new Scanner(System.in);
        String messageToSend;
        while(true) {
            messageToSend = scanner.nextLine();
            if(messageToSend.equals("U")) {
                String asciiart = "░░░░░▄▄▄▄▀▀▀▀▀▀▀▀▄▄▄▄▄▄░░░░░░░░\n" +
                                    "░░░░░█░░░░▒▒▒▒▒▒▒▒▒▒▒▒░░▀▀▄░░░░\n" +
                                    "░░░░█░░░▒▒▒▒▒▒░░░░░░░░▒▒▒░░█░░░\n" +
                                    "░░░█░░░░░░▄██▀▄▄░░░░░▄▄▄░░░░█░░\n" +
                                    "░▄▀▒▄▄▄▒░█▀▀▀▀▄▄█░░░██▄▄█░░░░█░\n" +
                                    "█░▒█▒▄░▀▄▄▄▀░░░░░░░░█░░░▒▒▒▒▒░█\n" +
                                    "█░▒█░█▀▄▄░░░░░█▀░░░░▀▄░░▄▀▀▀▄▒█\n" +
                                    "░█░▀▄░█▄░█▀▄▄░▀░▀▀░▄▄▀░░░░█░░█░\n" +
                                    "░░█░░░▀▄▀█▄▄░█▀▀▀▄▄▄▄▀▀█▀██░█░░\n" +
                                    "░░░█░░░░██░░▀█▄▄▄█▄▄█▄████░█░░░\n" +
                                    "░░░░█░░░░▀▀▄░█░░░█░█▀██████░█░░\n" +
                                    "░░░░░▀▄░░░░░▀▀▄▄▄█▄█▄█▄█▄▀░░█░░\n" +
                                    "░░░░░░░▀▄▄░▒▒▒▒░░░░░░░░░░▒░░░█░\n" +
                                    "░░░░░░░░░░▀▀▄▄░▒▒▒▒▒▒▒▒▒▒░░░░█░\n" +
                                    "░░░░░░░░░░░░░░▀▄▄▄▄▄░░░░░░░░█░░";
//                asciiart = "⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄\n" +
//                        "⠄⠄⠄⠄⠄⠄⠄⣀⣀⣐⡀⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄\n" +
//                        "⠄⠄⢠⠄⣠⣶⣿⣿⣿⠿⠿⣛⣂⣀⣀⡒⠶⣶⣤⣤⣬⣀⡀⠄⢀⠄⠄⠄⠄⠄⠄⠄\n" +
//                        "⠄⠄⢀⣾⣿⣿⣿⡟⢡⢾⣿⣿⣿⣿⣿⣿⣶⣌⠻⣿⣿⣿⣿⣷⣦⣄⡀⠄⠄⠄⠄⠄\n" +
//                        "⠄⠄⣈⣉⡛⣿⣿⣿⡌⢇⢻⣿⣿⣿⣿⣿⠿⠛⣡⣿⣿⣿⣿⣿⣿⣿⣿⣦⣄⠄⠄⠄\n" +
//                        "⠄⠺⠟⣉⣴⡿⠛⣩⣾⣎⠳⠿⠛⣋⣩⣴⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⠄⠄\n" +
//                        "⠄⠄⠄⠘⢋⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡆⠄\n" +
//                        "⠄⠄⢀⢀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠄\n" +
//                        "⠄⠄⠄⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⣀\n" +
//                        "⠄⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠃⠘⠛\n" +
//                        "⠄⠄⠄⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠋⣀⣀⣠⣤\n" +
//                        "⠄⠄⣀⣀⡙⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⢛⣩⠤⠾⠄⠛⠋⠉⢉\n" +
//                        "⠄⠺⠿⠛⠛⠃⠄⠉⠙⠛⠛⠻⠿⠿⠿⠟⠛⠛⠛⠉⠁⠄⠄⣀⣀⣠⣤⣠⣴⣶⣼⣿";
                byte[] sendBuffer = asciiart.getBytes();
                DatagramPacket sendPacket =
                        new DatagramPacket(sendBuffer, sendBuffer.length, this.serverAddress, this.serverPort);
                try {
                    this.updSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(messageToSend.equals("M")) {
                System.out.println("Write your multicast message: ");
                String multicastMessage = scanner.nextLine();
                multicastMessage = this.name + ": " + multicastMessage;
                byte[] sendBuffer = multicastMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, this.multicastInetAddress, this.multicastPort);
                try {
                    this.multicastSocket.leaveGroup(this.multicastInetAddress);
                    this.multicastSocket.send(sendPacket);
                    this.multicastSocket.joinGroup(this.multicastInetAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.clientWriter.println(messageToSend);
            }
        }
    }

    public void stopClient() {
        try {
            if(this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
