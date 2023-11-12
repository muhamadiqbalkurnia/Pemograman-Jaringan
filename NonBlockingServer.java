import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NonBlockingServer {
    private static final Map<Integer, SocketChannel> connectedClients = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(9999));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server mendengarkan pada port 9999...");

            while (true) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        // Accept connection
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);

                        // Baca port klien
                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        int bytesRead = clientChannel.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip();
                            int clientPort = buffer.getInt();
                            System.out.println("Menerima koneksi dari klien dengan port " + clientPort);
                            connectedClients.put(clientPort, clientChannel);

                            // Pesan selamat datang
                            sendMessageToClient(clientChannel, "Penerimaan: Koneksi diterima, klien dengan port " + clientPort + "!");
                        }
                    } else if (key.isReadable()) {
                        // Read from the client
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        handleRead(clientChannel);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRead(SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            // Connection closed by client
            int clientPort = getClientPort(clientChannel);
            clientChannel.close();
            System.out.println("Koneksi ditutup oleh klien dengan port " + clientPort);
            connectedClients.remove(clientPort);
        } else if (bytesRead > 0) {
            buffer.flip();
            String receivedMessage = receiveMessage(buffer, clientChannel);

            int clientPort = getClientPort(clientChannel);
            System.out.println("Menerima pesan dari klien dengan port " + clientPort + ": " + receivedMessage);

            // Kirim pesan penerimaan ke klien
            sendMessageToClient(clientChannel, "Penerimaan: Pesan diterima dari klien dengan port " + clientPort);
        }
    }

    private static void sendMessageToClient(SocketChannel clientChannel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
    }

    private static String receiveMessage(ByteBuffer buffer, SocketChannel channel) throws IOException {
        StringBuilder receivedMessage = new StringBuilder();
        Charset charset = Charset.forName("UTF-8");
        CharBuffer charBuffer = charset.decode(buffer);

        while (charBuffer.hasRemaining()) {
            char c = charBuffer.get();
            if (c == '\n') {
                break;
            }
            receivedMessage.append(c);
        }

        return receivedMessage.toString();
    }

    private static int getClientPort(SocketChannel clientChannel) {
        for (Map.Entry<Integer, SocketChannel> entry : connectedClients.entrySet()) {
            if (entry.getValue().equals(clientChannel)) {
                return entry.getKey();
            }
        }
        return -1;
    }
}
