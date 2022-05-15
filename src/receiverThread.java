import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * receiverThread This class implements runnable. This class' primary function
 * is to receive messages distributed by the server and print them to screen.
 * All subsequent functionality halts (within the class) while waiting to
 * receive a message and continues after it has. A receiver thread is run when a
 * udpClient object is created in a 1:1 relationship. The receiverThread class
 * also checks if a message was corrupted between the server and the receiver.
 * It does this by calculating a hash code of the message, then comparing it to
 * the original hash code sent along with the message (see buildChecksum in
 * senderThread). If these hash codes match, then the message arrived
 * uncorrupted.
 * 
 * @author FSHJAR002 RSNJOS005
 * @since 2021-03-31
 */

class receiverThread implements Runnable {

  private DatagramSocket dSock;

  /**
   * Constructor for receiverThread. Sets the DatagramSocket without specifying a
   * particular IP address and port.
   * 
   * @param ds UDP Socket object. Same as the one created in the udpClient object.
   */

  public receiverThread(DatagramSocket ds) { // Create a new receiver thread bound to the relevant Datagram Socket -
                                             // this comes from the associated client.
    dSock = ds;
  }

  /**
   * Receiver thread starts running. It acts independently of the sender thread.
   * The run method continously loops through waiting to receive and print out
   * messages being sent until the user closes their client.
   */
  @Override
  public void run() { // receiver thread starts running. It acts independently of the send thread.
    while (true) {
      byte[] buf = new byte[1024]; // Create buffer with size 1024 bytes.

      DatagramPacket dpRecv = new DatagramPacket(buf, buf.length); // Create new Datagram packet for receiving data.

      try {
        dSock.receive(dpRecv); // Wait to receive the data.
      } catch (IOException e) {
        System.out.println(e);
      }

      String str = new String(dpRecv.getData(), 0, dpRecv.getLength()).trim(); // Get the message sent with the packet.
      // This currently includes the appended hashcode.

      int y, iHash = 0;
      String msg = "";
      String errorMsg = "Message corrupted here at receiver side. Please resend Message.";

      if (str.contains("connected@")) {
        senderThread.isConnected = true;
        int x = str.indexOf("@");
        int z = str.lastIndexOf("@");
        String username = str.substring(x + 1, z - 1);
        printWelcomeInformation(username);

      } else if (str.contains("@Corrupted, please resend message.@")) { // This would be the case if the message was
                                                                        // corrupted between the original sender and the
                                                                        // server, or at the server itself.
        System.out.println("Corrupted at server side. Please resend message.");
      } else { // If the message arrived and was not corrupted between the original sender or
               // at the server.
        y = str.lastIndexOf("@");
        msg = str.substring(0, y - 1); // Find the actual message sent along with the packet.
        iHash = Integer.valueOf(str.substring(y + 1, str.length())); // Extract the hashcode from the packet.
      }

      if (str.contains("@shutdown@ by user:"))
        System.out.println( // If any client shuts the server down, all clients are notified.
            "Server has been shutdown, please exit client.");
      else if (iHash == msg.hashCode())
        System.out.println(msg); // If the hashcode that was sent along with the message
      // matches the hashcode calculated now, the message is printed.

      else
        System.out.println(errorMsg); // Otherwise, the relevant error message is printed.
    }
  }

  /**
   * This method prints welcoming messages and information to the client. It is primarily
   * for aesthetic purposes.
   * @param uname The username linked to the client
   */

  private void printWelcomeInformation(String uname) {
    System.out.println("\n");
    System.out.println("Welcome to the server, " + uname);
    System.out.println();
    System.out.println("The following commands are available to you:");
    System.out.println("@shutdown@ \t" + "Shuts the server down.");
    System.out.println("@exit@ \t \t" + "Closes your client down.");
    System.out.println("@history@ \t" + "Prints the chat history stored on the server.");
    System.out.println();
    System.out.print("Connecting ");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println(e);
    }
    System.out.print(".");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println(e);
    }
    System.out.print(".");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println(e);
    }
    System.out.print(".");
    System.out.print("\t Connected");
    System.out.println();
    System.out.println("You may begin by typing below:");
  }
}
