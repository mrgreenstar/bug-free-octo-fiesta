package com.company;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Interface extends Application {
    private String name;
    private TextField loginInput;
    private TextField ipInput;
    private TextField portInput;
    private Button loginBtn;
    private Label errorLabel;

    private int PORT;
    private String IP;

    public static void main(String args[]) {
        launch(args);
    }

    public void start(Stage MyStage) {
        MyStage.setTitle("Ввод ника");

        VBox loginNode = new VBox(10);
        loginNode.setAlignment(Pos.CENTER);

        // HBox для полей IP и Порт
        HBox testNode = new HBox(10);
        testNode.setAlignment(Pos.CENTER);

        Scene loginScene = new Scene(loginNode, 300, 150);
        MyStage.setScene(loginScene);

        errorLabel = new Label();

        loginInput = new TextField();
        loginInput.setPromptText("Введите ник");
        loginInput.setMaxWidth(260);
        loginInput.setMaxHeight(30);

        ipInput = new TextField();
        ipInput.setPromptText("Ip");
        ipInput.setText("localhost");
        ipInput.setMaxWidth(170);
        ipInput.setMaxHeight(30);

        portInput = new TextField();
        portInput.setPromptText("Порт");
        portInput.setText("8189");
        portInput.setMaxWidth(80);
        portInput.setMaxHeight(30);

        loginBtn = new Button("Войти");
        loginBtn.setOnAction( (ae) -> {
            if (loginInput.getText().trim().equals("")) {
                name = "Anonymous";
            }
            else {
                name = loginInput.getText().trim();
            }

            IP = ipInput.getText();
            try {
                PORT = Integer.parseInt(portInput.getText());
                MyStage.close();
                new MainWindow(IP, PORT, name);
            }
            catch (NumberFormatException exc) {
                errorLabel.setText("Ошибка при вводе порта.");
                errorLabel.setTextFill(Color.RED);
            }
        });

        testNode.getChildren().addAll(ipInput, portInput);
        loginNode.getChildren().addAll(loginInput, testNode, loginBtn, errorLabel);
        MyStage.show();
    }
}

class MainWindow {
    private Stage MainWindow;
    private BorderPane rootNode;
    private Scene MainScene;
    private String name;

    private int SERVER_PORT;
    private String SERVER_IP;
    private Socket connection;

    MainWindow(String SERVER_IP, int SERVER_PORT, String name) {
        MainWindow = new Stage();
        rootNode = new BorderPane();
        MainScene = new Scene(rootNode);
        MainWindow.setScene(MainScene);
        this.name = name;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;

        showMainWindow();
    }

    public void showMainWindow() {
        Scanner inMessage;
        PrintWriter outMessage;

        // Интерфейс
        MainWindow.setTitle("Тестовый интерфейс");
        // HBox для ввода сообщения и кнопки отправки
        AnchorPane bottomNode = new AnchorPane();

        TextArea messages = new TextArea("");
        messages.setWrapText(true);
        messages.setEditable(false);

        TextArea input = new TextArea();
        input.setWrapText(true);
        input.setMaxHeight(50);
        input.setPromptText("Сообщение");

        Button send = new Button("Отправить");
        try {
            connection = new Socket(SERVER_IP, SERVER_PORT);
            inMessage = new Scanner(connection.getInputStream());
            outMessage = new PrintWriter(connection.getOutputStream(), true);
            outMessage.println(name);

            MainWindow.setTitle(name + "[]");
            send.setOnAction((ae) -> {
                outMessage.println(name + ": " + input.getText());
                input.clear();
            });

            Thread serverListener = new Thread() {
                @Override
                public void run() {
                        try {
                            while (!connection.isClosed()) {
                                if (inMessage.hasNextLine())
                                    messages.setText(messages.getText() + "\n" + inMessage.nextLine());
                            }
                        } catch (Exception exc) {
                            System.out.println("Ошибка при ожидании сервера");
                        } finally {
                            outMessage.close();
                            inMessage.close();
                        }
                    }
            };
            serverListener.start();

            MainWindow.setOnCloseRequest( (ae) -> {
                try {
                    connection.close();
                }
                catch (IOException exc) {
                    System.out.println("Ошибка при закрытии сокета");
                }
            });
        }
        catch (IOException exc) {
            messages.setText("Ошибка при подключении к серверу.");
        }

        AnchorPane.setBottomAnchor(input, 10.0);
        AnchorPane.setLeftAnchor(input, 10.0);
        AnchorPane.setRightAnchor(input, 150.0);

        AnchorPane.setBottomAnchor(send, 10.0);
        AnchorPane.setRightAnchor(send, 10.0);
        bottomNode.getChildren().addAll(input, send);

        rootNode.setBottom(bottomNode);
        rootNode.setCenter(messages);
        MainWindow.show();
    }
}
