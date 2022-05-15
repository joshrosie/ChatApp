import java.util.Scanner;

import org.pgpainless.*;
import org.bouncycastle.openpgp.*;
/**
 *udpDriver
 *This class is where the main function is called. It has multiple invoking parameters that enable specific startup features.
 *Startup features include a “localhost” start-up which binds the server on the localhost – primarily used for testing,
 *a WAN startup which sets up the server on an external IP address, enabling cross network communication,
 *and a client startup which simply enables a user to interact with the server.
 *The server startup sequence creates a udpServer object and the client startup sequence creates a udpClient object.
 *@author FSHJAR002 RSNJOS005
 *@since 2021-03-29
 */

public class udpDriver {

  static udpClient cUdp;
 
  /**
   *Main function
   *@param args Command line arg
   */
  public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    String uName;
    udpServer server;

    
   
    switch (args[0].charAt(0)) { //First character of cmd line input/
      case 's': // 's' For server
        switch (args[0]) { //What type of client? i.e. locally connected or connected over WAN
          case "sWan":
            server = new udpServer("192.168.0.111", 1234, Boolean.parseBoolean(args[1])); //for wan connection
            break;
          default:
            server = new udpServer("localhost", 1234, Boolean.parseBoolean(args[1])); //For local connection
            break;
        }
        server.run(); //start server up
        /*Starts a single udpServer thread. The
        reason we opted to implement it this way was to ensure concurrent
        functionality with udpClient objects and to be sparing with resources. */
        break;
      case 'c': // 'c' For client
        

        System.out.println("Please enter your username below:");

        switch (args[0]) { //What type of client? i.e. locally connected or connected over WAN
          case "cWan":
            cUdp = new udpClient("192.168.100.112", 1234); //create new udpClient over specifc wan connection.
            break;
          default:
            cUdp = new udpClient("localhost", 1234); //create new udpClient over generic local host connection.
            break;
        }

 

        break;
    }
  }
}
