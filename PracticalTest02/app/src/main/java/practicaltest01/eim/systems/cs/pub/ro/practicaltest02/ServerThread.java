package practicaltest01.eim.systems.cs.pub.ro.practicaltest02;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by MARY on 5/17/2018.
 */
//  un fir de execuție care gestionează solicitările de conexiune de la clienți
 public class ServerThread extends Thread{
     private int port = 0;
     private ServerSocket serverSocket = null;


    // obiectul in care se tin infor obtinute anterior de la clienti
     private HashMap<String, WeatherForecastInformation> data = null;


    public ServerThread(int port) {
        this.port = port;
        // obiectul retinut de server
        this.data = null;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(Constants.TAG, "An exception has occurred: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }


    public synchronized  HashMap<String, WeatherForecastInformation> getData() {
        return data;
    }

    public synchronized void setData(String city, WeatherForecastInformation weatherForecastInformation) {
        this.data.put(city, weatherForecastInformation);
    }

    @Override
    public void run() {
        //atâta vreme cât firul de execuție nu este întrerupt
        //(aplicația Android nu a fost distrusă), sunt acceptate conexiuni de la clienți
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");

                // sunt acceptate conexiuni de la clienți prin invocarea metodei accept()
                Socket socket = serverSocket.accept();

                //comunicația dintre server si fiecare client fiind tratată
                // pe un fir de execuție dedicat
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  void stopThread() {
        interrupt();
        if (serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
