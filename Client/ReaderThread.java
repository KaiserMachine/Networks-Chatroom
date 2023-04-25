/**
 * This thread is passed a socket that it reads from. Whenever it gets input
 * it writes it to the ChatScreen text area using the displayMessage() method.
 */

import java.io.*;
import java.net.*;
import javax.swing.*;

public class ReaderThread implements Runnable
{
   Socket server;
   BufferedReader fromServer;
   ChatScreen screen;

   public ReaderThread(Socket server, ChatScreen screen) {
      this.server = server;
      this.screen = screen;
   }

   public void run() {
      try {
         fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
      
         while (true) {
            String message = fromServer.readLine();

            String[] parts = message.split(" ");

            if (parts[0].equals("JOIN"))
            {
               // Message: "(user) has just joined the chatroom!"
               message = parts[1] + " has just joined the chatroom!";
            }

            if (parts[0].equals("SEND"))
            {
               // Add all other words to the message (as we split by " ")
               String partsTogether = "";
               for (int i = 2; i < parts.length; i++)
               {
                  partsTogether = partsTogether + " " + parts[i];
               }
               // Message: (user):(message)
               // NOTE: Message starts with a space!
               message = parts[1] + ":" + partsTogether;
            }

            // Leave command
            if (parts[0].equals("LEAVE"))
            {
               message = parts[1] + " has left the chatroom!";
            }
         
         	// now display it on the display area
            screen.displayMessage(message);
         }
      }
      catch (IOException ioe) { System.out.println(ioe); }
   
   }
}
