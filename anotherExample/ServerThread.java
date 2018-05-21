package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class ServerThread extends Thread {

    ServerSocket serverSocket;
    int port;
    Hashtable<String, String> cacheTable = new Hashtable<String, String>();
    Hashtable<String, Long> timeoutTable = new Hashtable<String, Long>();

    public ServerThread(int port)
    {
        this.port = port;
    }

    public void stopThread()
    {
        interrupt();
        try {
            serverSocket.close();
            Log.d("QQ", "[Server] Closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Long getTimeStamp()
    {
        HttpClient httpClient = new DefaultHttpClient();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        HttpGet httpGet = new HttpGet("http://worldclockapi.com/api/json/est/now");

        String response = null;
        try {
            response = httpClient.execute(httpGet, responseHandler);
            JSONObject content = new JSONObject(response);
            response = content.getString("currentFileTime");

            Log.d("QQ", "[Server] Got file time: " + response);

            //content.getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("wind").getString("direction");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Long.parseLong(response);
    }

    public void putInformation(String tag, String value)
    {
        Log.d("QQ","[Server] Added key+value to hashtable");
        cacheTable.put(tag, value);
        timeoutTable.put(tag, getTimeStamp());
    }

    public String getInformation(String tag)
    {
        Long timeStamp = getTimeStamp();
        Log.d("QQ", Long.toString(timeStamp) + " - " + timeoutTable.get(tag));
        if (cacheTable.containsKey(tag) && getTimeStamp() - timeoutTable.get(tag) < 1000 * 1000 * 60)
        {
            Log.d("QQ","[Server] Key found in hashtable / cached");
            return cacheTable.get(tag);
        }
        else
        {
            Log.d("QQ","[Server] Key not found");
            return "NA";
        }
    }

    @Override
    public void run()
    {
        super.run();

        ArrayList<CommunicationThread> clientThreads = new ArrayList<CommunicationThread>();

        try {
            serverSocket = new ServerSocket(port);

            Log.d("QQ", "[Server] Started");

            while (!Thread.currentThread().isInterrupted())
            {
                Socket clientSocket = serverSocket.accept();
                Log.d("QQ", "[Server] Client connected");
                CommunicationThread comThread = new CommunicationThread(this, clientSocket);
                comThread.start();
                clientThreads.add(comThread);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
