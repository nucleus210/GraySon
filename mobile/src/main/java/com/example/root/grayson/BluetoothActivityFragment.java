package com.example.root.grayson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.grayson.adapters.DeviceListAdapter;
import com.example.root.grayson.services.BluetoothConnectionService;
import com.example.root.grayson.services.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.ContentValues.TAG;

public class BluetoothActivityFragment extends Fragment implements View.OnClickListener, Constants {

    private static final String ADAPTER_FRIENDLY_NAME = "Grayson device";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    @SuppressWarnings("unused")
    private static final String ARG_PARAM1 = "param1";
    @SuppressWarnings("unused")
    private static final String ARG_PARAM2 = "param2";
    private static final int ACTION_REQUEST_MULTIPLE_PERMISSION = 202;

    private byte[] mImageArray;
    boolean mBound = false;
    Context mContext;
    ListView mListBtDevices;
    ImageView mImageContainer;
    FrameLayout mFragmentContainer;
    SharedViewModel sharedViewModel;
    ProgressDialog progressDialog;
    int mSelectedPosition;                              // Selected device position from List
    private OnFragmentInteractionListener mListener;

    // Bluetooth constants
    private static final String LOG_TAG = "Bluetooth service";

    private static final int REQUEST_ENABLE_BT = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int DISCOVERABLE_TIMEOUT_MS = 102;
    private static final int REQUEST_ENABLE_BLUETOOTH = 103;

    BluetoothDevice mBTDevice;
    BluetoothA2dp mmBluetoothA2dp;
    BluetoothGatt mmBluetoothGatt;
    BluetoothHealth mmBluetoothHealth;
    BluetoothHeadset mmBluetoothHeadset;
    BluetoothGattServer mmBluetoothGattServer;
    BluetoothConnectionService mBluetoothConnectionService;

    // Bluetooth Array Lists
    ListView mActionLogger;
    StringBuffer mOutActionBuffer;
    String mConnectedDeviceName = null;
    ArrayAdapter<String> mActionAdapter;
    DeviceListAdapter pairedDeviceListAdapter;
    public DeviceListAdapter mDeviceListAdapter;
    public int uuidPos = 0;
    ArrayList<UUID> mUuid;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mSapDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mGattDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBluetoothA2dp = new ArrayList<>();
    public ArrayList<BluetoothDevice> mHealthDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mPairedBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mGattServerDevices = new ArrayList<>();

    // Get the default adapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener mProfileListener =
            new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {

                    final int[] states = new int[]{BluetoothProfile.STATE_CONNECTED,
                            BluetoothProfile.STATE_CONNECTING};
                    switch (profile) {
                        case BluetoothProfile.A2DP:
                            mBluetoothA2dp.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            mmBluetoothA2dp = (BluetoothA2dp) proxy;
                            break;
                        case BluetoothProfile.GATT:
                            mGattDevices.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            mmBluetoothGatt = (BluetoothGatt) proxy;
                            break;
                        case BluetoothProfile.GATT_SERVER:
                            mGattServerDevices.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            mmBluetoothGattServer = (BluetoothGattServer) proxy;
                            break;
                        case BluetoothProfile.HEADSET:
                            mBTDevices.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            mmBluetoothHeadset = (BluetoothHeadset) proxy;
                            break;
                        case BluetoothProfile.HEALTH:
                            mHealthDevices.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            mmBluetoothHealth = (BluetoothHealth) proxy;
                            break;
                        case BluetoothProfile.SAP:
                            mSapDevices.addAll(proxy.getDevicesMatchingConnectionStates(states));
                            break;
                    }
                }

                public void onServiceDisconnected(int profile) {

                    switch (profile) {
                        case BluetoothProfile.HEADSET:
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,
                                    mmBluetoothHeadset);
                            mmBluetoothHeadset = null;
                            break;
                        case BluetoothProfile.A2DP:
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mmBluetoothA2dp);
                            mmBluetoothA2dp = null;
                            break;
                        case BluetoothProfile.GATT:
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.GATT, mmBluetoothGatt);
                            mmBluetoothGatt = null;
                            break;
                        case BluetoothProfile.GATT_SERVER:
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.GATT_SERVER,
                                    mmBluetoothGattServer);
                            mmBluetoothGattServer = null;
                            break;
                        case BluetoothProfile.HEALTH:
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, mmBluetoothHealth);
                            mmBluetoothHealth = null;
                            break;
                        case BluetoothProfile.SAP:
                            mmBluetoothGatt = null;
                            break;
                    }
                }
            };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver for changing discoverability mode on/off.
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mBtOnOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBtOnOffReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBtOnOffReceiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBtOnOffReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver for changing discoverability mode on/off.
     */
