package tcp2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int port = 3434;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            MessageReceiver receiver = new MessageReceiver(socket, ois);
            MessageSender sender = new MessageSender(socket, oos);

            receiver.start();
            sender.start();
            receiver.join();
            sender.join();

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class MessageSender extends Thread {
    public Socket socket;
    public ObjectOutputStream objectOutputStream;

    public MessageSender(Socket socket, ObjectOutputStream objectOutputStream) {
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            try {
                this.objectOutputStream.writeUTF(message);
                this.objectOutputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class MessageReceiver extends Thread {
    public Socket socket;
    public ObjectInputStream objectInputStream;

    public MessageReceiver(Socket socket, ObjectInputStream objectInputStream) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {
        while (true) {
            String response = null;
            try {
                response = this.objectInputStream.readUTF();
                System.out.println(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
