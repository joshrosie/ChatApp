NETWORKS ASSIGNMENT 1
FSHJAR002, RSNJOS005, NDXPRE050

UDP CLIENT-SERVER APPLICATION

DESCRIPTION:

This project generates a simple UDP-CLIENT SERVER chat room. Where multiple clients
can communicate through a single server. The transport layer utilised is UDP.
Functionality is restricted to text-based communication and is done through command line.
 

EXECUTION:

	In order to compile the program, in the project directory, type "make" to
	compile the files. To run the program, the following options are available:

		- To run the server locally, type "make runServerLOCAL_noerror"
		- Upon doing the above, a local version of the client can be established
		  by typing in make runClientLOCAL. As many clients as you wish can be created.
		  One would however need to open a terminal window, on the same machine, 
		  for each client wishing to connect locally.
		
		- To run the server over a WAN connection, type "make runServerWAN_noerror".
		  NOTE: This version only works when the server is run on FSHJAR002's computer as 
		  an external connection (i.e. over WAN) requires the inclusion of port
		  forwarding to be setup in advance. In addition, the IP addresses that 
		  are used for this WAN setup are unique to FSHJAR002's computer and router.
		- Upon doing the above, a client capable of connecting to the public server 
		  can be established by typing in make runClientWAN.
 
	NOTE: A client can type "@end@" into the chat room and this will shutdown the server
	      as well as close all the clients (including the sender). All
	      connected clients are notified when the server is shutdown before their client
	      is closed.  	

Typing in "make clean" will remove all class files.