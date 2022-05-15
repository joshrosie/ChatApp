import java.net.InetAddress;

/**
 * This is a simple class used to create a light-weight data storage class for
 * connected clients. Its primary function is to keep track of users currently
 * connected to the server. It is created when a udpClient successfully connects
 * to the udpServer thread and it has several functions that we can use to
 * interact with clients through the server. These are mainly accessor methods
 * such as “getUsername()” which are pivotal in controlling the broadcasting of
 * messages to the appropriate clients.
 * 
 * @author FSHJAR002 RSNJOS005
 * @since 2021-04-02
 */

public class clientObj { // Basic client object. Used exclusively by the udpServer for data storage and
                         // ease-of-use.

  // Defines simple getter methods.

  private String userName;
  private InetAddress address;
  private int port;

  /**
   * Construction of this object requires a username, an IP address and a port
   * number.
   * 
   * @param u The username of the client
   * @param a The IP address of the client
   * @param p The port number of the client
   */

  public clientObj(String u, InetAddress a, int p) { // Client is created with their particular username, IP address and
                                                     // port.
    userName = u;
    address = a;
    port = p;
  }

  /**
   * Accesor method for returning the client's username
   * 
   * @return The client's username
   */
  public String getUsername() {
    return userName;
  }

  /**
   * Accesor method for returning the client's IP address
   * 
   * @return The client's IP address
   */
  public InetAddress getInetAddress() {
    return address;
  }

  /**
   * Accesor method for returning the client's IP address as a string
   * 
   * @return The client's IP address as a string
   */
  public String getStringAddress() {
    return address.getHostAddress();
  }

  /**
   * Accesor method for returning the client's port number
   * 
   * @return The client's port number
   */
  public int getPort() {
    return port;
  }
}
