package com.grupounlam.soa.collarinteligente;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class RecibirInformacion extends Thread {

    private DispositivosBT bt;
    private boolean ejecutar;
    private Handler mHandler;



    public RecibirInformacion(DispositivosBT blue, Handler miHandler) {
        this.bt = blue;

        mHandler = miHandler;

    }

    @Override
    public void run() {

        ejecutar = true;
        while(ejecutar){
            Message mensaje = new Message();
            mensaje.setTarget(mHandler);
            Bundle info = new Bundle();
            String cadena = bt.recibir();
            String valores[] = new String[3];
            if(cadena != null)
            {
                valores = cadena.split(",");
                Log.d("CADENA ",cadena);
                if(valores.length == 2){
                    info.putString("puerta",valores[0]);
                    info.putString("temp",valores[1]);
                }else{
                    info.putString("puerta", "SIN DATO");
                    info.putString("temp","SIN DATO");
                }


                mensaje.setData(info);
                mensaje.sendToTarget();
            }

        }
    }

    public void comenzarARecibir(){
        ejecutar = true;
        this.start();

    }

    public  void pararRecibir(){
       ejecutar = false;
    }




}