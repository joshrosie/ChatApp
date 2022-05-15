import java.io.*;
import java.lang.Math;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * udpServer is the server that udpClients communicate with and through. Since
 * this is the case, it also checks message integrity by comparing hash codes,
 * in the same way that the receiver thread class does. This is to ensure
 * message integrity when the sender communicates with the server. The server
 * broadcasts all messages that it is sent to the clients connected to the
 * server, except to the same client who sent the message. This creates the
 * group chat feature. To allow for this functionality, the server keeps track
 * of all currently connected users though the use of clientObjs – note, this is
 * different to the udpClient class.
 * 
 * @author FSHJAR002 RSNJOS005
 * @since 2021-03-29
 */

public class udpServer extends Thread {

  private DatagramSocket socket;
  private boolean running, transmitError;
  private InetAddress addressServer;
  private byte[] buf;
  private int serverPort;
  private ArrayList<clientObj> clientArrayList;
  private ArrayList<String> allowedUsers;
  private Scanner fileIn;

  private FileWriter chatFileOut;
  private Date currentDate;
  private final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  /**
   * Construction of a udpServer thread requires an IP address and a port number
   * The server is bound to these attributes and it can be either local or WAN.
   * 
   * @param aServer    The address of the server
   * @param port       The port of the server
   * @param forceError Whether to force a message corruption to occur (with a
   *                   probabilty of 10%)
   */

