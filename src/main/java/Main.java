import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try (ServerSocket socket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println("Client connected");

                executor.submit(() -> {
                    try {
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clientHandler.handle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

    }
}
