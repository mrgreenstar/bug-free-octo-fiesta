package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static LinkedList<String> userList = new LinkedList<>();
    private static HashMap<Socket, String> userMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            int i = 1;
            ServerSocket s = new ServerSocket(8189);
            System.out.println("Сервер запущен");

            while (true) {
                Socket incoming = s.accept();
                Scanner in = new Scanner(incoming.getInputStream());
                String nickname = in.nextLine();
                // Проверка ника на уникальность
                if (userList.contains(nickname)) {
                    incoming.close();
                }
                else {
                    Server.SendToAll("Пользователь " + nickname + " подключился");
                    userList.add(nickname);
                    userMap.put(incoming, nickname);
                    System.out.println("Соединение " + i);
                    Runnable r = new ClientHandler(incoming);
                    new Thread(r).start();
                    i++;
                }
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public static void SendToAll(String message) {
        userMap.forEach( (connection, nickname) ->  {
            try {
                if (!connection.isClosed()){
                    OutputStream output = connection.getOutputStream();
                    PrintWriter out = new PrintWriter(output, true);
                    out.println(message);
                }
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
        });
    }

    public static void deleteUser(String user) {
        userList.remove(user);
        userMap.forEach( (connection, nickname) -> {
            if (connection.isClosed()) {
                userMap.remove(connection);
            }
        });
    }

    public static String getUserNickBySocket(Socket connection) {
        return userMap.get(connection);
    }

    public static int UserCount() {
        return userList.size();
    }
}

class ClientHandler implements Runnable {
    private Socket incoming;

    ClientHandler(Socket incoming) {
        this.incoming = incoming;
    }

    public void run() {
        try {
            try {
                Scanner in = new Scanner(incoming.getInputStream());
                Server.SendToAll("Всего пользователей: " + Server.UserCount());
                while (in.hasNext()) {
                    Date date = new Date();
                    long time = date.getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    String line = in.nextLine();
                    line = formatter.format(time) + " " + line;
                    Server.SendToAll(line);
                }
            }
            finally {
                String exitUser = Server.getUserNickBySocket(incoming);
                Server.deleteUser(exitUser);
                Server.SendToAll(exitUser + " отключился. Всего пользователей: " + Server.UserCount());
                incoming.close();
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}