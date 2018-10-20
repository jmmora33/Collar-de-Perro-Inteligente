package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class RecibirInformacion extends Thread {
    String cadenita;
    DispositivosBT blue;
    boolean ejecutar;


    /**
     * Constructor
     *
     * @param bt
     */
    public RecibirInformacion(DispositivosBT bt) {
        blue = bt;
        ejecutar = true;
    }

    /**
     * @param ejecutar
     */
    public void setEjecutar(boolean ejecutar) {

        this.ejecutar = ejecutar;
    }

    public boolean getEjecutar() {
        return this.ejecutar;
    }

    @Override
    public void run() {
        while (ejecutar) {
            if (!blue.isConnected()) {
                blue.conectar();
            }
            cadenita = blue.recibir();

            if (cadenita != null) {
                Log.d("CADENA RECIBIDA:", cadenita);

            }
        }
    }

}