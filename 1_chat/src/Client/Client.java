package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    int serverPort;
    Socket clientSocket = null;
    BufferedReader clientReader;
    PrintWriter clientWriter;

    public Client(int serverPort) {
        this.serverPort = serverPort;
    }

    private void startTcpConnectionAndSendName() {
        try {
            this.clientSocket = new Socket("localhost", this.serverPort);
            this.clientWriter = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.clientReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Write your name: ");
            String name = scanner.nextLine();
            clientWriter.println(name);
        } catch(IOException e) {
            e.printStackTrace();
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

    private void listenForMessageToSend() {
        Scanner scanner = new Scanner(System.in);
        String messageToSend;
        while(true) {
            messageToSend = scanner.nextLine();
            this.clientWriter.println(messageToSend);

        }
    }

    public void startClient() {
        this.startTcpConnectionAndSendName();
        this.listenForTCPMessage();
        this.listenForMessageToSend();
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
