package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothServerSocket;
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


    ////Direccion de dispositivos
    public static final String DIR_COLLAR = "00:21:13:00:83:8C";

    //// ESTADOS
    public static  boolean IS_CONNECT = false;
    public static boolean PUERTA_ABIERTA = false;
    public static boolean LUZ_PRENDIDA = false;

    //// Sensores
    private SensorManager sensorManager;
    private static int UMBRAL_LUZ = 10;
    private static final int SHAKE = 15;
    private int contador_iluminancia;

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
    private final int UMBRAL_CONEXION = 5;
    private RecibirInformacion recibir;
    private RecibirInformacion verPuerta;
    private Handler handlerDatos;
    private Handler handlerPuerta;
    private Handler handlerNews;

    /// DATA PARA EL ENVIO DE INFORMACION
    private static final String ABRIR_PUERTA = "1";
    private static final String CERRAR_PUERTA = "2";
    private static final String PRENDER_LUZ = "3";
    private static final String APAGAR_LUZ = "4";
    private static final String ULTRASONIDO = "5";
    private static final String BUZZER = "6";

    //DIALOG
    private AlertDialog dialog;


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
                dialog = mostrarVentanaEmergente("Ingrese Opcion", "Desea abrir la puerta?", "puerta");
                dialog.show();
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

                                if(bt == null || !bt.isConnected()) {
                                   mostrarToast(Toast.LENGTH_LONG,  getResources().getString(R.string.no_conection));
                                 }else {
                                   bt.enviar("buz");
                                   mostrarToast(Toast.LENGTH_LONG, "Alarma!");
                                  }
                            //HAcer sonar el buzzer (que sean segundos)
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            if (Math.abs(event.values[0]) > SHAKE || Math.abs(event.values[1]) > SHAKE || Math.abs(event.values[2]) > SHAKE) {
                                Log.d("Sensor", "Hubo un shake madafaca, prendiendo ultrasonido");
                                if(bt == null || !bt.isConnected()) {
                                    mostrarToast(Toast.LENGTH_LONG, getResources().getString(R.string.no_conection));
                                }else {
                                    bt.enviar("ultra");
                                    mostrarToast(Toast.LENGTH_LONG, getResources().getString(R.string.action_shake));
                                }
                            }
                            break;
                        case Sensor.TYPE_LIGHT:
                            if(!LUZ_PRENDIDA) {
                                if (event.values[0] < UMBRAL_LUZ)
                                    contador_iluminancia++;
                                if (contador_iluminancia > 10) {
                                    dialog = mostrarVentanaEmergente("Se Detecto Poca Luz", "Desea prender la luz?", "luz");
                                    dialog.show();
                                    contador_iluminancia = 0;
                                    LUZ_PRENDIDA = true;
                                    luces.setText(R.string.action_luz_down);
                                }
                            }
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

                    //OPtimizar para quitar esta logica de aca.
                    while (i < UMBRAL_CONEXION && !bt.isConnected()){
                        bt.conectar(DIR_COLLAR);

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

                        mostrarToast(Toast.LENGTH_SHORT, getResources().getString(R.string.no_conection));
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    } else {
                        if(LUZ_PRENDIDA){
                            mostrarToast(Toast.LENGTH_SHORT, "Apagando Luz!");
                            bt.enviar(APAGAR_LUZ);
                            luces.setText(getResources().getString(R.string.action_luz_down));
                        }else {
                            mostrarToast(Toast.LENGTH_SHORT, "Prendiendo Luz!");
                            bt.enviar(PRENDER_LUZ);
                            luces.setText(getResources().getString(R.string.action_luz_up));
                        }
                    }
                }
            });

        }

        if (puerta != null) {

            puerta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bt ==null || !bt.isConnected()) {

                        mostrarToast(Toast.LENGTH_SHORT, getResources().getString(R.string.no_conection));
                        conectar.setEnabled(true);
                        desconectar.setEnabled(false);
                    } else {

                        if(PUERTA_ABIERTA){
                            mostrarToast(Toast.LENGTH_SHORT, "Abriendo Puerta!");
                            bt.enviar(CERRAR_PUERTA); //CerrarPuerta
                            puerta.setText(getResources().getString(R.string.action_puerta_close));
                        }else{
                            mostrarToast(Toast.LENGTH_SHORT, "cerrando Puerta!");
                            bt.enviar(ABRIR_PUERTA); //AbrirPuerta
                            puerta.setText(getResources().getString(R.string.action_puerta_open));
                        }

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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void eliminarRegistroSensores() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
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

    private AlertDialog mostrarVentanaEmergente(String titulo, String mensaje, final String enviar) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensaje);
        builder.setTitle(titulo);
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               /*
                while(bt == null || !bt.isConnected())
                        bt.conectar(DIR_COLLAR);

                        bt.enviar(enviar);
                        bt.cerrarBT();
                        */
               mostrarToast(Toast.LENGTH_SHORT,enviar+"!");
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
    public void onDestroy() {
        super.onDestroy();
        if(bt != null && bt.isConnected()) {
            recibir.pararRecibir();
            bt.cerrarBT();
        }
    }
}
