import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server mendengarkan pada port 8888...");

            while (true) {
                // Menerima koneksi dari klien
                Socket clientSocket = serverSocket.accept();
                System.out.println("Menerima koneksi dari " + clientSocket.getInetAddress());

                // Menggunakan thread untuk menangani setiap koneksi klien
                Thread clientHandlerThread = new Thread(() -> handleClient(clientSocket));
                clientHandlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();

            // Baca identifikasi pengirim dari klien
            String senderId = reader.readLine();
            System.out.println("Identifikasi pengirim dari " + clientSocket.getInetAddress() + ": " + senderId);

            while (true) {
                // Baca pesan dari klien
                String clientMessage = reader.readLine();
                System.out.println("Pesan dari " + senderId + ": " + clientMessage);

                // Kirim balasan ke klien
                String serverResponse = "Server: Pesan diterima dari " + senderId;
                outputStream.write(serverResponse.getBytes());
                outputStream.write('\n');
                outputStream.flush();

                // Jika klien mengirim "bye", tutup koneksi
                if (clientMessage.equalsIgnoreCase("bye")) {
                    System.out.println("Menutup koneksi dengan " + clientSocket.getInetAddress());
                    clientSocket.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
