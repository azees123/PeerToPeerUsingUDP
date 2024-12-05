package GUI;

import Leecher.Leecher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This class will create the scene that will render the leecher and handle GUI functionality assisted by the Leecher class
 */
public class LeecherStage
{
    /*
        Associated with Labels
     */
    Label heading = new Label();
    Label HostAddress = new Label();
    Label HostPort = new Label();

    /*
        Associated with Input fields
     */
    TextField hostAddress = new TextField();
    TextField portNumber = new TextField();

    /*
        Associated with Buttons
     */
    Button Connect = new Button();


    /*
        Associated with ChatBox and ListView
     */
    VBox ChatBox = new VBox();
    ListView<String> stringListView = new ListView<>();
    public ObservableList<String> listOfFiles = FXCollections.observableArrayList(); // observable list of items
    public TextField command = new TextField(); // text field to receive commands
    VBox layerList = new VBox();
    public Button Send = new Button(); // button to send requests
    Button SwitchModes = new Button();

    /*
        Associated with stage
     */
    Stage stage;

    /*
        Associated with other class instances
     */
    Leecher leecherHelper;

    /**
     * The constructor accepts a javafx stage instance
     * @param stage is the stage where the scene will be rendered
     */
    public LeecherStage(Stage stage)
    {
        this.stage = stage;
        initializeNodes();
    }

    /**
     * This function initializes the javafx nodes to be rendered by the scene
     */
    private void initializeNodes()
    {
        /*
            Initialize the heading labels
         */
        heading.setText("Leecher Mode");
        heading.setUnderline(true);
        heading.setFont(new Font("Verdana", 30));

        /*
            Initialize the input fields
         */
        HostAddress.setText("Host Address: ");
        HostAddress.setFont(new Font("Arial", 15));
        HostPort.setText("Port Number: ");
        HostPort.setFont(new Font("Arial", 15));
        hostAddress.setMinHeight(35);
        hostAddress.setMinWidth(250);
        hostAddress.setPromptText("Enter Host Address");
        portNumber.setMinHeight(35);
        portNumber.setMinWidth(250);
        portNumber.setPromptText("Enter Port Number");

        // initialize buttons
        Connect.setText("Connect");
        Connect.setMinHeight(35);

        /*
         -------------------------------- Chat box --------------------------------------
         */
        command.setMinWidth(350);
        command.setMinHeight(35);
        command.setPromptText(" Command Syntax: LIST || FILE -space- index");

        Send.setText("Send"); // button to send a request
        Send.setMinHeight(35);

        SwitchModes.setText("Switch Modes"); //  button to switch between modes
        SwitchModes.setMinHeight(35);

        /*
            Initializing the ListView
         */
        stringListView.setItems(listOfFiles);
        stringListView.setMaxHeight(350);

        Label listLabel = new Label("List of Files");
        listLabel.setFont(Font.font("Verdana", 15));
        listLabel.setUnderline(true);

        layerList.getChildren().addAll(layerNodesHorizontally(true, 0, listLabel), stringListView);
        layerList.setSpacing(10);

        ChatBox.setStyle("-fx-background-color: lightgray"); // style the background
        ChatBox.setMinWidth(500);
        ChatBox.setMinHeight(350);
        ChatBox.setPadding(new Insets(10)); // set spacing between messages to 10
    }

    /**
     * This function layers the javafx nodes horizontally
     * @param horizontal boolean to indicate to center the node or not
     * @param padding_left how many pixels to pad the node left
     * @param nodes the nodes to be layered
     * @return returns an HBox with the nodes
     */
    private HBox layerNodesHorizontally(boolean horizontal, double padding_left, Node ... nodes)
    {
        HBox layout = new HBox();
        layout.getChildren().addAll(nodes);
        // center nodes if indicated true else don't center rather pad the node left
        if (horizontal)
        {
            layout.setAlignment(Pos.CENTER);
        }
        else
        {
            layout.setPadding(new Insets(0, 0, 0, padding_left));
        }
        layout.setSpacing(30);
        return layout;
    }

