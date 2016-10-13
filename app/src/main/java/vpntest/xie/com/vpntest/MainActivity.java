package vpntest.xie.com.vpntest;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private String TAG = "VpnTest.MainActivity";
    private Context mContext;
    private int REQUEST_VPN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mContext = this;
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button1 onClick");
                Intent intent = VpnService.prepare(mContext);
                //if it is prepared, return null
                if (intent != null) {
                    Log.d(TAG, "startActivityForResult intent: " + intent);
                    startActivityForResult(intent, REQUEST_VPN);
                } else {
                    Log.d(TAG, "onActivityResult intent == null");
                    onActivityResult(REQUEST_VPN, RESULT_OK, null);
                }
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mContext, VpnTestService.class);
//                Boolean result = mContext.stopService(intent);
                Log.d(TAG, "button2 onClick ");
                Intent intent = new Intent("action_VPN_TEST_STOP_VPN");
                mContext.sendBroadcast(intent);
            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebViewAcitivity.class);
                mContext.startActivity(intent);
                Log.d(TAG, "button3 onClick ");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);
        if (requestCode == REQUEST_VPN) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "enable vpn");
                Intent intent = new Intent(mContext, VpnTestService.class);
                mContext.startService(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
