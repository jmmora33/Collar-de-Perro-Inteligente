package com.grupounlam.soa.collarinteligente;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;



public class RecibirInformacion extends AppCompatActivity{

    private DispositivosBT bt;
    private boolean ejecutar;
    private Handler mHandler;
    private boolean registrar;
    private static final int CANT_INFO = 20;
    private String[] valores;

    public RecibirInformacion(DispositivosBT blue, Handler miHandler, boolean registrar) {
        this.bt = blue;
        this.registrar = registrar;
        mHandler = miHandler;

    }

    public RecibirInformacion(Handler miHandler) {
        mHandler = miHandler;
        registrar = true;

    }

    public void comenzarARecibir(){

     new Thread(new Runnable() {
         @Override
         public void run() {
             ejecutar = true;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if(!MainActivity.PUERTA_ABIERTA){}
    /*
                        DispositivosBT bt = new DispositivosBT();
                        bt.conectar();
                        if (bt.isConnected()) {
                            Message mensaje = new Message();
                            Bundle info = new Bundle();
                            mensaje.setTarget(mHandler);
                            info.putString("sol", "abrir");
                            mensaje.setData(info);
                            bt.cerrarBT();
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                     */
                }

                }

        }).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}