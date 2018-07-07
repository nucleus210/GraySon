package com.example.root.grayson.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.root.grayson.ActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothConnectionService extends Service {
    private static final String NAME = "BluetoothConnectionService";
    private static final String TAG = "BluetoothService";

    String mFilename;
    // Threads for managing connections
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptDataThread mAcceptDataThread;

    BluetoothAdapter mBluetoothAdapter;                     // Bluetooth adapter
    BtServiceCallbacks mServerCallBack;                     // Bluetooth service callback

    ArrayList<String>           mDeviceAddresses;
    ArrayList<AcceptDataThread> mAcceptDataThreads;
    ArrayList<ConnectedThread>  mConnThreads;
    ArrayList<BluetoothSocket>  mSockets;
    ArrayList<UUID>             mUuid;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public interface BtServiceCallbacks {void requestImageServer();}
    public void setCallbacks(BtServiceCallbacks callbacks) {mServerCallBack = callbacks;}
    @SuppressWarnings("unused")
    public BluetoothConnectionService(){super();
    }

    Context mContext;
    Handler mHandler;                                      // handler to inform UI
    UUID mDeviceUUID;

    int fileLength;                                        // uploaded file size
    byte[] mImageByteArray;                                // buffer store input stream bytes

    private int mFileState;
    private int mConnectionState;
    private int mConnectionNewState;
    public static final int STATE_NONE = 0;               // no state to a remote device
    public static final int STATE_LISTEN = 1;             // listening for incoming connections
    public static final int STATE_CONNECTING = 2;            // initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;          // connected to a remote device
    public static final int STATE_DISCONNECTED = 4;       // disconnected to a remote device
    public static final int REQUEST_DATA_SERVER = 5;       // start command data transfer
    public static final int REQUEST_IMAGE_SERVER = 6;
    public static final int FILE_LENGTH_READ = 34;         // start image data transfer
    public static final int FILE_LENGTH_NONE = 14;         // start image data transfer

    public BluetoothConnectionService(Context context, Handler handler) {
        this.mContext = context;
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnectionNewState = STATE_NONE;
        mConnectionState = STATE_NONE;
        mFileState = FILE_LENGTH_NONE;
        mAcceptDataThreads = new ArrayList<>();
        mDeviceAddresses = new ArrayList<>();
        mConnThreads = new ArrayList<>();
        mSockets = new ArrayList<>();
        mUuid = new ArrayList<>();
        // 7 randomly-generated UUIDs. These must match on both server and client.
        mUuid.add(UUID.fromString("4350c323-3451-4a87-a711-2873ed17532a"));
        mUuid.add(UUID.fromString("348220fa-22e0-48d7-b687-fd5fddea1626"));
        mUuid.add(UUID.fromString("f08ddc88-724f-4856-a36a-bf5b9b886096"));
        mUuid.add(UUID.fromString("198d9b4d-7679-45cb-8b21-3f3723f36069"));
        mUuid.add(UUID.fromString("6a4857e4-4cbf-4f56-b04d-e168bc350154"));
        mUuid.add(UUID.fromString("b9c744fb-c464-46e4-9008-e48651398126"));
        mUuid.add(UUID.fromString("9debd0a7-6a14-40a0-b58c-dbd3aab8bb50"));
        initialize();           // Initialize Bluetooth adapter
        startBtService();       // Start bluetooth Server Socket
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        BluetoothConnectionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothConnectionService.this;
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Callback method from Activity interface
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Start the Bluetooth service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
//------------------------------------------------------------------------------------------------//
    private synchronized void startBtService() {
        Log.d(TAG, "Bluetooth Service: Starting.");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptDataThread != null) {mAcceptDataThread.cancel(); mAcceptDataThread = null;}

        // Start new server thread in listening state
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Start the ConnectedThread or AcceptDataThread to begin managing a Bluetooth connection
     *
     * @param socket      The BluetoothSocket on which the connection was made
     * @param device      The BluetoothDevice that has been connected
     * @param requestCode The request code that handle to start correct
     *                    Thread (Image Server or Remote Controls)
     */
//------------------------------------------------------------------------------------------------//
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int requestCode) {
        Log.d(TAG, "connected, Socket Type:");

        // Cancel the thread that completed the connection
        if (mConnectionNewState == STATE_DISCONNECTED) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        if (mConnectionNewState == STATE_DISCONNECTED) {
            if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
        }

        if (requestCode == REQUEST_DATA_SERVER) {
            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.setName("Remote Control Thread");
            mConnectedThread.start();
            Log.d(TAG, "Action service is connecting to Device name: " + device);
            setState(STATE_CONNECTED);
        }

        if (requestCode == REQUEST_IMAGE_SERVER) {
            mAcceptDataThread = new AcceptDataThread(socket);
            mAcceptDataThread.setName("Data Thread");
            mAcceptDataThread.start();
            mAcceptDataThreads.add(mAcceptDataThread);
            Log.d(TAG, "Data service is connecting to Device name: " + device);
            setNewState(STATE_NONE);
            setState(STATE_CONNECTED);
        }
        if (requestCode == STATE_CONNECTING) {
            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.setName("Remote Control Thread");
            mConnectedThread.start();
            mConnThreads.add(mConnectedThread);
            Log.d(TAG, "Data service is connecting to Device name: " + device);
            setState(STATE_CONNECTED);
        }

        if (mConnectionState == STATE_DISCONNECTED) {
            // Cancel the accept thread because we only want to connect to one device
             if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null; }
        }
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Stop all threads
     */
//------------------------------------------------------------------------------------------------//
    public synchronized void stop() {
        Log.d(TAG, "Threads stop");

        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptDataThread != null) {mAcceptDataThread.cancel(); mAcceptDataThread = null;}

        setState(STATE_NONE);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * AcceptThread starts and sits waiting for a connection.
     * ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     *
     * @param device      Bluetooth device name
     * @param uuid        UUID secure code
     * @param requestCode Request state code
     **/
//------------------------------------------------------------------------------------------------//
    public void startClient(BluetoothDevice device, UUID uuid, int requestCode) {
        Log.d(TAG, "startClient: Started.");
        mConnectThread = new ConnectThread(device, uuid, requestCode);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * AcceptThread starts and sits waiting for a connection.
     * ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     *
     * @param device      Bluetooth device name
     * @param uuid        UUID secure code
     * @param requestCode Request state code
     **/
//------------------------------------------------------------------------------------------------//
    public void startServer(BluetoothDevice device, UUID uuid, int requestCode) {
        Log.d(TAG, "startServer: Started.");
        mConnectThread = new ConnectThread(device, uuid, requestCode);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Set the current state of the message connection
     *
     * @param state An integer defining the current connection state
     */
//------------------------------------------------------------------------------------------------//
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mConnectionState + " -> " + state);
        mConnectionState = state;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Get connection state
     */
//------------------------------------------------------------------------------------------------//
    public synchronized int getState() {
        return mConnectionState;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Set the current state of the File operations. States are used to control type of messages
     *
     *
     * @param state An constant defining the current connection state
     */
//------------------------------------------------------------------------------------------------//
    private synchronized void setFileState(int state) {
        Log.d(TAG, "setState() " + mFileState + " -> " + state);
        mFileState = state;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Set the current state of the data connection.
     *
     * @param state An constant defining the current connection state
     */
//------------------------------------------------------------------------------------------------//
    public synchronized void setNewState(int state) {
        Log.d(TAG, "setState() " + mConnectionNewState + " -> " + state);
        mConnectionNewState = state;
    }

//------------------------------------------------------------------------------------------------//

    /**
     * AcceptThread server wait for incoming connections
     **/
//------------------------------------------------------------------------------------------------//
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket = null;

        AcceptThread() {
        }
        public void run() {
           Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket;
            try {
                // Listen for all 7 UUIDs
                for (int i = 0; i < 7; i++) {
                    mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, mUuid.get(i));
                    socket = mmServerSocket.accept();

                    if (socket != null) {
                        // A connection was accepted. Perform work associated with
                        // the connection in a separate thread.
                        String address = socket.getRemoteDevice().getAddress();
                        mSockets.add(socket);
                        mDeviceAddresses.add(address);
                        setState(STATE_CONNECTING);
                        manageMyConnectedSocket(socket, socket.getRemoteDevice(), mConnectionState);
                    }
                }
                Log.i(TAG, "AcceptThread: END mAcceptThread ");
            } catch (IOException e) {
                e.printStackTrace();
                connectionLost();
            }
        }

        // Closes the connect socket and causes the thread to finish.
        void cancel() {
            Log.d(TAG, "AcceptThread: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: Close of AcceptThread ServerSocket failed. "
                        + e.getMessage());
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * This is connect tread where we create socket and RFCOMM connection.
     */
//------------------------------------------------------------------------------------------------//
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device, UUID uuid, int mRequestState) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.d(TAG, "ConnectThread: started.");
            mDeviceUUID = uuid;
            mConnectionState = mRequestState;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            try {
                mBluetoothAdapter.cancelDiscovery();
            } catch (NullPointerException w) {
                Log.e(TAG, "adapter is not visible", w);
            }
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                mmSocket.connect();
                Log.d(TAG, "ConnectThread: Client socket created.");

            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                if (mDeviceUUID.toString().contentEquals(mUuid.get(6).toString())) {
                    connectionLost();
                }
                Log.e(TAG, "ConnectThread: Could not connect the client socket", connectException);

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "ConnectThread: Could not close the client socket", closeException);
                }
                BluetoothConnectionService.this.startBtService();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionService.this) {mConnectThread = null;}

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            if (mConnectionNewState != REQUEST_IMAGE_SERVER) {
                manageMyConnectedSocket(mmSocket, mmSocket.getRemoteDevice(), mConnectionState);
            } else {
                manageMyConnectedSocket(mmSocket, mmSocket.getRemoteDevice(), mConnectionNewState);
            }
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                mmSocket.close();
                Log.d(TAG, "ConnectThread: Client socket closed.");
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not close the client socket" + e.getMessage());
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Create data transfer socket to transmit data over bluetooth connection. This thread is used
     * for string command from phone to android app.
     */
