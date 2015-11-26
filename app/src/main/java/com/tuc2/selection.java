package com.tuc2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

/**
 * Created by Nishant on 06-Jul-15.
 */
public class selection extends Activity {
    RadioButton r1, r2,r3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuc_selection);
        r1 = (RadioButton) findViewById(R.id.rb1);
        r2 = (RadioButton) findViewById(R.id.rb2);
        r3 = (RadioButton)findViewById(R.id.rb3);
    }

    public void select(View v)
    {
        if(r1.isChecked())
        {
            Intent i = new Intent(this,play2tuc.class);
            startActivity(i);
            finish();
        }
        else
            if(r2.isChecked())
            {
                Intent i = new Intent(this,AI1player.class);
                startActivity(i);
            }
        else
                if(r3.isChecked())
                {
                    startActivity(new Intent(this,BluetoothChat.class));
                }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroy();
    }
}
