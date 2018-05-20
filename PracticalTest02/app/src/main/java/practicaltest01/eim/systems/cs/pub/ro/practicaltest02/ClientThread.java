package practicaltest01.eim.systems.cs.pub.ro.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by MARY on 5/17/2018.
 */

public  class ClientThread extends Thread {
    String address =  null;
    int port = 0;
    String city =  null;
    String infoType = null;
    TextView resultText;
    Socket socket;

    public ClientThread(String address, int port, String city, String infoType, TextView resultText) {
        this.address = address;
        this.port  = port;
        this.city =  city;
        this.infoType = infoType;
        this.resultText =  resultText;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            // transmit serverului orasul
            printWriter.println(city);
            printWriter.flush();

            // transmit serverului infoType
            printWriter.println(infoType);
            printWriter.flush();

            // citesc de la server rezultatul
            String weatherInfo;
            while ((weatherInfo = bufferedReader.readLine()) != null) {
                final String result = weatherInfo;
                resultText.post(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setText(result);
                    }
                });
            }
        }catch (Exception e){
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + e.getMessage());
            e.printStackTrace();
        }finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }
}
