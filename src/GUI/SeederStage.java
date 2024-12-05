package GUI;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * This class handles the seeder functionalities
 */
public class SeederStage
{
    Label heading = new Label();

    /*
        Related to  the listView box
     */
    ListView<String> listViewOfFiles = new ListView<>();
    Button refreshButton = new Button("List files");
    ObservableList<String> listOfFiles = FXCollections.observableArrayList();

    /*
        Related to the chat box
     */
    VBox chatBox = new VBox();
    TextField responseField = new TextField();
    Button sendButton = new Button("Send");

    /*
        Related to files
     */
    FileChooser fileChooser = new FileChooser();
    Button addFile = new Button("Upload file");
    File uploadedFIle;
    ArrayList<String> arrayListOfFiles;

    /*
        Related to scenes and stages
     */
    Button switchScene = new Button("Switch Mode");
    Stage stage;

    /**
     * This constructors takes in a stage instance where the scene will be rendered
     * @param stage the stage instance
     */
    public SeederStage(Stage stage)
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
            customize heading
         */
        heading.setText("Seeder Mode");
        heading.setFont(new Font("Arial", 30));
        heading.setUnderline(true);

        /*
            customize list view
         */
        listViewOfFiles.setMaxHeight(350);
        listViewOfFiles.setMinWidth(350);
        listViewOfFiles.setEditable(false);
        listViewOfFiles.setItems(listOfFiles);

        /*
            customize chatBox
         */
        chatBox.setMinWidth(500);
        chatBox.setMinHeight(350);
        chatBox.setStyle("-fx-background-color: lightgray");

        /*
            customize input field
         */
        responseField.setMinWidth(300);
        responseField.setMinHeight(40);
        responseField.setPromptText("Enter File Name to send");

        /*
            customize button
         */
        sendButton.setMinHeight(45);
        sendButton.setMaxWidth(150);

        /*
            set file chooser
         */
        fileChooser.setInitialDirectory(new File("data"));
        addFile.setMaxWidth(50);
        addFile.setMinWidth(100);
        refreshButton.setMinWidth(100);

