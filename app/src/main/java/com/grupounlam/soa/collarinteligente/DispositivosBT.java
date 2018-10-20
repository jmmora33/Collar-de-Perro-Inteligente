package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DispositivosBT extends AppCompatActivity {

    private static final String TAG = "DispositivosBT";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String address = "00:21:13:00:83:8C";

    public static String MENSAJE = "pepe";

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket btSocket = null;
    private ArrayAdapter<String> mPairedDevicesAA;
    private OutputStream mmOutStream = null;
    private InputStream mmInStream = null;

    public void conectar(){

        if (btSocket == null) {

            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

            device = mBtAdapter.getRemoteDevice(address);

            Log.d("DEVICE",device.toString());
            //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un btSocket
            Log.d("device","Hc-06 Conectado..");
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
            Log.d("arduino", "service conectado al arduino");
        } else {
            if (!btSocket.isConnected()) {
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
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) {
            Log.d("ERROR","No se que ha pasau");
        }

        mmOutStream = tmpOut;
        mmInStream =tmpIn;

    }

    public void cerrarBT(){

        try {
            mmInStream = null;
            mmOutStream = null;
            btSocket.close();

        } catch (IOException e) {
            Log.d("ERROR", "Error cerrando el dispositivo");
        }


    }
    public boolean isConnected(){
        if(btSocket != null)
        return btSocket.isConnected();

        return false;
    }

    public void setSocket(BluetoothSocket socket){
        btSocket = socket;
    }

    public String recibir() {
        byte[] msgBuffer = new byte[256];           //converts entered String into bytes
        int bytes;
        String readMessage = null;
        try {
            bytes = mmInStream.read(msgBuffer);
            readMessage = new String(msgBuffer, 0, bytes);
            msgBuffer = new byte[256];

        } catch (IOException e) {
        }
        return readMessage;
    }


    //write method
    public void enviar(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes

        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            Log.d("ERROR","OutPutStream Error...");
        }
    }


    private BluetoothSocket crearSocketBT(BluetoothDevice device)
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        BluetoothSocket retorno = null;
        try {
            retorno = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
        }catch(IOException e){

            finish();
        }finally{
            return retorno;
        }
        //VER SI ES NECESARIO ESTE ULTIMO RETURN
    }



}