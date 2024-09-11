import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static LinkedList<ServeOneClient> serverList = new LinkedList<>();
    private static File log = new File("serverLog.txt");
    private static final String WELCOME_TEMPLATE = "%s: User with nickname %s has joined the chat";
    private static final String GOOD_BYE_TEMPLATE = "%s: User with nickname %s has left the chat";
    private static List<String> users = new ArrayList<>();
    private static File settings = new File("settings.json");
    private static Random rnd = new Random();

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Server is started at the port %s\n", serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    ServeOneClient soc = new ServeOneClient(socket);
                    serverList.add(soc);
                } catch (IOException e) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getUsers() {
        return users;
    }

    public void addUser(String user) {
        getUsers().add(user);
    }

    public void deleteUser(String user) {
        getUsers().remove(user);
    }

    public static void main(String[] args) {
        int port = Utils.readPort(settings);
        new Server().startServer(port);
    }

    class ServeOneClient extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ServeOneClient(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(startServer()).start();
        }

        private void setNickname(String userNickname) {
            String resultName = null;
            String suffix = Integer.toString(rnd.nextInt(1000) + 1);
            if (getUsers().contains(userNickname)) {
                if (!userNickname.contains("#")) {
                    resultName = userNickname + "#" + suffix;
                } else {
                    resultName = userNickname.substring(0, userNickname.indexOf("#")) + "#" + suffix;
                }
            } else {
                resultName = userNickname;
            }
            this.nickname = resultName;
        }

        public Runnable startServer() {
            Runnable task = (() -> {
                String inputMessage;
                try {
                    String clientNickname = in.readLine();
                    setNickname(clientNickname);
                    send(nickname);
                    addUser(nickname);
                    joinChat(WELCOME_TEMPLATE, nickname);
                    try {
                        while (true) {
                            inputMessage = in.readLine().trim();
                            if (inputMessage.equals("/exit")) {
                                joinChat(GOOD_BYE_TEMPLATE, nickname);
                                deleteUser(nickname);
                                this.stopServer();
                                break;
                            } else if (inputMessage.equals("/allUsers")) {
                                send(getUsers().toString());
                                continue;
                            } else {
                                acceptMessage(inputMessage);
                            }

                        }
                    } catch (IOException e) {
                    }
                } catch (IOException e) {
                    this.stopServer();
                }
            });
            return task;
        }

        private void joinChat(String template, String nickname) throws IOException {
            String message = String.format(template, Utils.printTimeStamp(), nickname);
            Utils.log(message, log);
            sendToAll(message);
        }

        private void acceptMessage(String msg) throws IOException {
            Utils.log(msg, log);
            send("Your message has been delivered");
            sendToAll(msg);
        }

        private void stopServer() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (ServeOneClient soc : Server.serverList) {
                        if (soc.equals(this)) soc.interrupt();
                        Server.serverList.remove(this);
                    }
                }
            } catch (IOException e) {
            }
        }

        private void send(String msg) {
            out.println(msg);
        }

        private void sendToAll(String str) {
            for (ServeOneClient soc : Server.serverList) {
                if (!soc.equals(this)) {
                    soc.send(str);
                }
            }
        }
    }
}
