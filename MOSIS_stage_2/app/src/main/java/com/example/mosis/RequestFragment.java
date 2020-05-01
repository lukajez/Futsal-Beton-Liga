package com.example.mosis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    //region VIEWS
    TextView txtStatusRequest, txtUsernameRequest, txtHintListen, txtConStatus, txtConStatusExp, txtRequestInfo, txtConnectionRQ, txtConnectionRQExp, btnAddFriendRequest, btnBackRequest;
    Button btnAcc, btnDec, btnListen;
    CircleImageView profileImage;
    View view;
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

    //region FIREBASE
    FirebaseUser user;
    FirebaseAuth userAuth;
    FirebaseFirestore db;
    //endregion

    //region OTHER
    BluetoothAdapter myBluetoothAdapter;
    SendReceive sendReceive;
    String statusListener;
    String uuid, recUuid;
    boolean btAdapterDiscovering;
    //endregion

    public RequestFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_requests, container, false);

        //region PICK VIEWS
        profileImage = (CircleImageView) view.findViewById(R.id.cimgProfileRequest);
        txtStatusRequest = (TextView) view.findViewById(R.id.txtStatusRequest);
        txtUsernameRequest = (TextView) view.findViewById(R.id.txtUsernameRequest);
        btnAcc = (Button) view.findViewById(R.id.btnAcceptRequest);
        btnDec = (Button) view.findViewById(R.id.btnDeclineRequest);
        btnListen = (Button) view.findViewById(R.id.btnListenRequst);
        //endregion

        //region SET FIREBSE AND OTHER
        user = FirebaseAuth.getInstance().getCurrentUser();
        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        myBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        btAdapterDiscovering = myBluetoothAdapter.isDiscovering();
        uuid = user.getUid();
        //endregion

        //region ON BUTTON CLICK LISTENERS
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : method listenForRequest

                if(!btAdapterDiscovering) {
                    Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivity(discoverable);
                }

                listenForRequest();

            }
        });

        btnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : method acceptRequest
                acceptRequest();
            }
        });

        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : method declineRequest
            }
        });
        //endregion

        setUpFont();
        return view;
    }

    //region METHODS
    private  void listenForRequest() {
        ServerClass serverClass = new ServerClass();
        serverClass.start();
    }

    private  void acceptRequest() {

        db = FirebaseFirestore.getInstance();

        db.collection("users").document(uuid).update("friends", FieldValue.arrayUnion(recUuid)).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                db.collection("users").document(recUuid).update("friends", FieldValue.arrayUnion(uuid)).addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(getContext(), "You are friends!", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

    }

    private  void declineRequest() {

    }

    protected void setUpFont(){

        //region PickUpViews
        txtStatusRequest = (TextView) view.findViewById(R.id.txtStatusRequest);
        txtUsernameRequest = (TextView) view.findViewById(R.id.txtUsernameRequest);
        btnAcc = (Button) view.findViewById(R.id.btnAcceptRequest);
        btnDec = (Button) view.findViewById(R.id.btnDeclineRequest);
        btnListen = (Button) view.findViewById(R.id.btnListenRequst);
        txtHintListen = (TextView) view.findViewById(R.id.txtHintListen);
        txtConnectionRQ = (TextView) view.findViewById(R.id.txtConnectionRQ);
        txtConnectionRQExp = (TextView) view.findViewById(R.id.txtConnectionRQExp);
        txtConStatus = (TextView) view.findViewById(R.id.txtConStatus);
        txtConStatusExp = (TextView) view.findViewById(R.id.txtConStatusExp);
        txtRequestInfo = (TextView) view.findViewById(R.id.txtRequestInfo);
       // btnAddFriendRequest = (TextView) view.findViewById(R.id.btnAddFriendRequest);
        //endregion

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        txtStatusRequest.setTypeface(typeface);
        btnAcc.setTypeface(typeface);
        btnDec.setTypeface(typeface);
        btnListen.setTypeface(typeface);
        txtHintListen.setTypeface(typeface);
        txtConnectionRQ.setTypeface(typeface);
        txtConStatus.setTypeface(typeface);
        txtRequestInfo.setTypeface(typeface);
       // btnAddFriendRequest.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");
        txtUsernameRequest.setTypeface(typeface2);
        txtConnectionRQExp.setTypeface(typeface2);
        txtConStatusExp.setTypeface(typeface2);


        //endregion
    }
    //endregion

    //region HANDLER
    Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    txtStatusRequest.setText("Listening..");
                    txtStatusRequest.setTextColor(Color.WHITE);
                    statusListener = "Listening";
                    break;
                case STATE_CONNECTING:
                    txtStatusRequest.setText("Connecting..");
                    txtStatusRequest.setTextColor(Color.WHITE);
                    statusListener = "Connecting";
                    break;
                case STATE_CONNECTED:
                    txtStatusRequest.setText("Connected Successfully!");
                    txtStatusRequest.setTextColor(Color.GREEN);
                    statusListener = "Connected Successfully!";
                    break;
                case STATE_CONNECTION_FAILED:
                    txtStatusRequest.setText("Connection failed!");
                    txtStatusRequest.setTextColor(Color.RED);
                    statusListener = "Connection failed";
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String pathAsMessage = new String(readBuff,0,msg.arg1);
                    recUuid = pathAsMessage;

                    db = FirebaseFirestore.getInstance();
                   //FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

                    //set path to user, document name is userID, so received message is path to concrete user
                    db.collection("users").document(recUuid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            txtUsernameRequest.setText((CharSequence) documentSnapshot.get("username"));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "Failure, beacause of: " + e);
                        }
                    });

                    break;

            }
            return true;
        }
    });
    //endregion

    //region Server, CLient, SentRecieve
    private class ServerClass extends Thread
    {
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
                    message.what = STATE_LISTENING;
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

    private class ClientClass extends Thread
    {
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

    private class SendReceive extends Thread
    {
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
}