    /**
     * This function layers the javafx nodes vertically
     * @param nodes the javafx nodes to be layered
     * @return returns a VBox with the nodes layered vertically
     */
    private VBox layerNodesVertically(Node ... nodes)
    {
        VBox layout = new VBox();
        layout.getChildren().addAll(nodes);
        layout.setSpacing(20);
        return layout;
    }

    /**
     * This function creates a border pane to layer the nodes
     * @return return the borderpane
     */
    private BorderPane customLayout()
    {
       BorderPane layout = new BorderPane();
       /*
            A VBox for a message box
        */
       VBox vBox = new VBox();
       vBox.setMinWidth(500);
       vBox.setMinHeight(350);

       /*
            Scroll-pane for the box of messages
        */
       ScrollPane scrollPane = new ScrollPane(ChatBox);
       scrollPane.setPrefWidth(500);
       scrollPane.setPrefHeight(350);
       scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
       /*
            Labels and customization of the VBox
        */
       Label chatboxHeading = new Label("Chatbox");
       chatboxHeading.setFont(new Font("Arial", 15));
       chatboxHeading.setUnderline(true);
       vBox.setSpacing(10);
       vBox.getChildren().addAll(layerNodesHorizontally(true, 0, chatboxHeading), scrollPane);
       layout.setLeft(vBox);
       // layer list view
       layout.setRight(layerList);
       layout.setPadding(new Insets(20));
       return layout; // return the layout
    }

    /**
     * This function dynamically adds the messages to the VBox in the GUI
     * @param sender name of the character adding the message to the GUI
     * @param message the message to be added
     * @param isCurrentMode flags if the message comes from the current mode or another
     */
    public void addMessagesToGUI(String sender, String message, boolean isCurrentMode)
    {
        // create a label for the sender's name
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
        // a text node to hold the message
        Text messageNode = new Text(message);
        // creating a text-flow to contain the text
        TextFlow textFlow = new TextFlow(messageNode);
        textFlow.setPadding(new Insets(10));
        textFlow.setMaxWidth(480);

        // alternate styling based on sender's mode
        if(isCurrentMode)
        {
            textFlow.setStyle("-fx-background-color: lightblue; -fx-background-radius: 10px;");
            textFlow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT); // align text to the right
            senderLabel.setVisible(false); // hide label in current mode
        }
        else
        {
            textFlow.setStyle("-fx-background-color: lightgray; -fx-background-radius: 10px;");
            textFlow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT); // Align to the left
            senderLabel.setText("From: " + sender); // Set the sender's name
        }
        // contain both the sender label and the message
        VBox messageBox = new VBox();
        messageBox.getChildren().addAll(senderLabel, textFlow);

        // add the message container to the main Vbox
        ChatBox.getChildren().add(messageBox);
    }

    /**
     * This function creates a javafx with all the nodes
     * @return returns the scene
     */
    public Scene leecherScene()
    {
        return new Scene(layerNodesVertically(layerNodesHorizontally(true,0, heading), layerNodesHorizontally(false,20,  HostAddress,hostAddress),
                layerNodesHorizontally(false,20,  HostPort, portNumber), layerNodesHorizontally(false, 250, Connect),
                layerNodesVertically(customLayout(),
                        layerNodesHorizontally(false, 150, command, Send, layerNodesHorizontally(false,480, SwitchModes)))), 1200, 750);
    }

    /**
     * This function sets the event handlers for the buttons on the GUI
     * @param SeederScene the scene of the seeder
     */
    public void addButtonFunctionality(Scene SeederScene)
    {
        SwitchModes.setOnAction(e -> {
            stage.setScene(SeederScene);
        });

        Connect.setOnAction(e -> {
            try {
                leecherHelper =  new Leecher(new DatagramSocket(), hostAddress.getText(), Integer.parseInt(portNumber.getText()), this);
                leecherHelper.start(); // start thread
            } catch (SocketException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
