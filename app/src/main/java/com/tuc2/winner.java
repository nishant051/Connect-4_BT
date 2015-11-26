package com.tuc2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Nishant on 06-Jul-15.
 */
public class winner extends Activity {
   TextView t;
    @Override
    protected void onCreate(Bundle s) {

        super.onCreate(s);
        setContentView(R.layout.tuc_win);
        t = (TextView)findViewById(R.id.tv);
        Bundle b = getIntent().getExtras();
        t.setText(b.getString("w"));
    }
    public void playagain(View v)
    {
        startActivity(new Intent(this,selection.class));
        finish();
    }
    public void exit(View v)
    {
        finish();
    }

}
