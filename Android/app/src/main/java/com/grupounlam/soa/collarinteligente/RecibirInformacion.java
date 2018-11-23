package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Set;


public class RecibirInformacion{

    private DispositivosBT bt;
    private boolean ejecutar;
    private Handler mHandler;
    private static final int CANT_INFO = 20;
    private String[] valores;

    public RecibirInformacion(DispositivosBT blue, Handler miHandler) {
        this.bt = blue;
        mHandler = miHandler;

    }


    public void comenzarARecibir(){

     new Thread(new Runnable() {
         @Override
         public void run() {

             synchronized (this){
              ejecutar = true;
             }

             valores = new String[CANT_INFO];
             while(ejecutar) {
                 Message mensaje = new Message();
                 mensaje.setTarget(mHandler);
                 Bundle info = new Bundle();
                 String cadena = bt.recibir();

                 if (cadena != null && cadena.startsWith("#")) {

                     while (!cadena.contains("&")) {
                         cadena += bt.recibir();
                     }
                     valores = cadena.split(",");
                     Log.d("CADENA ", cadena);
                     if (valores.length >= 7) {
                         info.putString("puerta", valores[1]);
                         info.putString("temp", valores[2]);
                         info.putString("luz",valores[3]);//1= encendida 0 = apagada
                         info.putString("est",valores[4]); //Estado del collar 1=Desprendido 0= ok.
                         info.putString("ladr",valores[5]);
                         info.putString("alarmaTemp",valores[6]);

                     } else {
                         info.putString("puerta", "SIN DATO");
                         info.putString("temp", "0");
                         info.putString("luz","SIN DATO");
                         info.putString("est","SIN DATO");
                         info.putString("ladr","SIN DATO");
                         info.putString("alarma","SIN DATO");
                     }


                     mensaje.setData(info);
                     mensaje.sendToTarget();
                 }
             }

         }
     }).start();
    }

    public  void pararRecibir(){

        synchronized (this){
            ejecutar = false;
        }
    }

    public void cercaniaCollar(){
        new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!MainActivity.PUERTA_ABIERTA && !MainActivity.ACCESO_DENEGADO) {

                            bt.conectar(MainActivity.DIR_COLLAR);
                            if (bt.isConnected()) {
                                bt.cerrarBT();
                                MainActivity.PUERTA_ABIERTA = true;
                                Message mensaje = new Message();
                                Bundle info = new Bundle();
                                mensaje.setTarget(mHandler);
                                info.putString("sol", String.valueOf(MainActivity.PUERTA_ABIERTA));
                                mensaje.setData(info);
                                mensaje.sendToTarget();
                            }

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }}).start();
        }



}




