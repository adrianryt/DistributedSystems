package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int portNumber;
    private ServerSocket tcpServerSocket;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private ConcurrentLinkedQueue<PrintWriter> printWriters = new ConcurrentLinkedQueue<PrintWriter>();

    public Server(int portNumber) {
        this.portNumber = portNumber;
    }

    public void startServer() throws IOException {
        tcpServerSocket = new ServerSocket(this.portNumber);
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = this.tcpServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter client_write = null;
            BufferedReader client_read = null;
            try {
                client_write = new PrintWriter(clientSocket.getOutputStream(), true);
                client_read = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e){
                e.printStackTrace();
            }

            if(client_write != null && client_read != null){
                this.printWriters.add(client_write);
                String name = client_read.readLine();
                System.out.println(name);
                executorService.execute(new ServerTCPThread(clientSocket, client_read, client_write, this.printWriters, name));
            } else {
                System.out.println("Coś sie wyjebało");
            }

        }
    }

    public void closeServer() throws IOException {
        this.executorService.shutdown();
        if (this.tcpServerSocket != null) {
            this.tcpServerSocket.close();
        }
        System.out.println("Server down");

    }
}
