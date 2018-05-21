package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class ClientThread extends Thread
{
    String ipAddress;
    int port;
    PrintWriter printWriter;
    BufferedReader bufferedReader;
    View resultsTextView;
    Message crtMessage;

    public ClientThread(String ipAddress, int port, View resultsTextView, Message m)
    {
        this.ipAddress = ipAddress;
        this.port = port;
        this.resultsTextView = resultsTextView;
        crtMessage = m;
    }

    @Override
    public void run() {
        super.run();


        try {
            Socket clientSocket = new Socket(ipAddress, port);

            printWriter = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()), true);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //printWriter.println(tag);

            printWriter.println(Message.toString(crtMessage));

            String recvString;
            while((recvString = bufferedReader.readLine()) != null)
            {
                try {
                    final Message m = Message.fromString(recvString);
                    Log.d("QQ", "Recv: " + m.key + " " + m.value);

                    if (crtMessage.operation == 0)
                        resultsTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView)resultsTextView).setText(((TextView)resultsTextView).getText().toString() + " " + m.key + " " + m.value + "\n");
                            }
                        });

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
