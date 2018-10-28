package com.grupounlam.soa.collarinteligente;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //// Sensores
    private  SensorManager sensorManager;
    private Sensor sensorprox;
    private Sensor sensoracel;
    private Sensor sensorTemp;
    private SensorEventListener sensorListener;
    //// Layout
    private Button conectar;
    private Button desconectar;
    private Button luces;
    private Button puerta;
    private TextView temperatura;
    private TextView tempVar;
    private  TextView puertaVar;
    private Toast toast;
    //// BT
    private DispositivosBT bt ;
    private RecibirInformacion recibir;
    private Handler handler;
    /// PARA EL SHAKE
    private static final int  SHAKE = 15;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        initSensores();
        initComponentes();
        initListeners();


    }
    // Inicializa y asocia los campos y botones
    private void initComponentes(){
        puerta = (Button)findViewById(R.id.puertaButton);
        conectar = (Button) findViewById(R.id.conectarButton);
        desconectar = (Button) findViewById(R.id.desconectarButton);
        temperatura = (TextView) findViewById(R.id.tempView);
        luces = (Button) findViewById(R.id.lucesButton);
        tempVar = (TextView)findViewById(R.id.tempVar);
        puertaVar = (TextView)findViewById(R.id.estadoPuertaVar);
        toast = Toast.makeText(getBaseContext(),"",Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER ,0,0);
        desconectar.setEnabled(false);
        conectar.setEnabled(true);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle mensaje = msg.getData();
                Log.d("Mensaje:",mensaje.getString("temp"));
                puertaVar.setText(mensaje.getString("temp"));
                tempVar.setText(mensaje.getString("puerta"));
                removeMessages(0);
            }
        };



    }
    // Inicializa las variables de los sensores.
    private void initSensores(){

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        sensorprox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensoracel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);


        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                synchronized (this) {

                    switch(event.sensor.getType()) {

                        case Sensor.TYPE_PROXIMITY:
                            if(event.values[0] < sensorprox.getMaximumRange())
                                Toast.makeText(getBaseContext(),"PRECISAS ALGO?",Toast.LENGTH_SHORT).show();
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            if(Math.abs(event.values[0]) > SHAKE || Math.abs(event.values[1]) > SHAKE || Math.abs(event.values[2]) > SHAKE)
                            {
                                Log.d("Sensor", "Hubo un shake madafaca");
                                Toast.makeText(getBaseContext(), "Shake it!", Toast.LENGTH_SHORT).show();
                            }

                            break;




                    }

                }
                Log.d("Sensor", "El evento es"+ event.sensor.getType());
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }
    //// Inicializa los listeners.
    private void initListeners(){

//// Boton Conectar
        if(conectar != null){
            conectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bt = new DispositivosBT();

                    mostrarToast(Toast.LENGTH_LONG,"Conectando...");
                    bt.conectar();

                    if(bt.isConnected()){
                        conectar.setEnabled(false);
                        desconectar.setEnabled(true);
                        mostrarToast(Toast.LENGTH_LONG,"conexion exitosa!");
                        recibir = new RecibirInformacion(bt,handler);
                        recibir.comenzarARecibir();
                        Log.d("BT:", "Conexion exitosa");
                    }else{
                        mostrarToast(Toast.LENGTH_LONG,"Fallo Conexion :(");
                        Log.d("BT:", "Fallo conexion");

                    }

                }
            });
        }
//// Boton luces
        if(luces != null ){

            luces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                    if(!bt.isConnected()){
                        bt.cerrarBT();
                        mostrarToast(Toast.LENGTH_SHORT, "Servicio no conectado");
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    }else {
                        mostrarToast(Toast.LENGTH_SHORT, "Prendiendo Luz!");
                        bt.enviar("Luz");
                    }
                }
            });

        }

        if(puerta != null){

            puerta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!bt.isConnected()){
                        bt.cerrarBT();
                        mostrarToast(Toast.LENGTH_LONG, "Servicio no conectado");
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    }else {
                        mostrarToast(Toast.LENGTH_LONG, "Abriendo Puerta!");
                        bt.enviar("Puerta");
                    }


                }
            });
        }
////Boton Desconectar
        if( desconectar != null){

            desconectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(bt != null && bt.isConnected()){
                        recibir.pararRecibir();
                        bt.cerrarBT();
                    }
                    mostrarToast(Toast.LENGTH_LONG,"Desconectado!");
                    conectar.setEnabled(true);
                    desconectar.setEnabled(false);
                    tempVar.setText("");
                    puertaVar.setText("");
                }
            });
        }

    }

    private void registrarSensores(){
        sensorManager.registerListener(sensorListener,sensorprox,2000*1000);
    }

    private void eliminarRegistroSensores(){
        sensorManager.unregisterListener(sensorListener);
    }

    private void mostrarToast(int duracion, String text){

        toast.setText(text);
        toast.setDuration(duracion);
        toast.show();
    }

    private void verificarEstadoConexion(){

    }
    @Override
    protected void onPause() {
        eliminarRegistroSensores();
        if(bt != null && bt.isConnected()){
            recibir.pararRecibir();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        registrarSensores();
        if(bt != null && bt.isConnected()) {
           // recibir.comenzarARecibir();
        }
        super.onResume();
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onDestroy() {
        super.onDestroy();
        recibir.pararRecibir();
        bt.cerrarBT();
    }



}
