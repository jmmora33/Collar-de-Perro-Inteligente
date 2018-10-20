package com.grupounlam.soa.collarinteligente;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    public static String TEMPVAR;
    //// BT
    private DispositivosBT bt ;
    private String cadenita;
    /// PARA EL SHAKE
    private int shake;
    private RecibirInformacion info;

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
        desconectar.setEnabled(false);
        conectar.setEnabled(true);
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
                            acelerometro(event.values[0]);
                            break;

                        case Sensor.TYPE_TEMPERATURE:
                            Log.d("Temperatura:","Valor" + event.values[0]);
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
                    Toast.makeText(getBaseContext(),"Conectando....",Toast.LENGTH_SHORT).show();
                    bt.conectar();
                    if(bt.isConnected()){
                    conectar.setEnabled(false);
                    desconectar.setEnabled(true);
                        Toast.makeText(getBaseContext(),"Conexion exitosa",Toast.LENGTH_SHORT).show();
                        Log.d("BT:", "Conexion exitosa");
                    }else{
                        Toast.makeText(getBaseContext(),"Fallo Conexion",Toast.LENGTH_SHORT).show();
                        Log.d("BT:", "Fallo conexion");

                    }
                    recibirBT(true);
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
                        bt.conectar();
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            bt.enviar("Luz");
                        }
                    }).start();


                    Toast.makeText(getBaseContext(),"Prendiendo luz!",Toast.LENGTH_LONG).show();

                }
            });

        }

        if(puerta != null){

            puerta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!bt.isConnected()){
                        bt.cerrarBT();
                        bt.conectar();
                    }
                    Toast.makeText(getBaseContext(),"Abriendo Puerta!",Toast.LENGTH_LONG).show();

                   new Thread(new Runnable() {
                        public void run() {
                            bt.enviar("Puerta");
                        }
                    }).start();

                }
            });
        }
////Boton Desconectar
        if( desconectar != null){

            desconectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recibirBT(false);
                    if(bt.isConnected()){
                        bt.cerrarBT();
                    }
                    Toast.makeText(getBaseContext(),"Desconectado!",Toast.LENGTH_SHORT).show();
                    conectar.setEnabled(true);
                    desconectar.setEnabled(false);
                }
            });
        }

    }

    private void recibirBT(final boolean iniciar){
        RecibirInformacion info = new RecibirInformacion(bt);
        info.start();

      /*  MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                while (iniciar) {
                    if (!bt.isConnected()) {
                        bt.conectar();
                    }
                    cadenita = bt.recibir();
                    if (cadenita != null) {
                        Log.d("CADENA RECIBIDA:", cadenita);
                        tempVar.setText(cadenita);
                    }
                Log.d("UI thread", "I am the UI thread");
            }
        }
        });
    */
    }


    private void registrarSensores(){
        sensorManager.registerListener(sensorListener,sensorprox,2000*1000);
    }

    private void eliminarRegistroSensores(){
        sensorManager.unregisterListener(sensorListener);
    }

    private void acelerometro(float valor){
        if(valor <= 5 && shake == 0){
            shake++;
            getWindow().getDecorView().setBackgroundColor(Color.BLUE);
        }else if(valor > 5 && shake == 1){
            shake++;
            getWindow().getDecorView().setBackgroundColor(Color.GREEN);

        }
        if(shake == 2){
            Log.d("Sensor","Hubo un shake madafaca");
            shake = 0;
        }


    }
    @Override
    protected void onPause() {
        eliminarRegistroSensores();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registrarSensores();
        if(bt != null)
        recibirBT(true);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
