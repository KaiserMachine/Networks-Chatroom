/**
 * A server listening on port 6007. 
 *
 * @author - Greg Gagne.
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.Vector;
import java.util.ArrayList;

public class  Server
{
   public static final int DEFAULT_PORT = 5555;
   // ArrayList and Vector go here
   private static ArrayList<BufferedWriter> clients = new ArrayList<BufferedWriter>();
   private static Vector<String> messageQueue = new Vector<String>();

	// construct a thread pool for concurrency	
   private static final Executor exec = Executors.newCachedThreadPool();

   public static void main(String[] args) throws IOException {
      ServerSocket sock = null;
      Socket client = null;
      BufferedWriter toClient = null;
   
      try {
      	// establish the socket
         sock = new ServerSocket(DEFAULT_PORT);
         
         // Broadcast thread
         Runnable broadcastThread = new BroadcastThread(clients, messageQueue);
         exec.execute(broadcastThread);
         
         while (true) {
         	/**
         	 * now listen for connections
         	 * and service the connection in a separate thread.
         	 */
            client = sock.accept();
            toClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            clients.add(toClient);
            Runnable task = new Connection(client, messageQueue);
            System.out.println(task);
            exec.execute(task);
         }
      }
      catch (IOException ioe) { System.err.println(ioe); }
      finally {
         if (sock != null)
            sock.close();
         if (toClient != null)
            toClient.close();
         if (client != null)
            client.close();
      }
   }
}

class Handler 
{

	/**
	 * this method is invoked by a separate thread
	 */
    
   public void process(Socket client, Vector<String> messageQueue) throws java.io.IOException {
      // while buffered reader reads line...
      // Runnable reader = new ReaderThread(
      BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
      String line;
      while ( (line = fromClient.readLine()) != null ) {
         messageQueue.add(line);
      }
   }
}

class Connection implements Runnable
{
   private Socket	client;
   private Vector<String> messageQueue;
   private static Handler handler = new Handler();

   public Connection(Socket client, Vector<String> messageQueue) {
      this.client = client;
      this.messageQueue = messageQueue;
   }

	/**
	 * This method runs in a separate thread.
	 */	
   public void run() { 
      try {
         handler.process(client, messageQueue);
      }
      catch (java.io.IOException ioe) {
         System.err.println(ioe);
      }
   }
}

class BroadcastThread implements Runnable
{
   private ArrayList<BufferedWriter> clients;
   private Vector<String> messageQueue;
   private String message;

   public BroadcastThread(ArrayList<BufferedWriter> clients, Vector<String> messageQueue) {
      this.clients = clients;
      this.messageQueue = messageQueue;
   }
   public void run() {
      while (true) {
         // sleep for 1/10th of a second
         try { Thread.sleep(100); } 
         catch (InterruptedException ignore) { }

         ArrayList<BufferedWriter> toBeDeleted = null;

         while(!messageQueue.isEmpty()) {

            toBeDeleted = new ArrayList<BufferedWriter>();

            message = messageQueue.remove(0);
               for (int i = 0; i < clients.size(); i++) {
                  try {
                     clients.get(i).write(message + "\r\n");
                     clients.get(i).flush();
                  } catch (java.io.IOException ioe) {
                     // clients.remove(clients.get(i));
                     toBeDeleted.add(clients.get(i));
                     System.out.println(ioe);
                  }

               }
            }
            if (toBeDeleted != null && !toBeDeleted.isEmpty())
            {
               for (int i = 0; i < toBeDeleted.size(); i++)
               {
                  clients.remove(toBeDeleted.get(i));
               }
            }
      }
   }
}