  public udpServer(String aServer, int port, boolean forceError) { // create a new server that is bound to a partiuclar
                                                                   // IP address and port
    serverPort = port;
    running = true;
    clientArrayList = new ArrayList<clientObj>(); // List of client objects
    allowedUsers = new ArrayList<String>(); // List of users allowed to connect
    try {
      fileIn = new Scanner(new FileReader("whitelist.txt")); // The list of users who are allowed to connect
    } catch (FileNotFoundException e) {
      System.out.println("File not found error: " + e);
    }

    while (fileIn.hasNextLine()) {
      allowedUsers.add(fileIn.nextLine().trim()); // Add the allowed users to the list
    }
    fileIn.close();
    transmitError = forceError;
    try {
      addressServer = InetAddress.getByName(aServer);
      socket = new DatagramSocket(serverPort, addressServer); // create a new socket for the server.
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  /**
   * Server thread starts running. Within this run() method is where all the
   * processing of incoming and outcoming packets takes place.
   * 
   */
  @Override
  public void run() { // server thread starts running.
    boolean terminating = false;
    try {
      System.out.println("-- Running Server at " + InetAddress.getLocalHost() + "--"); // Print the address of where the
                                                                                       // server is running.
    } catch (IOException e) {
      System.out.println(e);
    }

    while (running) { // Loops until running is no longer true. This can happen if the server is
                      // forced or asked to be shutdown.
      buf = new byte[1024]; // Create buffer with size 1024 bytes.

      DatagramPacket packet = new DatagramPacket(buf, buf.length);

      try {
        socket.receive(packet); // Server trys to receive a packet on the socket created for the server.
      } catch (IOException e) {
        System.out.println(e);
      }

      InetAddress address = packet.getAddress(); // Address of where the message has come from is saved.

      int port = packet.getPort(); // Port of where the message has come from is saved.

      String currentUser = returnClientUserName(address, port); // Name of the user from which the message was sent is
                                                                // saved.

      String received = new String(packet.getData(), 0, packet.getLength()).trim(); // Get the buffer that was sent with
                                                                                    // the packet -
      // - this includes the hashcode for corruption checking.

      int y = received.lastIndexOf("@"); // Find the index of the '@' sign, this is used to delimit -
      // - the actual message from the appended hashcode.

      String msg = received.substring(0, y - 1); // Store the actual message sent with the packet.

      int iHash = Integer.valueOf(received.substring(y + 1, received.length())); // Generate a new hashcode from the
                                                                                 // message sent with the package.

      System.out.println("Message from " + currentUser + "@" + packet.getAddress().getHostAddress() + ": " + msg);
      // Print the user's data and their message - this is printed on the server's
      // terminal.

      if (transmitError)
        msg = transmissionError(msg); // Call the transmissionError method.

      if (iHash == msg.hashCode()) { // i.e. no corruption in message
        if (received.contains("connect-User@")) { // If the user is connecting for the first time and trying to add
                                                  // their username.
          int x = received.indexOf("@");
          int z = received.lastIndexOf("@");
          String username = received.substring(x + 1, z - 1);
          if (allowedUsers.contains(username)) {
            manageClientBase(username, address, port); // Call the manageClientBase method to create a new client object
                                                       // (if the user doesn't already exist)
            msg = "Current users in chat: ";
            for (int i = 0; i < clientArrayList.size(); i++)
              msg += clientArrayList.get(i).getUsername() + " ";

            sendMessage("connected@" + username, address, port);
            sendMessage(msg, address, port);
            broadCastMessage("(" + username + ") has entered the chat.", username, false);
          } else {
            sendMessage("You are not on the server whitelist. Please re-enter a valid username:", address, port);
          }

          continue;
        }
        if (!(allowedUsers.contains(currentUser))) // This prevents non-allowed users to send any messages.
          continue;
        if (received.contains("@exit@")) {
          System.out.println("[" + currentUser + "] has disconnected.");
          msg = "has disconnected.";
          for (int i = 0; i < clientArrayList.size(); i++) {
            if (clientArrayList.get(i).getUsername().equals(currentUser))
              clientArrayList.remove(i);
          }
        }

        if (received.contains("@history@")) { // Print chat history
          sendChatHistory(currentUser);
          continue;
        }

        if (received.contains("@shutdown@")) { // i.e. if a client has told the server to shutdown.
          running = false;
          System.out.println("Server shut down by user: " + currentUser);
          terminating = true;
          msg = "@shutdown@ by user: " + currentUser;
        } else
          msg = "[" + currentUser + "] " + msg; // Any message sent by the client that is not '@end'. Prepend the user's
                                                // message with their username.
        printMessageToChatLog(msg); // Print the message to the chat history file

        broadCastMessage(msg, currentUser, terminating);
      } else { // message corrupted i.e. the hashcode that was sent to the server along
        // with the message does not match the hashcode that was generated at the
        // server.

        msg = "@Corrupted, please resend message.@";
        buf = msg.getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        try {
          socket.send(packet); // Send message to client saying that the message was corrupted.
        } catch (IOException e) {
          System.out.println(e);
        }
      }
    }
    socket.close(); // Close the server socket.
  }

  /**
   * Populates the clientArrayList with clients everytime a new client connects. A
   * new user is not added if the user already exists.
   * 
   * @param username The username of the prospective client
   * @param address  The IP address of the prospective client
   * @param port     The port of the prospective client
   */

  private void manageClientBase(String username, InetAddress address, int port) {
    boolean clientPresent = false; // Flag to check for the pre-existence of a user in the client base (i.e.
                                   // contained in ClientArrayList).

    for (int i = 0; i < clientArrayList.size(); i++) { // Loop through the list and if the client already exists, set
                                                       // flag to true.
      if (clientArrayList.get(i).getUsername() == username)
        clientPresent = true;
    }

    if (!(clientPresent))
      clientArrayList.add(new clientObj(username, address, port)); // If the client does not yet exist, create a new
                                                                   // clientObj and add it to the list.
  }

  /**
   * Everytime a message is sent to the server, it is printed to the chat history
   * log file. In addition to who sent the message and the messsage itself, a
   * current date + time stamp is preppended.
   * 
   * @param msg The Message to be written
   */

  private void printMessageToChatLog(String msg) {
    currentDate = new Date(); // current date
    try {
      chatFileOut = new FileWriter("chat_history.txt", true);
      chatFileOut.write("(" + FORMATTER.format(currentDate) + ") " + msg + "\n");
      chatFileOut.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found error: " + e);
    } catch (IOException e) {
      System.out.println("I/O error: " + e);
    }
  }

  /**
   * This method sends the chat history to the user who requests it. The chat
   * history is read in from the chat_history log file.
   * 
   * @param username The username of the client to send the chat history to
   */
  private void sendChatHistory(String username) {
    String line = "";
    int clientIndex = -1;
    try {
      fileIn = new Scanner(new FileReader("chat_history.txt"));
    } catch (FileNotFoundException e) {
      System.out.println("File not found error: " + e);
    }

    for (int i = 0; i < clientArrayList.size(); i++) {
      if (clientArrayList.get(i).getUsername().equals(username)) {
        clientIndex = i;
        break;
      }
    }

    while (fileIn.hasNextLine()) {
      line = fileIn.nextLine();
      sendMessage(line, clientArrayList.get(clientIndex).getInetAddress(), clientArrayList.get(clientIndex).getPort());
    }

    sendMessage("History finished", clientArrayList.get(clientIndex).getInetAddress(),
        clientArrayList.get(clientIndex).getPort());
    fileIn.close();
  }

  /**
   * This is a basic function to send a message on the DatagramSocket. This is
   * done through the DatagramSocket.send() method.
   * 
   * @param msg     The message to be sent
   * @param address The IP address to which the message must be sent to
   * @param port    The port to which the message must be sent to
   */

  private void sendMessage(String msg, InetAddress address, int port) {
    msg = senderThread.buildMessageChecksum(msg);
    buf = msg.getBytes();
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
    try {
      socket.send(packet); // Send line of chat history
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /**
   * This auxillary method returns the username of a client with the specified
   * address and port
   * 
   * @param address The address at which the client resides
   * @param port    The port at which the client resides
   * @return The client's username
   */

  private String returnClientUserName(InetAddress address, int port) {
    String sAddress = address.getHostAddress();
    for (int i = 0; i < clientArrayList.size(); i++) {
      if ((clientArrayList.get(i).getStringAddress().equals(sAddress)) && (clientArrayList.get(i).getPort() == port))
        return clientArrayList.get(i).getUsername();
      // Loop through the list and if their exists a client with the same IP and port,
      // return that client's user name
    }
    return "";
  }

  /**
   * This is a basic implementation of a broadcast technique. This is achieved by
   * looping through the whole clientArrayList and sending the message to all
   * connected clients. The caveat to this is that the client who sent the message
   * will not be sent their own message. In the event that the server is shutting
   * down, all users (including the client who sent the @shutdown@ message) will
   * be notified.
   * 
   * @param msg       The message to be sent
   * @param username  The username of the client who sent the original message
   *                  (this is passed so that this client is NOT sent their own
   *                  message)
   * @param terminate Whether the server is shutting down.
   */

  private void broadCastMessage(String msg, String username, boolean terminate) {

    for (int i = 0; i < clientArrayList.size(); i++) { // Loop through the Client List and send the message to every
                                                       // client, besides the client that sent the message.
      // This allows for group chat functionality.

      if (!(clientArrayList.get(i).getUsername().equals(username)) || terminate) {
        sendMessage(msg, clientArrayList.get(i).getInetAddress(), clientArrayList.get(i).getPort());
      }
    }
  }

  /**
   * We have included (as per assignment specifications) a feature that manually
   * corrupts a message at a 10% probability. We have implemented this by using a
   * random number generator. When the generator produces integer 5 (arbitrarily
   * chosen) the server actively corrupts an incoming message by slicing a part of
   * it off. This error is subsequently detected and broadcasted.
   * 
   * @param msg The message to be corrupted
   * @returnn The corrupted message
   */

  private String transmissionError(String msg) { // Append 'error' to message. This will cause the subsequent
    // check on equality between hashcodes to fail
    int max = 10;
    int min = 1;
    int range = max - min + 1;

    int rand = (int) (Math.random() * range) + min; // Random number is picked between 1 and 10.

    if (rand == 5) { // Arbitary number to give the desired 10% chance of the msg being corrupted.
      msg += "error";

    }
    return msg;
  }
}
