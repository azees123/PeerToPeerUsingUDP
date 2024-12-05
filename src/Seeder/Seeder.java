package Seeder;

import GUI.SeederStage;
import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class handles all the functionalities of the seeder
 */
public class Seeder extends Thread
{
    /*
        Associated with sockets and networking
     */
   DatagramSocket socket;
   String messageFromLeecher;
   byte[] bufferIncoming = new byte[1024];
   SeederStage seederStage;

    /**
     * This constructor accepts a socket to send and receive data and an instance of the seeder GUI class handler
     * @param socket A datagram socket
     * @param seederStage an instance of the Seeder GUI instance class
     */
   public Seeder(DatagramSocket socket, SeederStage seederStage)
   {
      this.socket = socket;
      this.seederStage = seederStage;
   }

    /**
     * Overriding the run function from the thread class for multithreading
     */
   @Override
   public void run()
   {
       System.out.println("SeederHelper started");
       while (true)
       {
           DatagramPacket packet = new DatagramPacket(bufferIncoming, bufferIncoming.length);
           try
           {
               socket.receive(packet); // receive the packet
           }
           catch (IOException e)
           {
               System.out.println("SeederHelper received an exception");
           }
           // create a string to hold the message
           String message = new String(packet.getData());
           // handle requests
           Messages(message, packet.getAddress(), packet.getPort());
       }
   }

    /**
     * This function handles the communication messages from the leecher
     * @param message the message from the leecher packet
     * @param address  address where the message came from
     * @param port the port number it came from
     */
   public void Messages(String message, InetAddress address, int port)
   {
       if(message.contains("Init"))
       {
           /*
                Add message to gui
            */
           seederStage.addMessagesToGUI("Leecher", message, false);

           System.out.println("Leecher is initiating connection");
           // sending a response
           respondWithMessage("Connected", address, port);
       }
       else if(message.contains("LIST"))
       {
           /*
                Add message to gui
            */
           seederStage.addMessagesToGUI("Leecher", message, false);

           // first send response message
           respondWithMessage("LIST - Here's the list of files: ", address, port);
           // store message
           messageFromLeecher = message;
           try
           {
               // respond to leecher by sending the list
               String files = getListOfFiles().toString();
               byte[] sendData = files.getBytes();
               // create the packet
               DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);
               // send the packet
               socket.send(packet);
               System.out.println("List of files sent");
           }
           catch(IOException e)
           {
               System.out.println("Error sending list of files: " + e.getMessage());
           }
       }
       else if(message.contains("FILE"))
       {
           /*
                Add message to gui
            */
           seederStage.addMessagesToGUI("Leecher", message, false);

           // get message
           messageFromLeecher = message;
           //
           String[] requestedFile = message.split(" ");

           // send response message first
           respondWithMessage("FILE " + requestedFile[1], address, port);
           // send the actual file
           try
           {
               sendFile(Integer.parseInt(requestedFile[1].substring(0, 1)), address, port);
           }
           catch(IOException e)
           {
               System.out.println("Error sending file: " + e.getMessage());
           }

       }
       else
       {
           System.out.println("Leecher is sending messages the seeder is not expecting: " + message);
       }
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
     * This function is the middle man in sending the  requested file
     * @param index takes the index of the file wanted
     * @param address the address where to send the file
     * @param port the port number associated with that address
     * @throws IOException the function can throw an IOExecption
     */
   private void sendFile(int index, InetAddress address, int port) throws IOException
   {
       // look for file
       File directory = new File("data/seederData");
       if(directory.isDirectory())
       {
           if(directory.exists())
           {
               try {
                   File theFile = Objects.requireNonNull(directory.listFiles())[index - 1];
                   fileSender(theFile, address, port);
               }
               catch (IndexOutOfBoundsException e) {
                   System.out.println("There are no files in the folder");
               }
           }
           else
           {
               System.out.println("Directory does not exist");
           }
       }
       else
       {
           System.out.println("Folder is not a directory");
       }
   }

    /**
     * This function handles the sending of the file as a packet
     * @param file the file to be sent
     * @param address the address
     * @param port the port number
     */
   private void fileSender(File file, InetAddress address, int port)
   {
       try(FileInputStream fin = new FileInputStream(file))
       {
           byte[] buffer = new byte[1024];
           int byteRead;
           while((byteRead = fin.read(buffer)) > 0)
           {
               DatagramPacket packet = new DatagramPacket(buffer, byteRead, address, port);
               socket.send(packet); // send the file
               System.out.println("File sent " + file.getName());
           }
           /*
                Send end of file signal
            */
           byte[] end = ("SENT FILE TO YOU").getBytes();
           DatagramPacket packet = new DatagramPacket(end, end.length, address, port);
           socket.send(packet);
           Platform.runLater(() -> {
               seederStage.addMessagesToGUI("seeder", "SENT FILE TO YOU", true);
           });

           /*
                receive signal that it was received
            */
           byte[] receive = new byte[1024];
           DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
           socket.receive(receivePacket);
           String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
           Platform.runLater(() -> {
               seederStage.addMessagesToGUI("leecher", message, false);
           });
       }
       catch(IOException e)
       {
           System.out.println("File not found");
       }
   }

    /**
     * THis function handles the functionality of sending responses to the leecher
     * @param message the message to respond with
     * @param address the address
     * @param port the port number
     */
   private void respondWithMessage(String message, InetAddress address, int port)
   {
       try
       {
           byte[] sendData = message.getBytes();
           DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);
           socket.send(packet);
       }
       catch (IOException e)
       {
           System.out.println("Error sending response Message: " + e.getMessage());
       }
   }

}
