package com.msmaker.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.divyanshu.colorseekbar.ColorSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnConect, btnLed1, btnEnviar, btnEnviar2, btnEnviar3;
    EditText edtTextDados, edtTextDados2;
    TextView textColor, textColor2, textColor3;
    ColorSeekBar colorSeekBar;
    View view;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;


    ConnectedThread connectedThread;

    Handler handler;
    StringBuilder dadosBluetooth = new StringBuilder();

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;


    boolean conexao = false;
    int setCor;
    private static String MAC = null;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btnConect = findViewById(R.id.btnConect);
        btnLed1 = findViewById(R.id.btnLed1);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar2 = findViewById(R.id.btnEnviar2);
        btnEnviar3 = findViewById(R.id.btnEnviar3);
        edtTextDados = findViewById(R.id.edtTextDados);
        edtTextDados2 = findViewById(R.id.edtTextDados2);
        colorSeekBar = findViewById(R.id.colorSeekBar);
        textColor = findViewById(R.id.txtColor);
        textColor2 = findViewById(R.id.txtColor2);
        textColor3 = findViewById(R.id.txtColor3);
        view = findViewById(R.id.view);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        btnConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    try {
                        meuSocket.close();
                        conexao = false;
                        btnConect.setText("Conectar");
                        Toast.makeText(getApplicationContext(), "Bluetooth foi desconectado", Toast.LENGTH_LONG).show();

                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro" + erro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXAO);
                }
            }
        });

        btnLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.write("led1");
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetoothnão está conectado", Toast.LENGTH_LONG).show();
                }
            }
        });


        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    int valor = Integer.parseInt(edtTextDados.getText().toString(), 16);
                    String cor = String.valueOf(valor);
                    textColor2.setText(String.valueOf(cor));
                    connectedThread.write(cor);
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnEnviar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.write(String.valueOf(setCor));
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnEnviar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.write(String.valueOf(setCor));
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }
            }
        });


        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                view.setBackgroundColor(i);
                setCor = i + 16777216;
                textColor.setText(String.valueOf(setCor));
                textColor3.setText(String.valueOf(i));

                if (conexao) {
                    //connectedThread.write( String.valueOf(setCor));

                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
                }

            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String recebidos = (String) msg.obj;
                    dadosBluetooth.append(recebidos);

                    int fimInformacao = dadosBluetooth.indexOf("}");

                    if (fimInformacao > 0) {

                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);

                        int tamInformação = dadosCompletos.length();

                        if (dadosBluetooth.charAt(0) == '{') {

                            String dadosFinais = dadosBluetooth.substring(1, tamInformação);
                            Log.d("Recebidos", dadosFinais);

                            if (dadosFinais.contains("l1on")) {
                                btnLed1.setText("LED 1 LIGADO");
                            } else if (dadosFinais.contains("l1of")) {
                                btnLed1.setText("LED 1 DESLIGADO");
                            }

                        }

                        dadosBluetooth.delete(0, dadosBluetooth.length());


                    }
                }
            }
        };


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não ativado, o app será encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case SOLICITA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                    meuDevice = bluetoothAdapter.getRemoteDevice(MAC);
                    //Toast.makeText(getApplicationContext(), "MAC final " + MAC, Toast.LENGTH_LONG).show();
                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);

                        meuSocket.connect();

                        conexao = true;

                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();

                        btnConect.setText("Desconectar");

                        Toast.makeText(getApplicationContext(), "Você foi conectado com:  " + MAC, Toast.LENGTH_LONG).show();

                    } catch (IOException erro) {

                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro " + erro, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                // Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //  Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    //Message readMsg =
                    String dadosBt = new String(mmBuffer, 0, numBytes);
                    handler.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBt).sendToTarget();
                    // readMsg.sendToTarget();
                } catch (IOException e) {
                    // Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();

            try {
                mmOutStream.write(msgBuffer);

                // Share the sent message with the UI activity.
                // Message writtenMsg = handler.obtainMessage(
                //         MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                //  writtenMsg.sendToTarget();
            } catch (IOException e) {
               /* Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);*/


            }


        }
    }
}
