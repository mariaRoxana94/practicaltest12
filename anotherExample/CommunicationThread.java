package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;



public class CommunicationThread extends Thread
{
    Socket clientSocket;
    PrintWriter printWriter;
    BufferedReader bufferedReader;
    ServerThread serverThread;


    public CommunicationThread(ServerThread serverThread, Socket clientSocket)
    {
        this.serverThread = serverThread;
        this.clientSocket = clientSocket;
    }



    @Override
    public void run() {
        super.run();

        try {
            printWriter = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()), true);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Message m = Message.fromString(bufferedReader.readLine());

            Log.d("QQ", "[Server] Recv request for key " + m.key + " and value " + m.value + " (" + Integer.toString(m.operation) + ")");


            // put
            if (m.operation == 1)
            {
                Log.d("QQ", "[Server] Added key " + m.key + " and value " + m.value);
                serverThread.putInformation(m.key, m.value);

                Message m2 = new Message(m.key, m.value, 2);
                printWriter.println(Message.toString(m2));
                printWriter.flush();
            }
            else
                if (m.operation == 0)
                {
                    String value = serverThread.getInformation(m.key);

                    Log.d("QQ", "[Server] Returned key " + m.key + " and value " + value);

                    Message m2 = new Message(m.key, value, 2);
                    printWriter.println(Message.toString(m2));
                    printWriter.flush();
                }


            //Message m = new Message(serverThread.getInformation(tag));

            //printWriter.println(Message.toString(m));
            //printWriter.flush();

            clientSocket.close();


            Log.d("QQ", "[Server] Client disconnected");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
