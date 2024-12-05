package Leecher;

import GUI.LeecherStage;
import javafx.application.Platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the functionality of being a leecher
 */
public class Leecher extends Thread
{
    /*
        Associated with sockets and networking
     */
    DatagramSocket socket;
    int port;
    InetAddress address;
    LeecherStage leecherStage;
    ArrayList<String> files;

    /**
     * This constructor takes in a datagram socket, host , port number and instance of the class that handles the UI of the Leecher
     * @param socket the datagram socket
     * @param host the address to send packets to
     * @param port the port number associated with then address
     * @param leecherStage the instance of LeecherStage classs
     */
    public Leecher(DatagramSocket socket, String host, int port, LeecherStage leecherStage)
    {
        try
        {
            this.socket = socket;
            this.port = port;
            address = InetAddress.getByName(host);
            this.leecherStage = leecherStage;
        }
        catch (IOException e)
        {
            System.out.println("LeecherHelper Error: " + e.getMessage());
        }

        // sending initialization message to seeder
        initiateCommunication(socket, host, this.port);
        // send commands
        leecherStage.Send.setOnAction(e -> {
            sendCommands();
        });
    }

    /**
     * This function handles the responses from a peer
     * @param received is the message received
     */
    public void receiveResponse(String received)
    {
        if(received.contains("Connected"))
        {
            /*
                Add the message to the gui
             */
            Platform.runLater(() -> {
                leecherStage.addMessagesToGUI("seeder", received, false);
            });
            System.out.println("Communication with seeder successfully initiated");
        }
        if(received.contains("FILE"))
        {
            /*
                Add the message to the gui
             */
            Platform.runLater(() -> {
                leecherStage.addMessagesToGUI("seeder", received, false);
            });

            String[] responseData = received.split(" ");
            // receive the file and save it
            saveFile(responseData[1]);
        }
        else if(received.contains("LIST"))
        {
            /*
                Add the message to the gui
             */
            Platform.runLater(() -> {
                leecherStage.addMessagesToGUI("seeder", received, false);
            });

            /*
                 Now receive the list and save it
             */
            saveListOFFiles();
        }
    }

    /**
     * This function handles the functionality of receiving a file over the network and saving it
     * @param fileId the ID of the file
     */
    private void saveFile(String fileId)
    {
        // set the directory
        File fileDirectory = new File("data/leecherData");
        String fileName = files.get(Integer.parseInt(fileId.substring(0,1) ) - 1);
        // construct file path
        File newFile = new File(fileDirectory, fileName);
        try(FileOutputStream fos = new FileOutputStream(newFile))
        {
            // prepare to receive file
            byte[] buf  = new byte[1024];
            DatagramPacket packet;
            //socket.setSoTimeout(15000); // setting timeout to receive the file packets

            while (true)
            {
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                // Check if the packet marks the end of the file
                if (received.contains("SENT")) {  // Assuming empty packet signals end
                    Platform.runLater(() ->{
                        leecherStage.addMessagesToGUI("seeder", received, false);
                    });
                    byte[] bytes = ("I received it, thanks.").getBytes();
                    DatagramPacket packet2 = new DatagramPacket(bytes, bytes.length, address, port);
                    socket.send(packet2);
                    Platform.runLater(() -> {
                        leecherStage.addMessagesToGUI("seeder", "I received it, thanks", true);
                    });
                    break;
                }

                fos.write(packet.getData(), 0, packet.getLength());
                fos.flush();
                System.out.println("File saved to " + newFile.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            System.out.println("LeecherHelper saveFile Error: " + e.getMessage());
        }
    }

    /**
     * This function saves the list of files it receives from the peer and adds it to the GUI
     */
    private void saveListOFFiles()
    {
        try
        {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(packet.getData());
            Platform.runLater(() -> {
                leecherStage.addMessagesToGUI("seeder", received, false); // add message to gui
                int index = 1;
                files = getListToPutItOnListView(received); // store list of files
                for(String file : getListToPutItOnListView(received))
                {
                    leecherStage.listOfFiles.add(index + ". " +file);
                    index++;
                }
            });
        }
        catch (IOException e)
        {
            System.out.println("LeecherHelper saveListOFFiles Error: " + e.getMessage());
        }

    }

    /**
     * Overriding the run function from the thread class for multi-threading
     */
    @Override
    public void run()
    {
        System.out.println("Thread for messages on LeecherHelper now running");
       while(true)
       {
           byte[] receiveBytes = new byte[1024];
           DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
           try
           {
               socket.receive(response);
           }
           catch (IOException e)
           {
               System.out.println("LeecherHelper Error: " + e.getMessage());
           }
           // create a string
           String received = new String(response.getData());
           // handle responses
           receiveResponse(received);
       }
    }

    /**
     * This function is used to initiate a communication with the seeder
     * @param socket takes a datagram socket
     * @param Host the host address
     * @param Port the port number
     */
    private void initiateCommunication(DatagramSocket socket, String Host, int Port)
    {
        // first initiate connection
        try
        {
            byte[] buf = ("Init").getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(Host), Port);
            socket.send(packet);
        }
        catch (IOException e)
        {
            System.out.println("Leecher Initializing connection Error: " + e.getMessage());
        }
    }

    /**
     * This function is used to send commands to the seeder
     */
    private void sendCommands()
    {
        try
        {
            byte[] buf = leecherStage.command.getText().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            leecherStage.command.clear(); // refresh text field
            Platform.runLater(() -> {
                leecherStage.addMessagesToGUI("seeder", leecherStage.command.getText(), true);
            });
        }
        catch (IOException e)
        {
            System.out.println("LeecherHelper sendCommands Error: " + e.getMessage());
        }
    }

    /**
     * This is a function to process the list of files received
     * @param listOfFiles the list of files
     * @return returns an arraylist with all the files
     */
    public ArrayList<String> getListToPutItOnListView(String listOfFiles)
    {
        // first trim the string
        String whitespaceRemoved = listOfFiles.trim();
        // Remove brackets
        String trimmedString = whitespaceRemoved.substring(1, whitespaceRemoved.length() - 1);
        // split string by commas
        List<String> items = Arrays.asList(trimmedString.split(", "));
        // return the new arrayList
        return new ArrayList<>(items);
    }
}
