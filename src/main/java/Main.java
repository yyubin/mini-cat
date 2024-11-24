import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        try (ServerSocket socket = new ServerSocket(8080)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println("Client connected");

                new Thread(new ClientHandler(clientSocket)).start();
            }
        }

    }
}