//------------------------------------------------------------------------------------------------//
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String device;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");
            device = socket.getRemoteDevice().getName();
            Log.d(TAG, "ConnectedThread: Connected to " + device);

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "ConnectedThread InputStream: " + incomingMessage);

                    if (serverTasks(incomingMessage)) {
                        sendAction(incomingMessage);
                        ActionMenu m1 = ActionMenu.valueOf(incomingMessage);
                        switch (m1) {
                            case UPLOAD_IMAGE:
                                if (mServerCallBack != null) {
                                    mServerCallBack.requestImageServer();
                                    setState(STATE_CONNECTED);
                                    setNewState(REQUEST_IMAGE_SERVER);
                                } else {
                                    byte[] bytesOut = incomingMessage.getBytes(Charset.defaultCharset());
                                    writeMessage(bytesOut);
                                    setState(STATE_CONNECTED);
                                    setNewState(REQUEST_IMAGE_SERVER);
                                   // setFileState(FILE_LENGTH_READ);
                                }
                                break;

                            case FILE_OPERATION:
                                if (mServerCallBack != null) {
                                    setFileState(FILE_LENGTH_READ);
                                    String message =ActionMenu.FILE_OPERATION.name();
                                    byte[] bytesOut = message.getBytes(Charset.defaultCharset());
                                    writeMessage(bytesOut);
                                }
                                break;
                        }
                    }else{
                        try {
                            fileLength = Integer.parseInt(incomingMessage);
                        } catch (Exception e) {
                            Log.d(TAG, "Error", e);
                        }
                        Log.e(TAG, "ConnectedThread read: File length is: " + fileLength);
                        byte[] bytesOut = incomingMessage.getBytes(Charset.defaultCharset());
                        writeMessage(bytesOut);
                        setFileState(FILE_LENGTH_NONE);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "ConnectedThread read: Error reading Input Stream. "
                            + e.getMessage());
                    connectionLost();
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "ConnectedThread write: Writing to output stream: " + text);
            try {
                mmOutStream.write(bytes);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, bytes)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread write: Error writing to output stream. " + e.getMessage());
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                mmSocket.close();
                Log.d(TAG, "ConnectedThread: Client socket closed.");
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Could not close the connect socket", e);
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Create data transfer socket to transmit data over bluetooth connection. This thread is used
     * for sharing photos between devices.
     */
