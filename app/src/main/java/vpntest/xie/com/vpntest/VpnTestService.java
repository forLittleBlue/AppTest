package vpntest.xie.com.vpntest;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class VpnTestService extends VpnService {
    private String TAG = "VpnTest.VpnTestService";
    private ParcelFileDescriptor mVpn = null;

//    private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
//    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    private Selector udpSelector;
    private Selector tcpSelector;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        IntentFilter filter = new IntentFilter("action_VPN_TEST_STOP_VPN");
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        Builder vpnBuilder = new VpnServiceBuild();
        vpnBuilder.setSession("VpnTest");
        vpnBuilder.addAddress("10.1.10.1", 32);
        vpnBuilder.setMtu(1500);
        vpnBuilder.addRoute("0.0.0.0", 0);

        Intent intent2 = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        vpnBuilder.setConfigureIntent(pIntent);

        try {
            vpnBuilder.addDisallowedApplication("com.vivo.browser");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (mVpn != null) {
            try {
                mVpn.close();
            } catch (IOException e) {
            }
        }

        mVpn = vpnBuilder.establish();

        try {
            forIpRun();
        } catch (IOException e) {

        }

        return START_STICKY;
    }

    private void forIpRun() throws IOException {
        // Create a DatagramChannel as the VPN tunnel.
        DatagramChannel mTunnel = DatagramChannel.open();
        // Protect the tunnel before connecting to avoid loopback.
        if (!protect(mTunnel.socket())) {
            throw new IllegalStateException("Cannot protect the tunnel");
        }
        String mServerAddress = "127.0.0.1";
        final int mServerPort = 5555;
        InetSocketAddress server = new InetSocketAddress(mServerAddress, mServerPort);

        new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    DatagramSocket socket = new DatagramSocket(mServerPort);
                    DatagramPacket packet = new DatagramPacket(new byte[255], 255);

                    while (true) {
                        try {
                            socket.receive(packet);// 阻塞
                            socket.send(packet);
                            packet.setLength(255);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    // Packets to be sent are queued in this input stream.
                    FileInputStream in = new FileInputStream(mVpn.getFileDescriptor());

                    // Packets received need to be written to this output stream.
                    FileOutputStream out = new FileOutputStream(mVpn.getFileDescriptor());

                    // Allocate the buffer for a single packet.
                    ByteBuffer packet = ByteBuffer.allocate(32767);
                    int length;
                    while (true) {
                        // Read packets sending to this interface
                        length = in.read(packet.array());
                        // Write response packets back
                        out.write(packet.array(), 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mVpn != null) {
            try {
                mVpn.close();
            } catch (IOException e) {
            }
        }
        super.onDestroy();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "intent: " + intent);
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals("action_VPN_TEST_STOP_VPN")) {
                if (mVpn != null) {
                    try {
                        mVpn.close();
                    } catch (IOException e) {
                    }
                }
                stopSelf();
            }
        }
    };

    private class VpnServiceBuild extends VpnService.Builder {
        @Override
        public Builder setMtu(int mtu) {
            return super.setMtu(mtu);
        }

        @Override
        public Builder addDisallowedApplication(String packageName) throws PackageManager.NameNotFoundException {
            Log.d(TAG, "addDisallowedApplication: " + packageName);
            return super.addDisallowedApplication(packageName);
        }

        @Override
        public Builder addAddress(String address, int prefixLength) {
            return super.addAddress(address, prefixLength);
        }

        @Override
        public Builder addDnsServer(String address) {
            return super.addDnsServer(address);
        }

        @Override
        public Builder addRoute(String address, int prefixLength) {
            return super.addRoute(address, prefixLength);
        }

        @Override
        public Builder addSearchDomain(String domain) {
            return super.addSearchDomain(domain);
        }

        @Override
        public Builder setBlocking(boolean blocking) {
            return super.setBlocking(blocking);
        }

        @Override
        public Builder setSession(String session) {
            return super.setSession(session);
        }

        @Override
        public Builder setConfigureIntent(PendingIntent intent) {
            return super.setConfigureIntent(intent);
        }
    }
}