//------------------------------------------------------------------------------------------------//

    private final BroadcastReceiver mBtModeStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "BtModeStateReceiver: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "BtModeStateReceiver: Discoverability Disabled. " +
                                "Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "BtModeStateReceiver: Discoverability Disabled. " +
                                "Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "BtModeStateReceiver: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "BtModeStateReceiver: Connected.");
                        break;
                }
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     * Create a BroadcastReceiver for ACTION_FOUND.
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mDeviceDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "DeviceDiscoveryReceiver: ACTION FOUND.");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + deviceName + deviceHardwareAddress);
                mDeviceListAdapter = new DeviceListAdapter(context, intent,
                        R.layout.device_adapter_view, mBTDevices);
                mListBtDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
//------------------------------------------------------------------------------------------------//

    private final BroadcastReceiver mBondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BondStateReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                // creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BondStateReceiver: BOND_BONDING.");
                }
                // breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BondStateReceiver: BOND_NONE.");
                }
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver that detects signal stretch
     */
//------------------------------------------------------------------------------------------------//

    private final BroadcastReceiver mRssiReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    mActionAdapter.add(name + " => " + rssi + "dBm\n");
                }
            } catch (NullPointerException w) {
                Log.e(TAG, "Null signal", w);
            }
        }
    };

    public BluetoothActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();

        enableBluetooth();                    // enable Bluetooth
        btnEnableDisable_Discoverable();     // turn visibility on
        discoverUnpairedDevices();          // discover unpaired devices
        mHandler = new Handler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter instance

        mUuid = new ArrayList<>();
        // 7 randomly-generated UUIDs. These must match on both server and client.
        mUuid.add(UUID.fromString("4350c323-3451-4a87-a711-2873ed17532a"));
        mUuid.add(UUID.fromString("348220fa-22e0-48d7-b687-fd5fddea1626"));
        mUuid.add(UUID.fromString("f08ddc88-724f-4856-a36a-bf5b9b886096"));
        mUuid.add(UUID.fromString("198d9b4d-7679-45cb-8b21-3f3723f36069"));
        mUuid.add(UUID.fromString("6a4857e4-4cbf-4f56-b04d-e168bc350154"));
        mUuid.add(UUID.fromString("b9c744fb-c464-46e4-9008-e48651398126"));
        mUuid.add(UUID.fromString("9debd0a7-6a14-40a0-b58c-dbd3aab8bb50"));

        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", (dialog, which) -> System.exit(0))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        int pCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pCheck = Objects.requireNonNull(getActivity())
                    .checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            pCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            pCheck += getActivity().checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");
            pCheck += getActivity().checkSelfPermission("Manifest.permission.BLUETOOTH");
            pCheck += getActivity().checkSelfPermission("android.permission.STORAGE");
            pCheck += getActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            pCheck += getActivity().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");

            if (pCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH},
                        ACTION_REQUEST_MULTIPLE_PERMISSION);
            }
        }

        // Register RSSI receiver
        Objects.requireNonNull(getActivity()).registerReceiver(mRssiReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.SAP);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.A2DP);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.GATT);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.GATT_SERVER);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEADSET);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEALTH);

        // It will work if your bluetooth device is already bounded to your phone
        // If not, you can use the startDiscovery() method and connect to your device
        Set<BluetoothDevice> bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bluetoothDevice : bluetoothDeviceSet) {
            bluetoothDevice.connectGatt(getActivity(), true,
                    new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);
                        }

                        @Override
                        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                            if (status == BluetoothGatt.SAP)
                                Log.d("BluetoothRssi", String.format("BluetoothSaP ReadRssi[%d]", rssi));
                        }
                    });
        }

        //TODO Create setup method.. Note on Start make connection to crash. OnCreate is Run once

        // Bind to LocalService
        Intent intent = new Intent(getActivity(), BluetoothConnectionService.class);
        Objects.requireNonNull(getActivity()).bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
        // If BT is not on, request that it be enabled.
        // start Bluetooth service
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // setup the bluetooth activity
        } else if (mBluetoothConnectionService == null) {
            setupBtService();  // Setup Bluetooth
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_activity_mob,
                container, false);

        progressDialog = new ProgressDialog(getActivity());
        mImageContainer = view.findViewById(R.id.image_container);

        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).
                registerReceiver(mImageUploadConReceiver,
                        new IntentFilter("serviceConAction"));


        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).
                registerReceiver(mImageUploadReceiver,
                        new IntentFilter("uploadImageAction"));

        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).
                registerReceiver(mActionReceiver,
                        new IntentFilter("btAction"));

        mListBtDevices = view.findViewById(R.id.bt_devices_list_view_mob);
        mFragmentContainer = view.findViewById(R.id.image_view_container);
        mActionLogger = view.findViewById(R.id.bt_log_list_view_mob);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        Objects.requireNonNull(getActivity()).registerReceiver(mBondStateReceiver, filter);
        // initialize shared model
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Initialize the array adapter for the conversation thread
        mActionAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.bt_action_message);
        mActionLogger.setAdapter(mActionAdapter);
