package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * This class will serve as the main entry of the GUI with buttons to select a mode
 */
public class MainStage
{
    /*
         Associated with labels and texts
     */
    Label heading = new Label();
    Text statement = new Text();

    /*
        Associated with buttons
     */
    Button Seeder = new Button();
    Button Leecher = new Button();

    /*
        Associated with javafx stage and scenes
     */
    Stage stage;

    /**
     * This constructor creates an instance of the MainStage class
     * @param stage takes in a stage where all the scenes will be rendered
     * @param leecher the scene for the leecher
     * @param seeder the scene for the seeder
     */
    public MainStage(Stage stage, Scene leecher, Scene seeder)
    {
        this.stage = stage;
        initialize_Nodes();
        Functionalities(leecher, seeder); // set button functionalities
    }

    /**
     * This function initializes the javafx nodes
     */
    private void initialize_Nodes()
    {
        /*
            Setting the labels and messages
         */
        heading.setText("Peer - To - Peer file sharing with UDP");
        heading.setFont(new Font("Arial", 50));
        heading.setUnderline(true);
        statement.setText(
                "This is a UDP-based peer - to -peer file sharing system which consists of a Main that " +
                        "consists of a Main that can either send files in Seeder mode or receive files in Leecher mode.\n" +
                        "Below select a mode to continue"
        );
        statement.setTextAlignment(TextAlignment.CENTER);
        statement.setFont(new Font("Arial", 15));
        statement.setWrappingWidth(800);

        /*
            Initializing the buttons
         */
        Seeder.setText("Seeder");
        Seeder.setMinHeight(40);

        Leecher.setText("Leecher");
        Leecher.setMinHeight(40);
    }

    /**
     * This function layers nodes horizontally using the javafx HBox
     * @param nodes the javafx nodes to be layered
     * @return returns the HBox with the nodes layered
     */
    private HBox layerNodesHorizontally(Node ... nodes)
    {
        HBox hbox = new HBox();
        hbox.getChildren().addAll(nodes);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }

    /**
     * This function layers the nodes vertically using the javafx VBox
     * @param nodes the javafx nodes to be layered
     * @return returns the VBox with nodes layered
     */
    private VBox layerNodesVertically(Node ... nodes)
    {
        VBox vbox = new VBox();
        vbox.getChildren().addAll(nodes);
        vbox.setSpacing(90);
        vbox.setPadding(new Insets(50,0,0,0));
        return vbox;
    }

    /**
     * This function creates a scene with all the nodes associated with MainStage class
     * @return returns the scene with all the nodes
     */
    public Scene MainStageNodes()
    {
        return new Scene(layerNodesVertically(layerNodesHorizontally(heading), layerNodesHorizontally(statement), layerNodesHorizontally(Seeder, Leecher)), 1200, 700);
    }

    /**
     * This function sets the event handlers for the buttons on the GUI
     * @param leecher scene for the leecher
     * @param seeder scene for the seeder
     */
    private void Functionalities(Scene leecher, Scene seeder)
    {
        Leecher.setOnAction(e -> {

            stage.setScene(leecher);
        });

        Seeder.setOnAction(e -> {
            stage.setScene(seeder);
        });
    }
}