//------------------------------------------------------------------------------------------------//
    private class AcceptDataThread extends Thread {
        int arrayLength;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        AcceptDataThread(BluetoothSocket socket) {
            Log.d(TAG, "AcceptData: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                String device = mmSocket.getRemoteDevice().getName();
                Log.d(TAG, "AcceptData: Trying to connect to device " + device);
            } catch (NullPointerException e) {
                Log.e(TAG, "AcceptData: Error getting remote device name. " + e.getMessage());
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // buffer object to store incoming data and then transfer into byte Array
            try {
                int bytes;                          // bytes returned from read()
                int bytesCounter = 0;               // transfer data counter
                byte[] buffer = new byte[1024];     // buffer store for the stream
                arrayLength = fileLength;           // check array length after complete

                ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();

                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        bytes = mmInStream.read(buffer);               // read inputStream
                        bufferOut.write(buffer, 0, bytes);        // write to buffer object
                        bytesCounter += bytes;                       // increment uploaded data
                        mImageByteArray = bufferOut.toByteArray();  // incoming bytes to byte array
                        int i = mImageByteArray.length;            // convert file length to int
                        Log.d(TAG, "AcceptData InputStream file Length: " + i);
                    } catch (NullPointerException w) {
                        Log.d(TAG, "AcceptData read: Error reading Input Stream. "
                                + w.getMessage());
                    } finally {
                        if (bytesCounter == fileLength) {
                            Bitmap bMap = null;
                            String name = null;
                            try {
                                name = fileNameGenerator();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                bMap = BitmapFactory.decodeByteArray(mImageByteArray, 0,
                                        mImageByteArray.length);
                            } catch (NullPointerException w) {
                                Log.d(TAG, "AcceptData write: NuLL: ");
                            }
                            if(bMap!=null) {
                                saveBitMap(mContext, bMap, name);
                            }
                            Log.d(TAG, "AcceptData read: Data transfer complete. ");
                            try {
                                bufferOut.reset();
                                bufferOut.close();
                                mImageByteArray = null;
                                sendAction(ActionMenu.BT_DOWNLOAD.name());
                                mFilename = name;
                                Log.d(TAG, "AcceptData write: Reset Input Stream. ");
                                fileLength = 0;
                                bytesCounter = 0;
                            } catch (IOException e) {
                                Log.e(TAG, "AcceptData write: Error reset Input Stream. "
                                        + e.getMessage());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "AcceptData write: Error reset Input Stream. " + e.getMessage());
                connectionLost();
            }
        }

        //Call this from the main activity to send data to the remote device
        void write(byte[] bytes) {
            //String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "AcceptData write: Writing to output stream: ");
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "AcceptData write: Error writing to output stream. "
                        + e.getMessage());
                connectionLost();
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                mmSocket.close();
                Log.d(TAG, "AcceptData: Client socket closed.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptData: Could not close the connect data socket", e);
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Write to the ConnectedThread in an un synchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
//------------------------------------------------------------------------------------------------//
    public void writeMessage(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "ConnectedThread write: Write Called.");
        //perform the write
        if (out != null && mConnectedThread != null) {
            mConnectedThread.write(out);
        } else {
            Log.d(TAG, "ConnectedThread write: Connection Thread is empty.");
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Write to the AcceptDataThread in an un synchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    public void imageWrite(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "AcceptData write: Write Called.");
        //perform the write
        if (out != null && mAcceptDataThread != null) {
            mAcceptDataThread.write(out);
        } else {
            Log.d(TAG, "AcceptData write: Connection Thread is empty.");
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Start ConnectedThread
     *
     * @param mmSocket The bytes to write
     */
//------------------------------------------------------------------------------------------------//
    private void manageMyConnectedSocket(BluetoothSocket mmSocket, BluetoothDevice mDevice,
                                         int mState) {
        if (mmSocket.isConnected()) {
            synchronized (BluetoothConnectionService.this) {
                switch (mState) {

                    case STATE_NONE:
                    case STATE_LISTEN:
                    case STATE_CONNECTING:
                        // Situation normal. Start the connected thread.
                        if (mConnectionNewState != REQUEST_IMAGE_SERVER) {
                            connected(mmSocket, mDevice, mState);
                        } else {
                            connected(mmSocket, mDevice, mConnectionNewState);
                        }
                        break;
                    case STATE_CONNECTED:

                    case STATE_DISCONNECTED:

                    case REQUEST_DATA_SERVER:
                        mConnectedThread = new ConnectedThread(mmSocket);
                        mConnectedThread.start();
                        setState(REQUEST_DATA_SERVER);
                        break;

                    case REQUEST_IMAGE_SERVER:
                        mAcceptDataThread = new AcceptDataThread(mmSocket);
                        mAcceptDataThread.start();
                        setState(REQUEST_IMAGE_SERVER);
                        break;
                }
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * File name generator. Method is used when Image is upload from user to Server.
     */
//------------------------------------------------------------------------------------------------//
    private String fileNameGenerator() throws IOException {
        return File.createTempFile("img", ".png",
                Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_PICTURES)).toString();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Save image to externalPublicStorage from bitmap.
     *
     * @param context get default context
     * @param bmp  Bitmap object
     * @param name Filename for new file
     *
     */
//------------------------------------------------------------------------------------------------//
    public void saveBitMap(Context context, Bitmap bmp, String name) throws IOException {
        File file = new File(name);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (FileOutputStream fo = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 0, bytes);
            file.createNewFile();
            fo.write(bytes.toByteArray());
        } catch (NullPointerException w) {
            Log.d(TAG, "NuLL", w);
        }
        try {
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            String stringUri;
                            stringUri = uri.toString();
                            sendFilePath(stringUri);
                        }
                    });

        } catch (NullPointerException w) {
            Log.d("Media Scanner: NuLL", String.valueOf(w));
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Initializes a reference to the local Bluetooth adapter.
     */
//------------------------------------------------------------------------------------------------//
    private void initialize() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnectionState = STATE_NONE;
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Asynchronous call to Broadcast given intent to all interested BroadcastReceivers
     * Broadcast server request to  {@link BluetoothActivityFragment}
     * Broadcast receiver call user Remote actions and call methods inside Main Activity
     */
//------------------------------------------------------------------------------------------------//
    private void sendAction(String mPos) {
        Log.d("service sender", "Broadcasting action message");
        Intent intent = new Intent("serviceAction");
        // You can also include some extra data.
        intent.putExtra("service action", mPos);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Asynchronous call to Broadcast given intent to all interested BroadcastReceivers
     * Broadcast server request to {@link com.example.root.grayson.GraySonMainActivity}
     * Broadcast message with file path
     */
//------------------------------------------------------------------------------------------------//
    private void sendFilePath(String uri) {
        Log.d("service sender", "Broadcasting action message");
        Intent intent = new Intent("serviceActionFile");
        // You can also include some extra data.
        intent.putExtra("service uri", uri);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Generate random number
     *
     * @param requestTask  Server task requested. Look in enum menu if have no match switch to
     *                     file operation mode
     */
//------------------------------------------------------------------------------------------------//
    public static boolean serverTasks(String requestTask) {
        for (ActionMenu actionMenu : ActionMenu.values()) {
            if (requestTask.contentEquals(ActionMenu.valueOf(actionMenu.name()).name()))
                return true;
        }
        return false;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Restore service in STATE_LISTEN
     */
//------------------------------------------------------------------------------------------------//
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothConnectionService.this.startBtService();

        setState(STATE_NONE);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Update UI title according to the current state of the chat connection
     */
//------------------------------------------------------------------------------------------------//
    private synchronized void updateUserInterfaceTitle() {
        mConnectionState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() "
                + mConnectionNewState
                + " -> "
                + mConnectionState);
        mConnectionNewState = mConnectionState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mConnectionNewState, -1)
                .sendToTarget();
    }
}
