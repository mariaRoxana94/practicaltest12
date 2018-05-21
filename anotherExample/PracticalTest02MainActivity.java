package ro.pub.cs.systems.eim.practicaltest02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class PracticalTest02MainActivity extends AppCompatActivity {

    ServerThread serverThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);


        // views
        final View serverPortEditText = findViewById(R.id.serverPortEditText);
        final View startButton = findViewById(R.id.startButton);
        final View putKeyEditText = findViewById(R.id.putKeyEditText);
        final View getKeyEditText = findViewById(R.id.getKeyEditText);
        final View putValueEditText = findViewById(R.id.putValueEditText);
        final View putButton = findViewById(R.id.putButton);
        final View getButton = findViewById(R.id.getButton);
        final View resultTextView = findViewById(R.id.resultTextView);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serverThread == null) {
                    int port = Integer.parseInt(((EditText) serverPortEditText).getText().toString());
                    serverThread = new ServerThread(port);
                    serverThread.start();
                }
            }
        });

        putButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = "localhost";
                int port = Integer.parseInt(((EditText) serverPortEditText).getText().toString()); // portul serverului


                Message crtMessage = new Message(((EditText)putKeyEditText).getText().toString(), ((EditText)putValueEditText).getText().toString(), 1);
                ClientThread clientThread = new ClientThread(ipAddress, port, resultTextView, crtMessage);
                clientThread.start();
            }
        });

        getButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String ipAddress = "localhost";
                int port = Integer.parseInt(((EditText) serverPortEditText).getText().toString()); // portul serverului


                Message crtMessage = new Message(((EditText)getKeyEditText).getText().toString(), "nothing", 0);
                ClientThread clientThread = new ClientThread(ipAddress, port, resultTextView, crtMessage);
                clientThread.start();
            }
        });

    }

    @Override
    protected void onDestroy() {

        serverThread.stopThread();

        super.onDestroy();
    }
}
