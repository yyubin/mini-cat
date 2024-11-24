import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void handle() {
        try {
            String requestLine;
            while ((requestLine = reader.readLine()) != null && !requestLine.isEmpty()) {
                System.out.println("Request: " + requestLine);
            }
            String result = processRequest(requestLine);
            System.out.println("Response: " + result);
            writer.print(result);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) {
        return  """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: 13

                    Hello, world!
                    """;
    }
}
