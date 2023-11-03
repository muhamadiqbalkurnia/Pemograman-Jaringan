import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 8888);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            InetAddress clientAddress = clientSocket.getInetAddress();
            String clientName = "Client " + clientAddress.getHostAddress(); // Menggunakan alamat IP sebagai nama

            Thread receiveThread = new Thread(new ReceiveMessage(reader, clientName));
            receiveThread.start();

            while (true) {
                String message = scanner.nextLine();
                writer.println(clientName + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ReceiveMessage implements Runnable {
        private BufferedReader reader;
        private String clientName;

        public ReceiveMessage(BufferedReader reader, String clientName) {
            this.reader = reader;
            this.clientName = clientName;
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