        /*
            et list of files on director
         */
        arrayListOfFiles = getListOfFiles();

    }

    /**
     * This function layers the javafx nodes horizontally
     * @param center boolean to indicate to center the node or not
     * @param padding how many pixels to pad the node left
     * @param node the nodes to be layered
     * @return returns an HBox with the nodes
     */
    private HBox layerHorizontally(boolean center, double padding,  Node ... node)
    {
        HBox hbox = new HBox();
        hbox.getChildren().addAll(node);
        if(center)
        {
            hbox.setAlignment(Pos.CENTER);
        }
        else
        {
            hbox.setPadding(new Insets(0, 0, 0, padding));
        }
        hbox.setSpacing(30);
        return hbox;
    }

    /**
     * This function layers the javafx nodes vertically
     * @param node the javafx nodes to be layered
     * @return returns a VBox with the nodes layered vertically
     */
    private VBox layerVertically(Node ... node)
    {
        VBox vbox = new VBox();
        vbox.getChildren().addAll(node);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    /**
     * This function creates a border pane to layer the nodes
     * @return return the borderpane
     */
    private BorderPane customLayering()
    {
        BorderPane borderPane = new BorderPane();
        /*
            layer chatbox
         */
        VBox vbox = new VBox();
        vbox.setMinHeight(350);
        vbox.setMinWidth(500);
        vbox.setSpacing(10);

        /*
            Scroll pane for the message box
         */
        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        /*
            Labels
         */
        Label lbl = new Label("ChatBox");
        lbl.setFont(new Font("Arial", 15));
        lbl.setUnderline(true);
        vbox.getChildren().addAll(layerHorizontally(true,0,lbl), scrollPane);

        /*
            add chatbox
         */
        borderPane.setLeft(vbox);

        /*
            layer list view
         */
        Label list = new Label("List of Files");
        list.setFont(new Font("Arial", 15));
        list.setUnderline(true);
        borderPane.setRight(layerVertically(layerHorizontally(true, 0, list), listViewOfFiles, layerHorizontally(true, 0,refreshButton, addFile)));
        borderPane.setPadding(new Insets(10));

        return borderPane;
    }
    /**
     * This function creates a javafx with all the nodes
     * @return returns the scene
     */
    public Scene seederScene()
    {
        HBox hBox = new HBox(switchScene);
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        hBox.setPadding(new Insets(160,0 ,0 ,0 ));
        return new Scene(
                layerVertically(layerHorizontally(true, 20, heading), customLayering(),
                        layerHorizontally(false,50,responseField, sendButton), hBox),
                1200, 700
        );
    }

    /**
     * This function sets the event handlers for the buttons on the GUI
     * @param LeecherScene the scene of the leecher
     */
    public void setButtonFunctionalities(Scene LeecherScene)
    {
        addFile.setOnAction(e -> {
            uploadedFIle = fileChooser.showOpenDialog(stage);
            arrayListOfFiles.add(uploadedFIle.getName()); // add file to arraylist
            addListTOView(arrayListOfFiles); // refresh the listView
            saveFileToSeederFolder(uploadedFIle); // save uploaded file to seeder data folder
        });

        switchScene.setOnAction(e -> {
            stage.setScene(LeecherScene);
        });

        refreshButton.setOnAction(e -> {
            addListTOView(arrayListOfFiles);
        });
    }

    /**
     * This function dynamically adds the messages to the VBox in the GUI
     * @param nameOfSender name of the character adding the message to the GUI
     * @param message the message to be added
     * @param isCurrentMode flags if the message comes from the current mode or another
     */
    public void addMessagesToGUI(String nameOfSender, String message, boolean isCurrentMode)
    {
        // create a label for the sender
        Label senderLabel = new Label(nameOfSender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");

        // making a text node to hold message
        Text messageText = new Text(message);
        // creating Text flow to contain messages
        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(10));
        textFlow.setMaxWidth(480);

        // styling text-flow according to a mode
        if(isCurrentMode)
        {
            textFlow.setStyle("-fx-background-color: lightblue; -fx-background-radius: 10px;");
            textFlow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT); // Align to the right
            senderLabel.setVisible(false); // Hide the label for the current user
        } else {
            textFlow.setStyle("-fx-background-color: lightgray; -fx-background-radius: 10px;");
            textFlow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT); // Align to the left
            senderLabel.setText("From: " + nameOfSender); // Set the sender's name
        }

        // this container will hold the messages
        VBox messageBox = new VBox();
        messageBox.getChildren().addAll(senderLabel, textFlow);

        // add the box to the original box
        chatBox.getChildren().add(messageBox);
    }

    /**
     * This function  adds the list of files onto the LIstView on the GUI
     * @param arrayList array-list of name of files available
     */
    public void addListTOView(ArrayList<String> arrayList)
    {

        // convert arraylist to observable list
        listOfFiles.clear(); // refresh list
        listOfFiles.addAll(arrayList);
    }

    /**
     * This function reads through the data folder for files available and creates a list of all those files
     * @return returns that list
     */
    private ArrayList<String> getListOfFiles()
    {
        // go to file directory
        File file = new File("data/seederData");
        // a list of files
        ArrayList<String> listOfFiles = new ArrayList<>();

        if(file.isDirectory())
        {
            // get list of files
            File[] files = file.listFiles();
            // add files to list
            if(files != null)
            {
                for(File f : files)
                {
                    // add each file to the list
                    listOfFiles.add(f.getName());
                }
            }
            else
            {
                System.out.println("There are no files in the folder");
            }
        }
        else
        {
            System.out.println("File is not a directory");
        }
        return listOfFiles;
    }

    /**
     * THis function saves a file to the seeder data folder
     * @param file the file to be saved
     */
    private void saveFileToSeederFolder(File file)
    {
        // defining a file path to seeder folder
        File destinationSeederFolder =  new File("data/seederData");
        // defining destination file path
        File destinationSeederFile = new File(destinationSeederFolder, file.getName());
        // now copy uploaded file to this path
        try
        {
            Files.copy(file.toPath(), destinationSeederFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Failed to save  Uploaded file");
        }
    }
}
