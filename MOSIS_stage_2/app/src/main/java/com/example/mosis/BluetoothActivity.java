package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity<DeviceItem> extends AppCompatActivity {

    //region VIEWS
    Button showBtn, btnBack, buttonOn, buttonOff, btnSendRequest;
    ListView listView;
    TextView txtStatus, txtAddFr, txtConn, txt_Request_AB, txt_Request_AB_Exp, txt_Scan_AB, txt_Scan_AB_Exp, txt_ConnentionExp;
    //endregion

    //region STATES
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    public static int REQUEST_ENABLE_BLUETOOTH = 1;
    static final UUID MY_UUID = UUID.fromString("2b20910c-ad49-494e-85a9-b29c76096a68");
    private static final String APP_NAME = "MOSIS";
    //endregion

    //region REST
    Intent btEnablingIntent;
    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    DevicesListAdapter devicesListAdapter;
    ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    SendReceive sendReceive;
    String statusListener;
    String uuid;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //region VIEWS PICKING AND SET UPS
        buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOff = (Button) findViewById(R.id.buttonOff);
        showBtn = (Button) findViewById(R.id.btn_Show);
        listView = (ListView) findViewById(R.id.list_paired);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        btnSendRequest = (Button) findViewById(R.id.btn_SendRequest);

        btDevices = new ArrayList<>();
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //endregion

        //region BLUETOOTH ADAPTER BTN_ON BTN_OFF
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothONMethod();
            }
        });

        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOFFMethod();
            }
        });

        //endregion

        //region SCANNING BUTTON
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "SHOW BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                btDevices.clear();
                if(myBluetoothAdapter.isEnabled()) {
                    scanDevices();
                } else {
                    Toast.makeText(getApplicationContext(), "Your bluetooth is off, please turn it on", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //endregion

        //region LIST VIEW
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clClass = new ClientClass(btDevices.get(position));
                clClass.start();
            }
        });
        //endregion

        //region SEND REQUEST
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusListener == "Connected Successfully!") {
                    sendReceive.write(uuid.getBytes());
                }
            }
        });
        //endregion

        setUpFont();
    }

    //region RECEIVER
    private BroadcastReceiver reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Toast.makeText(getApplicationContext(), "Finding..", Toast.LENGTH_SHORT).show();

            if (action.equals(BluetoothDevice.ACTION_FOUND))
            {
                Toast.makeText(getApplicationContext(), "Founded", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!btDevices.contains(device))
                    btDevices.add(device);
                devicesListAdapter = new DevicesListAdapter(context, R.layout.devices_layout, btDevices);
                listView.setAdapter(devicesListAdapter);

                Toast.makeText(getApplicationContext(), "Device" + " " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    //endregion

    //region HANDLER
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    txtStatus.setText("Listening");
                    txtStatus.setTextColor(Color.WHITE);
                    statusListener = "Listening";
                    break;
                case STATE_CONNECTING:
                    txtStatus.setText("Connecting");
                    txtStatus.setTextColor(Color.WHITE);
                    statusListener = "Connecting";
                    break;
                case STATE_CONNECTED:
                    txtStatus.setText("Connected Successfully!");
                    txtStatus.setTextColor(Color.GREEN);
                    statusListener = "Connected Successfully!";
                    break;
                case STATE_CONNECTION_FAILED:
                    txtStatus.setText("Connection failed");
                    txtStatus.setTextColor(Color.RED);
                    statusListener = "Connection failed";
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMSg = new String(readBuff,0,msg.arg1);
                    Toast.makeText(getApplicationContext(), tempMSg, Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    });
    //endregion

    //region SERVER, CLIENT, SEND_RECEIVE
    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass()
        {
            try
            {
                serverSocket = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket = null;

            while(socket == null)
            {

                try
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }

                if(socket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1)
        {
            device = device1;

            try
            {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try
            {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e)
            {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try
            {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true)
            {
                try
                {
                    bytes =  inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes)
        {
            try
            {
                outputStream.write(bytes);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
    //endregion

    //region Methods
    private void scanDevices() {

        if(myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
            checkPremissions();
            myBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevs = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(reciever, discoverDevs);
        }

        if(!myBluetoothAdapter.isDiscovering()) {
            checkPremissions();
            myBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevs = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(reciever, discoverDevs);
        }
    }

    private void checkPremissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            Toast.makeText(getApplicationContext(), "Checking..", Toast.LENGTH_SHORT).show();
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    private void showPairedDevices() {
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                int index = 0;

                if(bt.size() > 0) {
                    for(BluetoothDevice device:bt) {
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });
    }

    private void bluetoothOFFMethod() {
        if(myBluetoothAdapter.isEnabled()) {
            myBluetoothAdapter.disable();
            buttonOff.setVisibility(View.GONE);
            buttonOn.setVisibility(View.VISIBLE);
        }
    }

    private void bluetoothONMethod() {
        if(myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this devise", Toast.LENGTH_SHORT).show();
        } else if(!myBluetoothAdapter.isEnabled()){
            buttonOn.setVisibility(View.GONE);
            buttonOff.setVisibility(View.VISIBLE);
            startActivityForResult(btEnablingIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    protected void setUpFont(){

        //region PickUpViews
        buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOff = (Button) findViewById(R.id.buttonOff);
        showBtn = (Button) findViewById(R.id.btn_Show);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        btnBack = (Button) findViewById(R.id.btn_Back_BlueT);
        txtAddFr = (TextView) findViewById(R.id.txt_Add_Friend);
        txtConn = (TextView) findViewById(R.id.txt_Connention);
        btnSendRequest = (Button) findViewById(R.id.btn_SendRequest);
        txt_Request_AB = (TextView) findViewById(R.id.txt_Request_AB);
        txt_Request_AB_Exp = (TextView) findViewById(R.id.txt_Request_AB_Exp);
        txt_Scan_AB = (TextView) findViewById(R.id.txt_Scan_AB);
        txt_Scan_AB_Exp = (TextView) findViewById(R.id.txt_Scan_AB_Exp);
        txt_ConnentionExp = (TextView) findViewById(R.id.txt_ConnentionExp);
        //endregion

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        buttonOn.setTypeface(typeface);
        buttonOff.setTypeface(typeface);
        showBtn.setTypeface(typeface);
        btnBack.setTypeface(typeface);
        txtStatus.setTypeface(typeface);
        txtAddFr.setTypeface(typeface);
        txtConn.setTypeface(typeface);
        btnSendRequest.setTypeface(typeface);
        txt_Request_AB.setTypeface(typeface);
        txt_Scan_AB.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
        txt_ConnentionExp.setTypeface(typeface2);
        txt_Request_AB_Exp.setTypeface(typeface2);
        txt_Scan_AB_Exp.setTypeface(typeface2);
        //endregion
    }
    //endregion
}
