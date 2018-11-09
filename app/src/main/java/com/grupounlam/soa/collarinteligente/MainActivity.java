package com.grupounlam.soa.collarinteligente;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements  SensorEventListener {

    //// Sensores
    private SensorManager sensorManager;


    //// Layout
    private Button conectar;
    private Button desconectar;
    private Button luces;
    private Button puerta;
    private TextView temperatura;
    private TextView tempVar;
    private TextView puertaVar;
    private Toast toast;
    //// BT
    private DispositivosBT bt;
    private final int UMBRAL = 5;
    private RecibirInformacion recibir;
    private RecibirInformacion verPuerta;
    private Handler handlerDatos;
    private Handler handlerPuerta;
    private Handler handlerNews;
    /// PARA EL SHAKE
    private static final int SHAKE = 15;
    //DIALOG
    private AlertDialog dialogPuerta;


    // Inicializa y asocia los campos y botones
    private void initComponentes() {
        puerta = (Button) findViewById(R.id.puertaButton);
        conectar = (Button) findViewById(R.id.conectarButton);
        desconectar = (Button) findViewById(R.id.desconectarButton);
        temperatura = (TextView) findViewById(R.id.tempView);
        luces = (Button) findViewById(R.id.luzButton);
        tempVar = (TextView) findViewById(R.id.tempVar);
        puertaVar = (TextView) findViewById(R.id.estadoPuertaVar);
        toast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        desconectar.setEnabled(false);
        conectar.setEnabled(true);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        handlerNews = new Handler();
        handlerDatos = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle mensaje = msg.getData();
                Log.d("Mensaje:", mensaje.getString("temp"));
                puertaVar.setText(mensaje.getString("temp"));
                tempVar.setText(mensaje.getString("puerta"));
                removeMessages(0);
            }
        };
        handlerPuerta = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialogPuerta = mostrarVentanaEmergente("Ingrese Opcion", "Desea abrir la puerta?");
                dialogPuerta.show();
            }
        };
        verPuerta = new RecibirInformacion(handlerPuerta);
        verPuerta.cercaniaCollar();
    }


            @Override
            public void onSensorChanged(SensorEvent event) {

                synchronized (this) {

                    switch (event.sensor.getType()) {

                        case Sensor.TYPE_PROXIMITY:
                            if (event.values[0] < event.sensor.getMaximumRange())
                                Toast.makeText(getBaseContext(), "PRECISAS ALGO?", Toast.LENGTH_SHORT).show();
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            if (Math.abs(event.values[0]) > SHAKE || Math.abs(event.values[1]) > SHAKE || Math.abs(event.values[2]) > SHAKE) {
                                Log.d("Sensor", "Hubo un shake madafaca, prendiendo ultrasonido");
                                if(bt == null || !bt.isConnected()) {
                                    mostrarToast(Toast.LENGTH_LONG, "No se encontro conexion! ");
                                }else {
                                    bt.enviar("u");
                                    mostrarToast(Toast.LENGTH_LONG, "Shake it! ultrasonido UP!");
                                }
                            }
                            break;
                        case Sensor.TYPE_LIGHT:
                            mostrarToast(Toast.LENGTH_SHORT,"Poca Luz!");
                            break;
                    }


                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }



    //// Inicializa los listeners.
    private void initListeners() {

//// Boton Conectar
        if (conectar != null) {

            conectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = 0;
                    bt = new DispositivosBT();
                    mostrarToast(Toast.LENGTH_SHORT, "Conectando...");
                    while (i < UMBRAL && !bt.isConnected()){
                        bt.conectar();
                        try {
                            Thread.sleep(2000);
                        } catch(InterruptedException e) {
                            Log.d("SLEEPING ERROR","LCG");
                        }
                    if (bt.isConnected()) {
                        conectar.setEnabled(false);
                        desconectar.setEnabled(true);

                        mostrarToast(Toast.LENGTH_SHORT, "conexion exitosa!");
                        recibir = new RecibirInformacion(bt, handlerDatos, false);
                        recibir.comenzarARecibir();
                        Log.d("BT:", "Conexion exitosa");
                    } else {

                        mostrarToast(Toast.LENGTH_SHORT, "Fallo Conexion :(");
                        Log.d("BT:", "Fallo conexion");
                    }
                    i++;
                }
                }
            });
        }
//// Boton luces
        if (luces != null) {

            luces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bt ==null || !bt.isConnected()) {
                        bt.cerrarBT();
                        mostrarToast(Toast.LENGTH_SHORT, "Servicio no conectado");
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    } else {
                        mostrarToast(Toast.LENGTH_SHORT, "Prendiendo Luz!");
                        bt.enviar("Luz");
                    }
                }
            });

        }

        if (puerta != null) {

            puerta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bt ==null || !bt.isConnected()) {
                        bt.cerrarBT();
                        mostrarToast(Toast.LENGTH_SHORT, "Servicio no conectado");
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    } else {
                        mostrarToast(Toast.LENGTH_SHORT, "Abriendo Puerta!");
                        bt.enviar("Puerta");
                    }


                }
            });
        }
////Boton Desconectar
        if (desconectar != null) {

            desconectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bt != null && bt.isConnected()) {
                        recibir.pararRecibir();
                        bt.cerrarBT();
                    }
                    mostrarToast(Toast.LENGTH_LONG, "Desconectado!");
                    conectar.setEnabled(true);
                    desconectar.setEnabled(false);
                    tempVar.setText("");
                    puertaVar.setText("");
                }
            });
        }

    }

    private void registrarSensores() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void eliminarRegistroSensores() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    }

    private void mostrarToast(final int duracion, final String text) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.setText(text);
                toast.setDuration(duracion);
                toast.show();
            }
        },500);

    }

    private AlertDialog mostrarVentanaEmergente(String titulo, String mensaje) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensaje);
        builder.setTitle(titulo);
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                while(bt == null || !bt.isConnected())
                    bt.conectar();
                bt.enviar("puerta");
                bt.cerrarBT();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return builder.create();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        initComponentes();
        initListeners();

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
    public void onStop() {
        super.onStop();
        eliminarRegistroSensores();

        if(bt != null && bt.isConnected()){
            recibir.pararRecibir();
        }
    }

    @Override
    protected void onResume() {
        registrarSensores();
        if(bt != null && bt.isConnected()) {
            recibir.comenzarARecibir();
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        registrarSensores();
        if(bt != null && bt.isConnected()) {
            recibir.comenzarARecibir();
        }
        super.onRestart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        recibir.pararRecibir();
        bt.cerrarBT();
    }



}
