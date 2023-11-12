import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class BlockingClient {
    public static void main(String[] args) {
        try {
            // Membuat koneksi ke server
            Socket socket = new Socket("localhost", 8888);
            System.out.println("Terhubung ke server.");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            // Kirim identifikasi pengirim ke server
            System.out.print("Masukkan identifikasi pengirim: ");
            String senderId = scanner.nextLine();
            outputStream.write(senderId.getBytes());
            outputStream.write('\n');
            outputStream.flush();

            // Menggunakan thread untuk membaca pesan dari server
            Thread serverResponseThread = new Thread(() -> {
                try {
                    while (true) {
                        // Baca balasan dari server
                        String serverResponse = reader.readLine();
                        System.out.println("Balasan dari server: " + serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverResponseThread.start();

            while (true) {
                // Kirim pesan ke server
                System.out.print("Masukkan pesan (ketik 'bye' untuk keluar): ");
                String message = scanner.nextLine();
                outputStream.write(message.getBytes());
                outputStream.write('\n');
                outputStream.flush();

                // Jika pengguna ingin keluar, tutup koneksi dan keluar dari loop
                if (message.equalsIgnoreCase("bye")) {
                    System.out.println("Menutup koneksi.");
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