//--------------------------------------List OnItemClick------------------------------------------//

        mListBtDevices.setOnItemClickListener((parent, view1, position, id) -> {
            mBluetoothAdapter.cancelDiscovery();
            mSelectedPosition = position;
            Log.d(TAG, "onItemClick: You Clicked on a device.");
            String deviceName = mBTDevices.get(position).getName();
            String deviceAddress = mBTDevices.get(position).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            //create the bond.
            //NOTE: Requires API 17+? I think this is JellyBean
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);

            Log.d(TAG, "onItemClick: Starting Bluetooth connection.");
            startBtConnection(BluetoothConnectionService.REQUEST_DATA_SERVER);
            Toast.makeText(getActivity(), getString(R.string.toast_bt_starting_connection)
                    + deviceName + deviceAddress, Toast.LENGTH_SHORT).show();
        });

//--------------------------------------Switch Button---------------------------------------------//
        final CompoundButton mySwitch = view.findViewById(R.id.bt_on_off);
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enableDisableBT(); // Start/Stop Bluetooth
        });

//-------------------------------------Bottom Navigation------------------------------------------//
        //TODO Here we start Bluetooth connection and wait for incoming data;

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {

                    switch (item.getItemId()) {

                        case R.id.action_discoverable:
                            Log.d(TAG, "onClick: enabling/disabling discovery.");
                            btnEnableDisable_Discoverable();
                            Toast.makeText(Objects.requireNonNull(getActivity()).getBaseContext(),
                                    R.string.toast_bt_discovery, Toast.LENGTH_SHORT).show();
                            break;

                        case R.id.action_search:
                            discoverUnpairedDevices();
                            break;

                        case R.id.action_upload:
                            Log.d(TAG, "onClick: Pick image...");
                            mFragmentContainer.setVisibility(View.VISIBLE);
                            ImageUploadFragment mImageFragment = (ImageUploadFragment) getActivity()
                                    .getSupportFragmentManager().findFragmentByTag("CHILD_FRAGMENT");

                            if (mImageFragment != null && mImageFragment.isVisible()) {
                                mFragmentContainer.setVisibility(View.VISIBLE);
                            } else {
                                Fragment childFragment = new ImageUploadFragment();
                                FragmentTransaction transaction = getChildFragmentManager()
                                        .beginTransaction();
                                transaction.replace(R.id.image_view_container, childFragment,
                                        "CHILD_FRAGMENT").commit();
                            }
                            int state = mBluetoothConnectionService.getConnectionState();
                            if (state != 6) {
                                try {
                                    String message = ActionMenu.UPLOAD_IMAGE.name();
                                    byte[] bytes = message.getBytes(Charset.defaultCharset());
                                    mBluetoothConnectionService.writeMessage(bytes);
                                } catch (NullPointerException e) {
                                    System.out.println("Could not parse " + e);
                                }
                                Toast.makeText(getActivity(), R.string.image_server_connected,
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                    return true;
                });
        return view;
    }


    @SuppressLint("StaticFieldLeak")
    public void prepareImageUpload() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SharedViewModel model = ViewModelProviders
                            .of(Objects.requireNonNull(getActivity())).get(SharedViewModel.class);
                    model.getSelected().observe(getActivity(), item -> {
                        // TODO Update the UI.
                        Log.d(TAG, "passed");
                        mImageArray = convertBitmapToArray(Objects.requireNonNull(item));
                    });
                } catch (NullPointerException e) {
                    Log.w(TAG, "onClick: Missing data.. ", e);
                }
                if (mImageArray != null) {
                    ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
                    // Prepare checkSum message with array length to be send over socket
                    // to the server to and check message from inputStream and check is
                    // send successfully and notify user for result on both side
                    int messageLength = mImageArray.length;
                    String message = Integer.toString(messageLength);
                    // construct message
                    bufferOut.write(messageLength);
                    // write message to byte array
                    byte[] bytes = message.getBytes(Charset.defaultCharset());
                    // write byte array with message to Socked to make checksum
                    mBluetoothConnectionService.writeMessage(bytes);
                }
                return null;
            }
        }.execute();
        Toast.makeText(getActivity(), R.string.bt_image_sending_prepare, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    public void imageUpload() {
        Log.d(TAG, "onClick: Images sending... ");
        Toast.makeText(getActivity(), R.string.toast_bt_image_sending, Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //send image actual data via socked
                mBluetoothConnectionService.imageWrite(mImageArray);
                mImageArray = null;
                return null;
            }
        }.execute();
    }

    //------------------------------------------------------------------------------------------------//
    // TODO: Rename method, update argument and hook method into UI event
    @SuppressWarnings("unused")
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onBtFragmentListener();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onBtFragmentListener();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Bluetooth States
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    private void mBluetoothState() {
        mPairedBTDevices.addAll(mBluetoothConnectionService.findPairedDevices());
        pairedDeviceListAdapter.notifyDataSetChanged();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * enable/disable bluetooth.
     */
//------------------------------------------------------------------------------------------------//
    public void enableDisableBT() {
        // Get the BluetoothAdapter.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        // Enable Bluetooth.
        assert mBluetoothAdapter != null;
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, BTIntent);
            Toast.makeText(getActivity(), R.string.toast_bt_enabled, Toast.LENGTH_SHORT).show();
        }

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            // stop running Threads
            mBluetoothConnectionService.stop();
            // disable bluetooth
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, BTIntent);
            Toast.makeText(getActivity(), R.string.toast_bt_disable, Toast.LENGTH_SHORT).show();
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Check and enable Bluetooth onCreate method or register receiver
     */
