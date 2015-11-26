package com.tuc2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by Nishant on 18-Oct-15.
 */
public class btmultiplayer extends Activity {
    RadioButton r1, r2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.selection_mark);
        r1 = (RadioButton) findViewById(R.id.rbX);
        r2 = (RadioButton) findViewById(R.id.rbO);

    }
    public void nxt(View v)
    {
        Intent i = new Intent(this,DeviceListActivity.class);
        if(r1.isChecked())
        {

            i.putExtra("symbol",'X');
        }
        else
            if(r2.isChecked()) {
                i.putExtra("symbol", 'O');
            }
        setResult(Activity.RESULT_OK, i);
        finish();
    }

}
