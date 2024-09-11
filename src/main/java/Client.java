import java.io.*;
import java.net.Socket;

public class Client {
    private static final String MESSAGE_TEMPLATE = "%s %s: %s";
    private static File log = new File("log.txt");
    private static File settings = new File("settings.json");
    private Socket socket;
    private String nickname;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader userInput;

    public Client(String host, int port) {
        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            System.err.println("Connection is failed");
        }
        System.out.printf("Client is connected, port %s\n", socket.getPort());
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            userInput = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            this.stopClient();
        }
    }

    public void startClient() {
        setNickname();
        new Thread(readingThread()).start();
        new Thread(writingThread()).start();
    }

    public void setNickname() {
        System.out.println("Please specify your nickname: ");
        try {
            while (true) {
                String userNickname = userInput.readLine().trim();
                if (userNickname.isBlank() || userNickname == null) {
                    System.out.println("Nickname can't be empty, please try again");
                    continue;
                } else {
                    send(userNickname);
                    userNickname = in.readLine();
                    this.nickname = userNickname;
                    System.out.printf("Welcome, your nickname is %s\n", nickname);
                    break;
                }
            }
        } catch (IOException e) {
        }
    }

    private void send(String msg) {
        out.println(msg);
    }

    Runnable writingThread() {
        Runnable task = () -> {
            while (true) {
                String outMessage;
                try {
                    outMessage = (userInput.readLine()).trim();
                    if (outMessage.equals("/exit")) {
                        System.out.println("Disconnected...");
                        send(outMessage);
                        this.stopClient();
                        break;
                    } else if (outMessage.equals("/allUsers")) {
                        send(outMessage);
                    } else {
                        sendRequest(outMessage);
                    }
                } catch (IOException e) {
                    this.stopClient();
                }
            }
        };
        return task;
    }

    private void sendRequest(String userInput) throws IOException {
        String message;
        if (!userInput.trim().isBlank()) {
            message = formatMessage(MESSAGE_TEMPLATE, userInput);
            Utils.log(message, log);
            send(message);
        }
    }

    Runnable readingThread() {
        Runnable task = () -> {
            String inMessage;
            try {
                while (true) {
                    inMessage = in.readLine();
                    System.out.println(inMessage);
                }

            } catch (IOException e) {
                this.stopClient();
            }
        };
        return task;
    }

    private String formatMessage(String template, String str) {
        return String.format(template, Utils.printTimeStamp(), nickname, str);
    }

    private void stopClient() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
        }

    }

    public static void main(String[] args) {
        String host = Utils.readHost(settings);
        int port = Utils.readPort(settings);
        new Client(host, port).startClient();
    }
}
