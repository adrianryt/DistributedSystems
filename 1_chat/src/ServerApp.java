import Server.Server;

import java.io.IOException;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        Server server = new Server(6888);
        try {
            server.startServer();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("jojojo");
        } finally {
            server.closeServer();
        }
    }
}
