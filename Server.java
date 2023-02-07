package tcp2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Server {
    public static final int port = 3434;
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Server running on port: " + port);
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("New connection: " + socket);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                ClientHandler handler = new ClientHandler(socket, ois, oos);
                clients.add(handler);
                handler.start();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ClientHandler extends Thread {
    public String name;
    public boolean isOnline = false;
    public Socket socket;
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;

    public ClientHandler(Socket socket, ObjectInputStream objectInputStream,ObjectOutputStream objectOutputStream) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String request = objectInputStream.readUTF();
                if (request.split(" ")[0].toLowerCase().equals("login")) {
                    this.name = request.split(" ")[1];
                    this.isOnline = true;
                    objectOutputStream.writeUTF("You are now logged in " + request.split(" ")[1]);
                } else if (request.split(" ")[0].toLowerCase().equals("get-clients")) {
                    if (!this.isOnline) {
                        objectOutputStream.writeUTF("Please log in first. (use command \"login name)\", where name is your name.");
                    } else {
                        StringBuilder s = new StringBuilder();
                        for (int i = 0; i < Server.clients.size(); i++) {
                            if (Server.clients.get(i).isOnline) {
                                s.append(Server.clients.get(i).name);
                                if (i != Server.clients.size() - 1) {
                                    s.append(", ");
                                }
                            }
                        }
                        objectOutputStream.writeUTF(s.toString());
                    }
                }
                else if (request.equals("logout")) {
                    this.isOnline = false;
                    objectOutputStream.writeUTF("You are now logged out. (To login again use command \"login name)\", where name is your name.");
                }
                else if (request.split("#")[0].toLowerCase(Locale.ROOT).equals("send_to")) {
                    boolean receiver_found = false;
                    for (int i = 0; i < Server.clients.size(); i++) {
                        if (Server.clients.get(i).name.equals(request.split("@@")[1])) {
                            Server.clients.get(i).objectOutputStream.writeUTF(request.split("#")[1].split("@@")[0]);
                            Server.clients.get(i).objectOutputStream.flush();
                            receiver_found = true;
                            break;
                        }
                    }
                    if (!receiver_found) {
                        objectOutputStream.writeUTF("User " + request.split("@@")[1] + " not found.");
                        objectOutputStream.flush();
                    } else {
                        objectOutputStream.writeUTF("Message to " + request.split("@@")[1] + " was sent successfully.");
                        objectOutputStream.flush();
                    }
                }
                else {
                    objectOutputStream.writeUTF("Unknown command...");
                }
                System.out.println(request);
                objectOutputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ClientInfo {
    public String name;
    public ClientHandler clientHandler;

    public ClientInfo(String name, ClientHandler clientHandler) {
        this.name = name;
        this.clientHandler = clientHandler;
    }
}