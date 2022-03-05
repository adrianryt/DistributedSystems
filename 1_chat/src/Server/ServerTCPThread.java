package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerTCPThread implements Runnable {

    private ConcurrentLinkedQueue<PrintWriter> userWriters;
    private Socket clientSocket;
    private BufferedReader clientRead;
    private PrintWriter clientWrite;

    private String name;

    public ServerTCPThread(Socket clientSocket, BufferedReader clientRead, PrintWriter clientWrite, ConcurrentLinkedQueue<PrintWriter> userWriters, String name) {
        this.clientSocket = clientSocket;
        this.userWriters = userWriters;
        this.clientRead = clientRead;
        this.clientWrite = clientWrite;
        this.name = name;
    }

    @Override
    public void run() {
        String userMessage;
        while(true) {
            try {
                userMessage = clientRead.readLine();
                System.out.println("Server TCP received: " + userMessage);

                for(PrintWriter userWriter: this.userWriters){
                    if(userWriter != this.clientWrite){
                        userWriter.println(this.name + ": " + userMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
