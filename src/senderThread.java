import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.io.OutputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.util.io.Streams;
import org.pgpainless.PGPainless;
import org.pgpainless.algorithm.HashAlgorithm;
import org.pgpainless.algorithm.SymmetricKeyAlgorithm;
import org.pgpainless.encryption_signing.EncryptionOptions;
import org.pgpainless.encryption_signing.EncryptionStream;
import org.pgpainless.encryption_signing.ProducerOptions;
import org.pgpainless.encryption_signing.SigningOptions;
import org.pgpainless.key.generation.type.rsa.RsaLength;

/**
 * senderThread
 * This class implements runnable.
 * The class' primary function is to send user input (as a message)
 * to the server which in turn will distribute these messages to relevant
 * receiver threads.
 * A sender thread is run when a udpClient object is created in a 1:1
 * relationship.
 * Within this class is where user input is requested and processed.
 * To check the integrity of the message being sent, the message’s corresponding
 * hash code is calculated
 * and is appended onto the original message delimited by “@@”.
 * Integrity is then checked when the message arrives at the server.
 * 
 * @author FSHJAR002 RSNJOS005
 * @since 2021-03-31
 */

public class senderThread implements Runnable {

  private static DatagramSocket dSock;
  private static InetAddress serverAddress;
  private static int port;
  public static boolean isConnected;

  /**
   * This is the constructor for the senderThread
   * 
   * @param ds       UDP Socket object. Same as the one created in the udpClient
   *                 object.
   * @param sAddress Server address to send data to.
   * @param p        Server's port number to send data to.
   */
  public senderThread(DatagramSocket ds, InetAddress sAddress, int p) { // Create a new sender thread that is bound to
                                                                        // the relevant Datagram socket.
    // As well as the server details to which messages must be directed.
    dSock = ds;
    serverAddress = sAddress;
    port = p;
    isConnected = false; // isConnected is set to false until the server has confirmed the user can
                         // connect.
  }

  /**
   * Sender thread starts running. It acts independently of the receiver thread.
   * The run method continously loops through taking in userinput and sending the
   * message on
   * until the user closes their client.
   */
  @Override
  public void run() {
    String uname = "";
    Scanner input = new Scanner(System.in); // Create a new Scanner for taking in user input.
    PGPSecretKeyRing secretKey = null;
    PGPPublicKeyRing publicKeys = null;
   
    while (true) {
      String msg = (input.nextLine()).trim();
      // Get the message to be sent from user input.

      if (isConnected) { // If the user has been allowed into the server.
        sendMessage(msg,publicKeys); // Send the message
        // new Thread(new realiableThread(dSock)).start();
      } else { // Try connect the user to the server
        uname = msg;
        try {
        
          secretKey = PGPainless.generateKeyRing().simpleRsaKeyRing(uname, RsaLength._4096);
          publicKeys = PGPainless.extractCertificate(secretKey);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | PGPException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    
        System.out.println("Username is: " + msg);
        sendMessage("connect-User@" + msg, publicKeys);
      }
      if (msg.contains("@exit@"))
        System.exit(0); // If the user requests to shut their client down then the application is
                        // closed.
    }
  }

  /**
   * Sendmessage function is where the actual sending of the data occurs.
   * This is done through first making a packet which contains the data and the
   * destination address.
   * Then the data is sent with the DatagramSocket.send() function.
   * The message being sent has a hashcode appended onto it for checking integrity
   * at the receiving end.
   * 
   * @param msg The message to be sent.
   */

  public static void sendMessage(String msg, PGPPublicKeyRing keys) { // This is a simple sendmessage method to send to server.
    msg = buildMessageChecksum(msg); // Add checksum to message

// Information about the encryption (algorithms, detached signatures etc.)

    byte[] buf = msg.getBytes(); // buffer built from the message.

    DatagramPacket packet = new DatagramPacket( // Create a packet of The buffer, buffer length as well as the IP and
                                                // port of the server.
        buf,
        buf.length,
        serverAddress,
        port);

    try {
      dSock.send(packet); // Try send the message.
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /**
   * Simple method to create a basic checksum for integrity checking purposes.
   * The hashcode generated is appended to the original message and then checked
   * at the receiving end.
   * 
   * @param msg The message to be sent.
   * @return The message being sent plus the generated hashcode delimmited by
   *         '@@'.
   */

  public static String buildMessageChecksum(String msg) {
    String hash = String.valueOf(msg.hashCode()); // Generate a hashcode of the message to be sent.

    msg += "@@" + hash; // Append the hashcode onto the message. Delimtted by the '@@'.

    return msg;
  }
}

