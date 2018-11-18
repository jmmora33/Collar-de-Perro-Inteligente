package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class DispositivosBT  extends AppCompatActivity {

    private static final String TAG = "DispositivosBT";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothAdapter mBtAdapter ;
    private BluetoothDevice device ;
    private BluetoothSocket btSocket;
    private OutputStream mmOutStream;
    private InputStream mmInStream;



    public DispositivosBT(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        device = null;
        btSocket = null;
        mmOutStream = null;
        mmInStream = null;
    }

    public synchronized void conectar(String mac){



                if(!mBtAdapter.enable()){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent,1);

                }
    synchronized (this) {

            device = mBtAdapter.getRemoteDevice(mac);

            if (device == null) {
                Log.d(TAG, "No se pudo vincular con el dispositivo");
            } else {
                Log.d(TAG, device.toString() + " HC-06");
                btSocket = crearSocketBT(device);
                // Establish the Bluetooth btSocket connection.
                try {
                    btSocket.connect();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e2) {
                        //insert code to deal with this
                    }
                }
            }

            if (btSocket.isConnected()) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                //Create I/O streams for connection
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "ERROR" + "No se que ha pasau");
            }

            mmOutStream = tmpOut;
            mmInStream = tmpIn;
            }else{

                try {
                    btSocket.close();
                    btSocket = null;
                } catch (IOException e2) {
                    //insert code to deal with this
                }
            }
    }
    }





    public void cerrarBT(){

        try {
            mmInStream = null;
            mmOutStream = null;

            if(btSocket != null && btSocket.isConnected())
                btSocket.close();
                btSocket = null;
            } catch (IOException e) {
                Log.d(TAG, "Error cerrando el dispositivo");
            }


    }
    public boolean isConnected(){

        return btSocket != null && btSocket.isConnected();
    }



    public String recibir() {
        byte[] msgBuffer = new byte[256];           //converts entered String into bytes
        int bytes;
        String readMessage = null;
        try {
            bytes = mmInStream.read(msgBuffer);
            readMessage = new String(msgBuffer, 0, bytes);
        } catch (IOException e) {
            Log.d(TAG,"Error al recibir dato...");
            return "ERROR";
        }
        return readMessage;
    }


    //write method
    public void enviar(final String input) {

        new Thread(new Runnable() {
            public void run() {
                byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                try {
                        mmOutStream.write(msgBuffer);
                } catch (IOException e) {
                    Log.d("ERROR", "Error al enviar dato...");
                }
            }
        }).start();
    }

    /**
     *
     crea un conexion de salida segura para el dispositivo
     usando el servicio UUID
     * @param device Arduino
     * @return retorno
     */
    private BluetoothSocket crearSocketBT(BluetoothDevice device)
    {
        BluetoothSocket retorno = null;


        try {

            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            retorno = (BluetoothSocket) m.invoke(device, 1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return retorno;

    }

    public BluetoothAdapter getmBtAdapter() {
        return mBtAdapter;
    }
}