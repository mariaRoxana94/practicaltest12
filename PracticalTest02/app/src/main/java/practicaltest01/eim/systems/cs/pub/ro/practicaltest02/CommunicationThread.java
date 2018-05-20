package practicaltest01.eim.systems.cs.pub.ro.practicaltest02;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by MARY on 5/17/2018.
 */

public class CommunicationThread extends Thread{
    //comunicația dintre server si fiecare client fiind tratată pe un fir de execuție dedicat
    ServerThread serverThread = null;
    Socket socket = null;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");

            // citeste de la client
            String city = bufferedReader.readLine();
            // spinner ul cu optiuni
            String informationType =  bufferedReader.readLine();
            if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            //info meteo se găsesc în obiectul gestionat de server, sunt preluate local
            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation = null;

            if (data != null && data.containsKey(city)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);

            } else { // info nu sunt in obiectul de pe server
                // sunt preluate prin interogarea serviciului Internet, la distanță
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");

                //facem asta printr-o cerere POST
                HttpClient httpClient = new DefaultHttpClient();
                // https://www.wunderground.com/weather/ro
                //http://www.wunderground.com/cgi-bin/findweather/getForecast
                HttpPost httpPost = new HttpPost("https://www.wunderground.com/weather/ro");
                List<NameValuePair> pairs = new ArrayList<>();

                //orașul despre care se doresc să se obțină informațiile meteorologice
                pairs.add(new BasicNameValuePair("query", city));
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, HTTP.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);

                // raspunsul de la site, care vine in format HTML - DOM
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSource = httpClient.execute(httpPost, responseHandler);

                if (pageSource == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }

                //parsare DOM
                Document document = Jsoup.parse(pageSource);
                Element element = document.child(0);
                Elements elements = element.getElementsByTag("script");
                for (Element script : elements) {
                    String scriptData = script.data();
                    if (scriptData.contains("wui.api_data =\\n")) {
                        // cum calculeaza pozitia?
                        int position = scriptData.indexOf("wui.api_data =\\n") + ("wui.api_data =\\n").length();
                        scriptData = scriptData.substring(position);
                        Log.i(Constants.TAG, "[COMMUNICATION THREAD] the position" + String.valueOf(position) + "scriptData este" + scriptData);

                        JSONObject content = new JSONObject(scriptData);
                        JSONObject currentObservation = content.getJSONObject("current_observation");
                        // de acolo se ia info pt cele de mai jos
                        String temperature = currentObservation.getString("temperature");
                        String windSpeed = currentObservation.getString("wind_speed");
                        String condition = currentObservation.getString("condition");
                        String pressure = currentObservation.getString("pressure");
                        String humidity = currentObservation.getString("humidity");
                        weatherForecastInformation = new WeatherForecastInformation(
                                temperature, windSpeed, condition, pressure, humidity
                        );
                        serverThread.setData(city, weatherForecastInformation);
                        break;
                    }
                }
            }
            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = null;
            switch(informationType) {
                case "all":
                    result = weatherForecastInformation.toString();
                    break;
                case "temperature":
                    result = weatherForecastInformation.getTemperature();
                    break;
                case "wind_speed":
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case "condition":
                    result = weatherForecastInformation.getCondition();
                    break;
                case "humidity":
                    result = weatherForecastInformation.getHumidity();
                    break;
                case "pressure":
                    result = weatherForecastInformation.getPressure();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }

            //scrie rezultatul clientului
            printWriter.println(result);
            printWriter.flush();

        }catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            if (socket != null){
                try {
                    socket.close();
                }catch (Exception e) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}