//------------------------------------------------------------------------------------------------//
    private void enableBluetooth() {
        assert mBluetoothAdapter != null;
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            try {
                Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, BTIntent);
            } catch (NullPointerException w) {
                Log.d(TAG, "enableDisableBT NuLL");
            }
            Toast.makeText(getActivity(), R.string.toast_bt_enabled, Toast.LENGTH_SHORT).show();
        } else {
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            try {
                Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, BTIntent);
            } catch (NullPointerException w) {
                Log.d(TAG, "enableDisableBT NuLL");
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * starting/stopping bluetooth discoverable.
     */
//------------------------------------------------------------------------------------------------//
    public void btnEnableDisable_Discoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        Objects.requireNonNull(getActivity()).registerReceiver(mDeviceDiscoveryReceiver, intentFilter);
        Objects.requireNonNull(getActivity()).registerReceiver(mBtModeStateReceiver, intentFilter);
        Objects.requireNonNull(getActivity()).registerReceiver(mBondStateReceiver, intentFilter);
        Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, intentFilter);

    }
//------------------------------------------------------------------------------------------------//

    /**
     * starting bluetooth search for unpaired devices.
     */
//------------------------------------------------------------------------------------------------//
    public void discoverUnpairedDevices() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            mBluetoothAdapter.startDiscovery();
            if (mListBtDevices != null) {
                mListBtDevices.setAdapter(null);
            }
            if (mBTDevices != null) {
                mBTDevices.clear();
            }
            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            Objects.requireNonNull(getActivity()).registerReceiver(mDeviceDiscoveryReceiver, filter);
        }

        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
            if (mListBtDevices != null) {
                mListBtDevices.setAdapter(null);
            }

            if (mBTDevices != null) {
                mBTDevices.clear();
            }
            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            Objects.requireNonNull(getActivity()).registerReceiver(mDeviceDiscoveryReceiver, filter);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Starting bluetooth connection
     *
     * @param requestCode Request Code
     */
