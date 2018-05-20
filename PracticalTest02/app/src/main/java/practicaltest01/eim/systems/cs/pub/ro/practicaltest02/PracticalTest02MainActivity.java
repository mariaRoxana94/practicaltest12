package practicaltest01.eim.systems.cs.pub.ro.practicaltest02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class PracticalTest02MainActivity extends AppCompatActivity {
    //server widgets
    private EditText portServer = null;
    private Button  connectServer  = null;

    //client
    private EditText addressClient  = null;
    private EditText portClient  = null;
    private EditText cityClient  = null;
    private Button  getWeatherClient = null;
    private Spinner informationTypeSpinner = null;

    //rezultat
    private TextView resultText  = null;

    //Threaduri
    private ServerThread severThread = null;
    private ClientThread clientThread =  null;

    //listeners
   private ButtonClickListener connectServerClickListener = new ButtonClickListener();
    private class ButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            String serverPort = portServer.getText().toString();
            if (serverPort ==  null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                // pornirea firului de execuție corespunzător serverului care duce catre serverSocket
                severThread = new ServerThread(Integer.parseInt(serverPort));
                if (severThread.getServerSocket() == null) {
                    Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                    return;
                }
                // nu e null
                severThread.start();
            }
        }
    }

    private ButtonClickListenerClient weatherClientClickListener = new ButtonClickListenerClient ();
    private class ButtonClickListenerClient implements Button.OnClickListener{

        @Override
        public void onClick(View view) {
            String address = addressClient.getText().toString();
            String clientPort  = portClient.getText().toString();
            if (address == null || address.isEmpty()
                    || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (severThread == null || !severThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            String  city = cityClient.getText().toString();
            String infoType = informationTypeSpinner.getSelectedItem().toString();
            if (city == null || city.isEmpty()
                    || infoType == null || infoType.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (city / information type) should be filled", Toast.LENGTH_SHORT).show();
                return;
            }
            resultText.setText(" ");

            clientThread = new ClientThread(address, Integer.parseInt(clientPort),
                    city, infoType, resultText);

            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");

        // server
        portServer = findViewById(R.id.port);
        connectServer = findViewById(R.id.buttonConnect);
        connectServer.setOnClickListener(connectServerClickListener);


        //client
        addressClient = findViewById(R.id.address);
        portClient  = findViewById(R.id.portClient);
        cityClient  = findViewById(R.id.cityClient);
        getWeatherClient   = findViewById(R.id.buttonWeather);
        getWeatherClient.setOnClickListener(weatherClientClickListener);
        informationTypeSpinner = findViewById(R.id.information_type_spinner);

        //result
        resultText = findViewById(R.id.weather_forecast_text_view);
    }

    // aplicația Android este distrusă să se oprească firul de execuție
    // corespunzător serverului, eliberându-se resursele alocate
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        severThread.stopThread();
    }
}
