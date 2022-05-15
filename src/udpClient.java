import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
*udpClient
*This class is the platform through which the client interacts with the server and by extension, other clients.
*A udpClient contains a sender and a receiver thread which allow clients to send and receive messages simultaneously. 
*@author FSHJAR002 RSNJOS005
*@since 2021-03-29 
*/

public class udpClient {

  private  DatagramSocket socket;
  private  InetAddress serverAddress;
  private  int port;
  

  
  /**
   * This is the constructor for the udpClient. The relevant attributes to this class are set here.
   * If anything goes wrong with the creating of the socket or threads, the error is caught and printed to terminal.
   * @param destinationAddr The address of the server to send data to.
   * @param port The port of the server to send data to.
   */

  public udpClient(String destinationAddr, int port) { //Create a new udpClient for a particular user.
    //The address and port of the server are also needed.
    try {
      this.port = port;
      serverAddress = InetAddress.getByName(destinationAddr);
      
      socket = new DatagramSocket();
      senderThread sendT = new senderThread(socket, serverAddress, port);
      receiverThread recieveT = new receiverThread(socket);
     // senderThread.sendMessage("connect-User@" + uName); //Call sendMessage() with a message telling the server a new user is connecting.
      new Thread(sendT).start(); //Start a sender thread bound to the udpClient
      new Thread(recieveT).start(); //Start a receiver thread bound to the udpClient
    } catch (IOException e) {
      System.out.println(e);
    }
  }

 
}
