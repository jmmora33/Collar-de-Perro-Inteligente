package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.Set;

public class RecibirInformacion extends AppCompatActivity{

    private DispositivosBT bt;
    private boolean ejecutar;
    private Handler mHandler;
    private BroadcastReceiver bReciever;
    private IntentFilter filter;
    private boolean estadoPuerta;
    private boolean registrar;


    public RecibirInformacion(DispositivosBT blue, Handler miHandler, boolean registrar) {
        this.bt = blue;
        this.registrar = registrar;
        mHandler = miHandler;

    }

    public RecibirInformacion( Handler miHandler) {
        mHandler = miHandler;
        registrar = true;

    }

    public void comenzarARecibir(){

     new Thread(new Runnable() {
         @Override
         public void run() {
             ejecutar = true;
             while(ejecutar) {
                 Message mensaje = new Message();
                 mensaje.setTarget(mHandler);
                 Bundle info = new Bundle();
                 String cadena = bt.recibir();
                 String[] valores = new String[7];

                 if (cadena != null && cadena.startsWith("#")) {

                     while (cadena != null && !cadena.contains("&")) {
                         cadena += bt.recibir();
                     }
                     valores = cadena.split(",");
                     Log.d("CADENA ", cadena);
                     if (valores.length >= 3) {
                         info.putString("puerta", valores[1]);
                         info.putString("temp", valores[2]);
                     } else {
                         info.putString("puerta", "SIN DATO");
                         info.putString("temp", "0");
                     }


                     mensaje.setData(info);
                     mensaje.sendToTarget();
                 }
             }

         }
     }).start();
    }

    public  void pararRecibir(){
       ejecutar = false;
    }

    public void cercaniaCollar(){
        // Register the BroadcastReceiver




        bReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getAddress().equals(bt.ADDRESS)){
                        Message mensaje = new Message();
                        Bundle info = new Bundle();
                        mensaje.setTarget(mHandler);

                        //EL estado puerta podra verse a partir de la recepcion de información
                        //por parte del arduino.
                        if(!estadoPuerta){
                            info.putString("sol","abrir");
                            mensaje.setData(info);
                            Log.d("pepe","pase por aca");
                        }else{
                            info.putString("sol","abierta");
                            mensaje.setData(info);
                            Log.d("pepe","pase por aca");
                        }
                        mensaje.sendToTarget();
                    }

                }
            }

        };
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

    }

    public void cercaniaCollar(int i){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> bd = bt.getBondedDevices();

                    for (BluetoothDevice device : bd) {
                        if (device.getAddress().equals(DispositivosBT.ADDRESS)) {
                            Message mensaje = new Message();
                            Bundle info = new Bundle();
                            mensaje.setTarget(mHandler);
                            info.putString("sol", "abrir");
                            mensaje.setData(info);
                        }

                    }
                }
            }
        }).start();
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(registrar)
            registerReceiver(bReciever,filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(registrar)
            unregisterReceiver(bReciever);
    }

    @Override
    protected void onResume() {
        if(registrar)
            registerReceiver(bReciever,filter);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if(registrar)
            registerReceiver(bReciever,filter);
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registrar)
            unregisterReceiver(bReciever);
    }

}