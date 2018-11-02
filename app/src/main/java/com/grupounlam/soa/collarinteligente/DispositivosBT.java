package com.grupounlam.soa.collarinteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DispositivosBT  extends AppCompatActivity {

    private static final String TAG = "DispositivosBT";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String ADDRESS = "00:21:13:00:83:8C";

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket btSocket = null;
    private OutputStream mmOutStream = null;
    private InputStream mmInStream = null;

    private boolean iniciar;
    private String cadenita;

    public void conectar(){

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                if(!mBtAdapter.enable()){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent,1);
                }

                if (btSocket == null) {
                    device = mBtAdapter.getRemoteDevice(ADDRESS);

                    if(device == null){
                        Log.d("DEVICE","No se pudo vincular con el dispositivo");
                    }else {
                        Log.d("DEVICE", device.toString() + " HC-06");
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
                    }


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
                if(btSocket.isConnected())
                {
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

            }





    public void cerrarBT(){

        try {
            mmInStream = null;
            mmOutStream = null;

            if(btSocket != null && btSocket.isConnected())
                btSocket.close();

            iniciar = false;
            } catch (IOException e) {
                Log.d("ERROR", "Error cerrando el dispositivo");
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
            Log.d("ERROR","Error al recibir dato...");

        }
        return readMessage;
    }


    //write method
    public void enviar(final String input) {

        new Thread(new Runnable() {
            public void run() {
                byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                try {
                    mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
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
        mBtAdapter.cancelDiscovery();
        try {
            retorno = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }catch(IOException e){
            Log.d("Socket","Error en la vinculacion con Arduino.");
        }
            return retorno;

    }



}