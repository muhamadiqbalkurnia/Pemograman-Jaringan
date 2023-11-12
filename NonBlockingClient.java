import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NonBlockingClient {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 9999));
            socketChannel.configureBlocking(false);

            System.out.println("Terhubung ke server.");

            Scanner scanner = new Scanner(System.in);

            // Baca port klien
            System.out.print("Masukkan port klien: ");
            int clientPort = Integer.parseInt(scanner.nextLine());
            sendClientPort(socketChannel, clientPort);

            // Baca pesan selamat datang dari server
            String welcomeMessage = readMessage(socketChannel);
            System.out.println(welcomeMessage);

            while (true) {
                // Baca pesan dari pengguna
                System.out.print("Masukkan pesan (ketik 'bye' untuk keluar): ");
                String message = scanner.nextLine();
                sendMessage(socketChannel, message);

                // Baca balasan dari server
                String serverResponse = readMessage(socketChannel);
                System.out.println("Balasan dari server: " + serverResponse);

                if (message.equalsIgnoreCase("bye")) {
                    System.out.println("Menutup koneksi.");
                    break;
                }
            }

            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendClientPort(SocketChannel socketChannel, int clientPort) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(clientPort);
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private static void sendMessage(SocketChannel socketChannel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private static String readMessage(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return new String(data, StandardCharsets.UTF_8);
        }
        return "";
    }
}
