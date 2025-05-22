package com.avm.application7;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    EditText txtIP, txtMensaje;
    TextView tvMesaageLog;

    Button btnIniciarServicor, btConectar, btEnviarMensaje;
    ServerSocket serverSocket;
    Socket socket;
    BufferedWriter writer;
    Handler handler= new Handler();
    boolean isRunning = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtIP= findViewById(R.id.txtIP);
        btConectar = findViewById(R.id.btConectar);
        btnIniciarServicor = findViewById(R.id.btIniciarServidor);
        Switch sw = findViewById(R.id.swModo);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                if(b){
                    btnIniciarServicor.setVisibility(VISIBLE);
                    btConectar.setVisibility(INVISIBLE);
                    txtIP.setVisibility(INVISIBLE);
                }
                else {
                    btnIniciarServicor.setVisibility(INVISIBLE);
                    btConectar.setVisibility(VISIBLE);
                    txtIP.setVisibility(VISIBLE);
                }
            }
        });
        txtMensaje = findViewById(R.id.txtMensaje);
        tvMesaageLog = findViewById(R.id.txtMessageLog);
        btEnviarMensaje = findViewById(R.id.btnEnviarMensaje);
        btnIniciarServicor.setOnClickListener( v -> startServer() );
        btConectar.setOnClickListener(v -> connectarServer());
        btEnviarMensaje.setOnClickListener(
                v -> sendMessage(txtMensaje.getText().toString())
        );
        new Thread(this::lintendMessage).start();
    }//Contructor de la interfaz
    //-------------------Metodos
    private void startServer(){
        new Thread( () -> {
            try {
                serverSocket = new ServerSocket(5000);
                runOnUiThread( ()-> Toast.makeText(this, "Servidor iniciado", Toast.LENGTH_SHORT).show());
                socket = serverSocket.accept();
                writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()));
                isRunning = true;

            }catch (IOException e){
                runOnUiThread( () ->
                        Toast.makeText(this, "Error al iniciar el servidor", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }//iniciar servidor
    private void connectarServer(){
        if(txtIP.getText().toString().isEmpty()){
            Toast.makeText(this, "debe ingresar la ip", Toast.LENGTH_SHORT).show();
            txtIP.requestFocus();
            return;
        }
        new Thread( ()-> {
            try{
                socket = new Socket(txtIP.getText().toString(), 5000);
                isRunning = true;
                writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()));
                runOnUiThread( () ->  Toast.makeText(this,"Conectado al servidor", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                runOnUiThread( () ->
                        Toast.makeText(this,"Error al conectar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }//conectar al servidor
    @Override
    protected void onDestroy(){
        super.onDestroy();
        isRunning=false;
        try{
            if(serverSocket != null)
                serverSocket.close();
            if(socket != null)
                socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } //
    private void sendMessage(String s){
        if(writer == null || s.isEmpty()) return;
        new Thread( () -> {
            try{
                writer.write(s+"\n");
                writer.flush();
                runOnUiThread( () -> tvMesaageLog.append("YO > " + s + "\n"));
                txtMensaje.setText("");
            }catch (IOException e){
                runOnUiThread( ()->
                        Toast.makeText(this, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }//enviar mensajes
    private void lintendMessage(){
        try{
            while(isRunning){
                if(socket != null && socket.getInputStream() != null){
                    BufferedReader r = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    String m;
                    while((m=r.readLine()) != null){
                        final String mensaje = m;
                        handler.post( ()->{
                            tvMesaageLog.append("<" + mensaje);
                        });
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //------------------
}//llave de la clase