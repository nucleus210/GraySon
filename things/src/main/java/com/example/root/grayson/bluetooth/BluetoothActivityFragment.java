package com.example.root.grayson.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.grayson.R;
import com.example.root.grayson.TextToSpeechManager;
import com.google.android.things.bluetooth.BluetoothClassFactory;
import com.google.android.things.bluetooth.BluetoothConfigManager;
import com.google.android.things.bluetooth.BluetoothConnectionCallback;
import com.google.android.things.bluetooth.BluetoothConnectionManager;
import com.google.android.things.bluetooth.BluetoothProfileManager;
import com.google.android.things.bluetooth.BluetoothPairingCallback;
import com.google.android.things.bluetooth.ConnectionParams;
import com.google.android.things.bluetooth.PairingParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BluetoothActivityFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BluetoothActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothActivityFragment extends Fragment implements
        TextToSpeechManager.Listener, BluetoothConnectionService.BtServiceCallbacks {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "Bluetooth Fragment";
    private static final String ADAPTER_FRIENDLY_NAME = "GraySon device";

    Context mContext;
    // Activity context
    boolean mBound = false;                                  // is fragment bound to service
    boolean mA2dpBound = false;                              // is A2dp bound to service

    ListView mActionLogger;                                  // Log list view
    ListView mListBtDevices;                                 // List with found Bluetooth devices
    ListView mPairedDevices;                                 // List with paired Devices
    StringBuffer mOutActionBuffer;                           // Remote control actions
    String mConnectedDeviceName = null;                      // Connected devices
    DeviceListAdapter mDeviceListAdapter;                    // Device list adapter
    DeviceListAdapter mPairedDeviceListAdapter;              // Paired devices adapter
    VerticalTextView mVerticalText;
    int mSelectedPosition;                                   // Selected device position from List
    BluetoothProfile mBluetoothProfile;                      // Bluetooth Profiler
    TextToSpeechManager mTextToSpeechManager;                // Define TTS manager
    OnFragmentInteractionListener mListener;                 // Interface Listener
    BluetoothConnectionService mBluetoothConnectionService;  // Bluetooth Service
    BluetoothDevice mBluetoothDevice;
    A2dpSinkHelper mA2dpSinkHelper;
    TextView mDeviceName;
    TextView mConnectState;
    // Bluetooth constants
    private static final int REQUEST_ENABLE_BT = 101;
    private static final int DISCOVERABLE_TIMEOUT_MS = 102;
    private static final int REQUEST_ENABLE_BLUETOOTH = 103;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 104;

    public static BluetoothDevice mBTDevice;
    public static BluetoothA2dp mmBluetoothA2dp;
    public static BluetoothGatt mmBluetoothGatt;
    public static BluetoothHealth mmBluetoothHealth;
    public static BluetoothHeadset mmBluetoothHeadset;
    public static BluetoothGattServer mmBluetoothGattServer;
    public static BluetoothConnectionManager mBluetoothConnectionManager;

    // Bluetooth Array Lists
    public int uuidPos=0;
    ArrayList<UUID>             mUuid;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mSapDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mGattDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBluetoothA2dp = new ArrayList<>();
    public ArrayList<BluetoothDevice> mHealthDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mPairedBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mGattServerDevices = new ArrayList<>();
    private ArrayAdapter<String> mActionAdapter;

    // Get the default adapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {

            final int[] states = new int[]{BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_CONNECTING};
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
                final int state = intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

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

    private final BroadcastReceiver mRssiReceiver = new BroadcastReceiver(){
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = Objects.requireNonNull(getActivity())
                        .findViewById(R.id.signal_rssi_text);
                rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
            }
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Handle an intent that is broadcast by the Bluetooth adapter whenever it changes its
     * state (after calling enable(), for example).
     * Action is {@link BluetoothAdapter#ACTION_STATE_CHANGED} and extras describe the old
     * and the new states. You can use this intent to indicate that the device is ready to go.
     */
//------------------------------------------------------------------------------------------------//
       private final BroadcastReceiver mAdapterStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int oldState = A2dpSinkHelper.getPreviousAdapterState(intent);
            int newState = A2dpSinkHelper.getCurrentAdapterState(intent);
            Log.d(TAG, "Bluetooth Adapter changing state from " + oldState + " to " + newState);
            if (newState == BluetoothAdapter.STATE_ON) {
                mTextToSpeechManager.say("Bluetooth Adapter ready ");
                Log.i(TAG, "Bluetooth Adapter is ready");
                initA2DPSink();
            }
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * connects or disconnects to it.
     * Action is {@link A2dpSinkHelper#ACTION_CONNECTION_STATE_CHANGED} and
     * extras describe the old and the new connection states. You can use it to indicate that
     * there's a device connected.
     */
//------------------------------------------------------------------------------------------------//

    private final BroadcastReceiver mSinkProfileStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                Log.d(TAG, "Bluetooth A2DP sink changing connection state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    String deviceName = Objects.toString(device.getName(), "a device");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mTextToSpeechManager.say("Connected to " + deviceName);

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mTextToSpeechManager.say("Disconnected from " + deviceName);
                    }
                }
            }
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * starts or stops playing through the A2DP sink.
     * Action is {@link A2dpSinkHelper#ACTION_PLAYING_STATE_CHANGED} and
     * extras describe the old and the new playback states. You can use it to indicate that
     * there's something playing. You don't need to handle the stream playback by yourself.
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mSinkProfilePlaybackChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED.equals(intent.getAction())) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                Log.d(TAG, "Bluetooth A2DP sink changing playback state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    if (newState == A2dpSinkHelper.STATE_PLAYING) {
                        mTextToSpeechManager.say("Playing audio ");
                        Log.i(TAG, "Playing audio from device " + device.getAddress());
                    } else if (newState == A2dpSinkHelper.STATE_NOT_PLAYING) {
                        mTextToSpeechManager.say("Stop playing audio ");
                        Log.i(TAG, "Stopped playing audio from " + device.getAddress());
                    }
                }
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Interfaces that are used for Text to Speech manager
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onTtsInitialized() {}

    @Override
    public void onTtsSpoken() {}

    @Override
    public void requestImageServer() {}

    public BluetoothActivityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BluetoothActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static BluetoothActivityFragment newInstance(String param1, String param2) {
        BluetoothActivityFragment fragment = new BluetoothActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();

        enableBluetooth();                    // enable Bluetooth
        btnEnableDisable_Discoverable();     // turn visibility on
        discoverUnpairedDevices();          // discover unpaired devices

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter instance
        mA2dpSinkHelper = new A2dpSinkHelper();

        mUuid = new ArrayList<>();
        // 7 randomly-generated UUIDs. These must match on both server and client.
        mUuid.add(UUID.fromString("4350c323-3451-4a87-a711-2873ed17532a"));
        mUuid.add(UUID.fromString("348220fa-22e0-48d7-b687-fd5fddea1626"));
        mUuid.add(UUID.fromString("f08ddc88-724f-4856-a36a-bf5b9b886096"));
        mUuid.add(UUID.fromString("198d9b4d-7679-45cb-8b21-3f3723f36069"));
        mUuid.add(UUID.fromString("6a4857e4-4cbf-4f56-b04d-e168bc350154"));
        mUuid.add(UUID.fromString("b9c744fb-c464-46e4-9008-e48651398126"));
        mUuid.add(UUID.fromString("9debd0a7-6a14-40a0-b58c-dbd3aab8bb50"));

        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            @SuppressWarnings("unused")
            String mParam1 = getArguments().getString(ARG_PARAM1);
            @SuppressWarnings("unused")
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mTextToSpeechManager = new TextToSpeechManager(mContext, this);

        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.A2DP);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.GATT);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.GATT_SERVER);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEADSET);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEALTH);
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.SAP);


        Objects.requireNonNull(getActivity()).registerReceiver(mAdapterStateChangeReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        getActivity().registerReceiver(mSinkProfileStateChangeReceiver, new IntentFilter(
                A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED));
        getActivity().registerReceiver(mSinkProfilePlaybackChangeReceiver, new IntentFilter(
                A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED));

        //TODO
        @SuppressWarnings("unused")
        String bondedDevices = mBluetoothAdapter.getBondedDevices().toString();

        BluetoothConfigManager manager = BluetoothConfigManager.getInstance();

        // Report the local Bluetooth device class as a speaker
        BluetoothClass deviceClass = BluetoothClassFactory.build(
                BluetoothClass.Service.OBJECT_TRANSFER,
                BluetoothClass.Device.COMPUTER_DESKTOP);
        manager.setBluetoothClass(deviceClass);

        // Report full input/output capability for this device
        // IO_CAPABILITY_IO: Device has a display and can accept basic (yes/no) input.
        manager.setIoCapability(BluetoothConfigManager.IO_CAPABILITY_IO);
        manager.setLeIoCapability(BluetoothConfigManager.IO_CAPABILITY_IO);

        // Report full input/output capability for this device
        BluetoothProfileManager profileManager = BluetoothProfileManager.getInstance();

        List<Integer> enabledProfiles = profileManager.getEnabledProfiles();

        if (!enabledProfiles.contains(BluetoothProfile.SAP)) {
            Log.d(TAG, "Enabling A2DP sink mode.");
            List<Integer> toDisable = Collections.emptyList();
            List<Integer> toEnable = Arrays.asList(
                    BluetoothProfile.SAP,
                    BluetoothProfile.A2DP,
                    com.google.android.things.bluetooth.BluetoothProfile.A2DP_SINK);
            profileManager.enableAndDisableProfiles(toEnable, toDisable);
        }

        // Enables apps to connect to additional profiles and services on remote devices
        mBluetoothConnectionManager = BluetoothConnectionManager.getInstance();
        mBluetoothConnectionManager.registerPairingCallback(mBluetoothPairingCallback);
        mBluetoothConnectionManager.registerConnectionCallback(mBluetoothConnectionCallback);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_activity,
                container, false);

        // find views
        mDeviceName = view.findViewById(R.id.device_address);
        mConnectState = view.findViewById(R.id.connection_state);
        mVerticalText = view.findViewById(R.id.found_device_text);
        mVerticalText = view.findViewById(R.id.remote_control_text);
        mActionLogger = view.findViewById(R.id.bt_log_list_view);
        mListBtDevices = view.findViewById(R.id.bt_devices_list_view);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        Objects.requireNonNull(getActivity()).registerReceiver(mBondStateReceiver, filter);

        mListBtDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                mSelectedPosition = position;
                Log.d(TAG, "onItemClick: You Clicked on a device.");
                String deviceName = mBTDevices.get(position).getName();
                String deviceAddress = mBTDevices.get(position).getAddress();
                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
                //create the bond.
                Log.d(TAG, "Trying to pair with " + deviceName);
                mBTDevices.get(position).createBond();
                mBTDevice = mBTDevices.get(position);
                Toast.makeText(getActivity(), "Trying to pair with.. "
                        + deviceName + deviceAddress, Toast.LENGTH_SHORT).show();
            }
        });

        findPairedDevices();     // find paired devices
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDateTimeFragmentListener");
        }
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnHeadlineSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(Objects.requireNonNull(context.toString()
                    + " must implement OnHeadlineSelectedListener"));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    OnHeadlineSelectedListener mCallback;
    // Container Activity must implement this interface
    @SuppressWarnings("unused")
    public interface OnHeadlineSelectedListener {
        void onArticleSelected(int position);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    @SuppressWarnings("unused")
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void startPairing(BluetoothDevice remoteDevice) {
        mBluetoothConnectionManager.initiatePairing(remoteDevice);
    }

    private BluetoothPairingCallback mBluetoothPairingCallback = new BluetoothPairingCallback() {

        @Override
        public void onPairingInitiated(BluetoothDevice bluetoothDevice,
                                       PairingParams pairingParams) {
            // Handle incoming pairing request or confirmation of outgoing pairing request
            handlePairingRequest(bluetoothDevice, pairingParams);
        }

        @Override
        public void onPaired(BluetoothDevice bluetoothDevice) {
            // Device pairing complete
        }

        @Override
        public void onUnpaired(BluetoothDevice bluetoothDevice) {
            // Device unpaired
        }

        @Override
        public void onPairingError(BluetoothDevice bluetoothDevice,
                                   BluetoothPairingCallback.PairingError pairingError) {
            // Something went wrong!
        }
    };

    // Set up callbacks for the profile connection process.
    private final BluetoothConnectionCallback mBluetoothConnectionCallback =
            new BluetoothConnectionCallback() {
        @Override
        public void onConnectionRequested(BluetoothDevice bluetoothDevice,
                                          ConnectionParams connectionParams) {
            // Handle incoming connection request
            handleConnectionRequest(mBluetoothDevice,connectionParams);
        }

        @Override
        public void onConnectionRequestCancelled(BluetoothDevice bluetoothDevice, int requestType) {
            // Request cancelled
        }

        @Override
        public void onConnected(BluetoothDevice bluetoothDevice, int profile) {
            // Connection completed successfully
        }

        @Override
        public void onDisconnected(BluetoothDevice bluetoothDevice, int profile) {
            // Remote device disconnected
        }
    };

    private void handleConnectionRequest(BluetoothDevice bluetoothDevice,
                                         ConnectionParams connectionParams) {
        // Determine whether to accept the connection request
        boolean accept = false;
        if (connectionParams.getRequestType() == ConnectionParams.REQUEST_TYPE_PROFILE_CONNECTION) {
            accept = true;
        }

        // Pass that result on to the BluetoothConnectionManager
        mBluetoothConnectionManager.confirmOrDenyConnection(bluetoothDevice, connectionParams, accept);
    }


    private void handlePairingRequest(BluetoothDevice bluetoothDevice, PairingParams pairingParams) {
        switch (pairingParams.getPairingType()) {
            case PairingParams.PAIRING_VARIANT_DISPLAY_PIN:
            case PairingParams.PAIRING_VARIANT_DISPLAY_PASSKEY:
                // Display the required PIN to the user
                Log.d(TAG, "Display Passkey - " + pairingParams.getPairingPin());
                break;
            case PairingParams.PAIRING_VARIANT_PIN:
            case PairingParams.PAIRING_VARIANT_PIN_16_DIGITS:
                // Obtain PIN from the user
                String pin = "";
                // Pass the result to complete pairing
                mBluetoothConnectionManager.finishPairing(bluetoothDevice, pin);
                break;
            case PairingParams.PAIRING_VARIANT_CONSENT:
            case PairingParams.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                // Show confirmation of pairing to the user

                // Complete the pairing process
                mBluetoothConnectionManager.finishPairing(bluetoothDevice);
                break;
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
            Toast.makeText(getActivity(), "enable Bluetooth...", Toast.LENGTH_SHORT).show();
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
            try {
                Objects.requireNonNull(getActivity()).registerReceiver(mBtOnOffReceiver, BTIntent);
            } catch (NullPointerException w) {
                Log.d(TAG, "enableDisableBT NuLL");
            }
            Toast.makeText(getActivity(), "enable Bluetooth...", Toast.LENGTH_SHORT).show();
        }

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            // stop running Threads
            mBluetoothConnectionService.stop();
            // disable bluetooth
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            try {
                getActivity().registerReceiver(mBtOnOffReceiver, BTIntent);
            } catch (NullPointerException w) {
                Log.d(TAG, "enableDisableBT NuLL");
            }
            Toast.makeText(getActivity(), "disable Bluetooth...", Toast.LENGTH_SHORT).show();
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
        Objects.requireNonNull(getActivity()).registerReceiver(mBtModeStateReceiver, intentFilter);
        getActivity().registerReceiver(mRssiReceiver, intentFilter);
        Toast.makeText(Objects.requireNonNull(getActivity()).getBaseContext(),
                "Discover enabled for 300 sec.", Toast.LENGTH_SHORT).show();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * starting bluetooth search for unpaired devices.
     */
//------------------------------------------------------------------------------------------------//
    @RequiresApi(api = Build.VERSION_CODES.M)
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
     * starting bluetooth connection
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    public void startBtConnection() {
        startBTConnection(mBTDevice, setUuidFromList(mUuid),
                BluetoothConnectionService.REQUEST_DATA_SERVER);
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
     * Find paired devices and add them to list
     **/
//------------------------------------------------------------------------------------------------//
    public  void findPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                mPairedBTDevices.add(device);
                Log.d(TAG, "findPairedDevices: device name: " + device.getName() + ", " +
                        "device address: " + device.getAddress());
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Activity lifecycle override methods
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), BluetoothConnectionService.class);
        Objects.requireNonNull(getActivity())
                .bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        // If BT is not on, try to enable.
        // start Bluetooth service
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Setup the Bluetooth session
        } else if (mBluetoothConnectionService == null) {
            setupBtService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            mBluetoothConnectionService.setCallbacks(null);
            Objects.requireNonNull(getActivity()).unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBound = false;
        mListener = null;
        mBluetoothConnectionService.stop();
        Objects.requireNonNull(getActivity()).unbindService(mConnection);
        Objects.requireNonNull(getActivity()).unregisterReceiver(mDeviceDiscoveryReceiver);
        getActivity().unregisterReceiver(mBtModeStateReceiver);
        getActivity().unregisterReceiver(mBondStateReceiver);
        try {
            getActivity().unregisterReceiver(mBtOnOffReceiver);
        }catch (UnknownError e) {
            Log.e(TAG,"Error", e);
        }
        getActivity().unregisterReceiver(mRssiReceiver);
        getActivity().unregisterReceiver(mAdapterStateChangeReceiver);
        getActivity().unregisterReceiver(mSinkProfileStateChangeReceiver);
        getActivity().unregisterReceiver(mSinkProfilePlaybackChangeReceiver);

        if (mmBluetoothA2dp != null) {
            mBluetoothAdapter.closeProfileProxy(A2dpSinkHelper.A2DP_SINK_PROFILE,
                    mmBluetoothA2dp);
            A2dpSinkHelper.disconnect(Objects.requireNonNull(mmBluetoothA2dp), mBTDevices);
        }
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
            BluetoothConnectionService.LocalBinder binder =
                    (BluetoothConnectionService.LocalBinder) service;
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBluetoothConnectionService = binder.getService();
            // register callback from service
            mBluetoothConnectionService.setCallbacks(BluetoothActivityFragment.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
                Log.d(TAG, "Enable discoverable returned with result " + resultCode);
                mTextToSpeechManager.say("Bluetooth audio sink is discoverable for "
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
        }
    }

//------------------------------------------------------------------------------------------------//
    /**
     * Give uuid from list. Max size 7.
     * @param uuid Uuid number to the new connection
     */
//------------------------------------------------------------------------------------------------//
    public UUID setUuidFromList(ArrayList<UUID> uuid) {
        UUID uuids = UUID.fromString(String.valueOf(uuid.get(uuidPos)));
        uuidPos++;
        int uuidMaxPos = 7;
        if (uuidPos==uuidMaxPos){
            uuidPos = 0;
        }
        return  uuids;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Set up the UI and background operations for Bluetooth Service.
     */
//------------------------------------------------------------------------------------------------//
    private void setupBtService() {
        Log.d(TAG, "setup BT service");
        // Initialize the array adapter for the conversation thread
        mActionAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.bt_action_message);

        mActionLogger.setAdapter(mActionAdapter);

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
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            mActionAdapter.clear();
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                            mDeviceName.setText(mConnectedDeviceName);
                            mConnectState.setText(R.string.bt_connecting_state);
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                        case BluetoothConnectionService.STATE_NONE:
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
                    mDeviceName.setText(mConnectedDeviceName);
                    mConnectState.setText(R.string.bt_connected_state);
                    if (null != activity) {
                        Toast.makeText(activity, getString(R.string.bt_toast_connect_to)
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                        mDeviceName.setText(null);
                        mConnectState.setText(R.string.bt_not_connected_state);
                    }
                    break;
            }
        }
    };

