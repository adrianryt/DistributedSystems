import Client.Client;
import Server.Server;

public class ClientApp {
    public static void main(String[] args) {
        Client client = new Client(6888);
        try {
            client.startClient();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("dadadada");
        } finally {
            client.stopClient();
        }
    }
}