//------------------------------------------------------------------------------------------------//
    public void startBtConnection(int requestCode) {
        if (requestCode == BluetoothConnectionService.REQUEST_DATA_SERVER) {
            startBTConnection(mBTDevice, setUuidFromList(mUuid), BluetoothConnectionService.REQUEST_DATA_SERVER);
            Log.d(TAG, "Request action server.");
        } else {
            startBTConnection(mBTDevice, setUuidFromList(mUuid), BluetoothConnectionService.REQUEST_IMAGE_SERVER);
            Log.d(TAG, "Request image server.");
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param device      The BluetoothDevice that has been connected
     * @param uuid        UUID secure hash
     * @param requestCode The code for bluetooth state
     */
//------------------------------------------------------------------------------------------------//
    public void startBTConnection(BluetoothDevice device, UUID uuid, int requestCode) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        if (device != null && uuid != null) {
            if (requestCode == BluetoothConnectionService.REQUEST_DATA_SERVER) {
                mBluetoothConnectionService.startClient(device, uuid, requestCode);
            } else if (requestCode == BluetoothConnectionService.REQUEST_IMAGE_SERVER) {
                mBluetoothConnectionService.startServer(device, uuid, requestCode);
            }
        } else {
            Log.d(TAG, "startBTConnection: Error initializing RFCOM Bluetooth Connection. " +
                    "Please make sure that you select device from list");
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * starting bluetooth server
     */
//------------------------------------------------------------------------------------------------//
    public void startBtServer() {
        startBTConnection(mBTDevice, setUuidFromList(mUuid), BluetoothConnectionService.REQUEST_IMAGE_SERVER);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();

    }
    //------------------------------------------------------------------------------------------------//

    /**
     * Activity lifecycle override methods
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothConnectionService.stop();
        Objects.requireNonNull(getActivity()).unbindService(mConnection);
        mBound = false;
        Objects.requireNonNull(getActivity()).unregisterReceiver(mDeviceDiscoveryReceiver);
        getActivity().unregisterReceiver(mBtModeStateReceiver);
        getActivity().unregisterReceiver(mBondStateReceiver);
        getActivity().unregisterReceiver(mBtOnOffReceiver);
        getActivity().unregisterReceiver(mRssiReceiver);
    }
//------------------------------------------------------------------------------------------------//
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
//------------------------------------------------------------------------------------------------//
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) service;
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBluetoothConnectionService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


//------------------------------------------------------------------------------------------------//

    /**
     * Convert and resize our image to 400dp for faster uploading our images to DB
     */
//------------------------------------------------------------------------------------------------//
    private Bitmap decodeUri(Uri selectedImage) {

        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(Objects.requireNonNull(getActivity()).
                    getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;
            int imageHeight = o.outHeight;
            int imageWidth = o.outWidth;
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= imageWidth &&
                    height_tmp / 2 >= imageHeight) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getActivity().
                    getContentResolver().openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


//------------------------------------------------------------------------------------------------//

    /**
     * onActivityResult method
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                Log.d(TAG, "Enable discoverable returned with result " + resultCode
                        + DISCOVERABLE_TIMEOUT_MS
                        + " milliseconds. Look for a device named "
                        + ADAPTER_FRIENDLY_NAME);

                // ResultCode, as described in BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE, is either
                // RESULT_CANCELED or the number of milliseconds that the device will stay in
                // discoverable mode.

                if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "Enable discoverable has been cancelled by the user. "
                            + "This should never happen in an Android Things device.");
                    Objects.requireNonNull(getActivity()).finish();
                    return;
                }
                break;

            case PICK_IMAGE_REQUEST:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            String uri = selectedImage.getPath();
                            Bitmap bmp = decodeUri(selectedImage);
                            sharedViewModel.select(bmp);
                            System.out.print("Image Path: " + uri);
                            mFragmentContainer.setVisibility(View.VISIBLE);
                            Fragment childFragment = new ImageUploadFragment();
                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                            transaction.replace(R.id.image_view_container, childFragment).commit();
                        }
                    }

                } catch (NullPointerException w) {
                    Log.v(TAG, "Please select image.", w);
                }
                break;
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to convert Bitmap to byte array
     */
//------------------------------------------------------------------------------------------------//
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] convertBitmapToArray(Bitmap b) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to copy music files from raw directory to External Device Storage Dir
     *
     * @param resourceId   pass resource file ID
     * @param resourceName pass name of file
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    private void copyFileToExternalStorage(int resourceId, String resourceName) {
        File file = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), resourceName);
        if (!file.canWrite()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        if (isExternalStorageWritable()) {
            File pathSDCard = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), resourceName);
            try {
                InputStream in = getResources().openRawResource(resourceId);
                FileOutputStream out;
                out = new FileOutputStream(pathSDCard);
                byte[] buff = new byte[1024];
                int read;
                try {
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                } finally {
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to check Writable Permission
     */
//------------------------------------------------------------------------------------------------//
    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


//------------------------------------------------------------------------------------------------//------------------------------------------------
    /**
     * REMOTE CONTROL Broadcast receiver. Receive message form Remote control fragment
     * to handle user actions. Also send data to Server via local Bluetooth service.
     * Message FROM :{@link RemoteControlFragment}
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("remote action");
            Log.d("receiver", "Got action position message: " + message);
            try {
                byte[] bytes = message.getBytes(Charset.defaultCharset());
                mBluetoothConnectionService.writeMessage(bytes);
            } catch (NullPointerException w) {
                Log.d("receiver", "Got null data: " + w);
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast receiver. Receive message with new Service to startup prepare and uploading image.
     * Message FROM :{@link BluetoothConnectionService}
     * Execute method in fragment to start sending real data to the server.
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mImageUploadConReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("receiver", "Got action request upload: ");
            try {
                String message = intent.getStringExtra("service action");
                Log.d("receiver", "Got action message: " + message);
                if (message.contentEquals(ActionMenu.UPLOAD_IMAGE.name())) {
                    startBtServer();
                }
            } catch (NullPointerException e) {
                System.out.println("Could not parse " + e);
            }
        }
    };

//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast receiver. Receive message with new Service to startup prepare and uploading image.
     * Message FROM :{@link BluetoothConnectionService}
     * Execute method in fragment to start sending real data to the server.
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mImageUploadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("receiver", "Got action request upload: ");
            try {
                String message = intent.getStringExtra("service action upload");
                Log.d("receiver", "Got action position message: " + message);
                int fileSize = Integer.parseInt(message);
                int messageLength = mImageArray.length;
                if (fileSize == messageLength) {
                    imageUpload();
                    sharedViewModel.clearSelected();
                    mFragmentContainer.setVisibility(View.GONE);
                }
            } catch (NullPointerException e) {
                System.out.println("Could not parse " + e);
            }
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Give uuid from list. Max size 7.
     *
     * @param uuid Uuid number to the new connection
     */
//------------------------------------------------------------------------------------------------//
    public UUID setUuidFromList(ArrayList<UUID> uuid) {
        UUID uuids = UUID.fromString(String.valueOf(uuid.get(uuidPos)));
        uuidPos++;
        int uuidMaxPos = 7;
        if (uuidPos == uuidMaxPos) {
            uuidPos = 0;
        }
        return uuids;
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Set up Bluetooth service.
     */
//------------------------------------------------------------------------------------------------//
    private void setupBtService() {
        Log.d(TAG, "setup BT service");
        // Initialize the buffer for outgoing messages
        mOutActionBuffer = new StringBuffer();
        // Initialize the Bluetooth Service and start listening for incoming connections
        mBluetoothConnectionService = new BluetoothConnectionService(getActivity(), mHandler);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Handler to display messages from {@link BluetoothConnectionService}
     */
//------------------------------------------------------------------------------------------------//
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            mActionAdapter.clear();
                            mActionAdapter.add(getString(R.string.bt_connected_to)
                                    + mConnectedDeviceName);
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            mActionAdapter.add(getString(R.string.bt_connecting_state));
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                        case BluetoothConnectionService.STATE_NONE:
                            mActionAdapter.add("Disconnected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mActionAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mActionAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, getString(R.string.toast_connected_to_bt)
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

}