//------------------------------------------------------------------------------------------------//
    /**
     * Initiate the A2DP sink. Check current state of {@link BluetoothAdapter} and if is Enabled
     * Set Bluetooth device name. Next enable Bluetooth A2DP profile listener and wait for incoming
     * connections after enable Bluetooth discovery {@link #DISCOVERABLE_TIMEOUT_MS} for 300 ms.
     */
//------------------------------------------------------------------------------------------------//
    private void initA2DPSink() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth adapter not available or not enabled.");
            return;
        }
        setupBTProfiles();
        Log.d(TAG, "Set up Bluetooth Adapter name and profile");
        mBluetoothAdapter.setName(ADAPTER_FRIENDLY_NAME);
        mBluetoothAdapter.getProfileProxy(getContext(), new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                mBluetoothProfile = proxy;
                mA2dpBound = true;
            }

            @Override
            public void onServiceDisconnected(int profile) {
                mA2dpBound = false;
            }
        }, A2dpSinkHelper.A2DP_SINK_PROFILE);
    }
//------------------------------------------------------------------------------------------------//
    /**
     * Setup A2DP SINK PROFILE {@link BluetoothProfileManager} and disable others available profiles
     */
//------------------------------------------------------------------------------------------------//
    private void setupBTProfiles() {
        BluetoothProfileManager bluetoothProfileManager = BluetoothProfileManager.getInstance();
        List<Integer> enabledProfiles = bluetoothProfileManager.getEnabledProfiles();
        if (!enabledProfiles.contains(A2dpSinkHelper.A2DP_SINK_PROFILE)) {
            Log.d(TAG, "Enabling A2dp sink mode.");
            List<Integer> toDisable = Collections.singletonList(BluetoothProfile.A2DP);
            List<Integer> toEnable = Arrays.asList(
                    A2dpSinkHelper.A2DP_SINK_PROFILE,
                    A2dpSinkHelper.AVRCP_CONTROLLER_PROFILE);
            bluetoothProfileManager.enableAndDisableProfiles(toEnable, toDisable);
        } else {
            Log.d(TAG, "A2dp sink profile is enabled.");
        }
    }
}
