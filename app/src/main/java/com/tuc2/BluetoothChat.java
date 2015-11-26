/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuc2;


import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	//playing logic
	int bordsize = 8;
	TableLayout layout;
	boolean turn = false;
	int b[][] = new int[bordsize][bordsize];
	int score1=0;
	int score2=0;
	int movecounter=0;
	TextView tv1,tv2;
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
//	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	//private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.main);

		//final LinearLayout lm = (LinearLayout) findViewById(R.id.dnm2);
		TableRow.LayoutParams params = new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);

		layout = (TableLayout)findViewById(R.id.tbl);
		//layout.setLayoutParams( new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) );
		layout.setPadding(1,1,1,1);

		for (int f=0; f<=7; f++) {
			TableRow tr = new TableRow(this);
			//tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			tr.setWeightSum(80);
			for (int c=0; c<=7; c++) {
				//ImageButton b = new ImageButton(this);
				Button b = new Button(this);
				// b.setText(""+f+c);
				b.setTextSize(15.0f);
				b.setTextColor(Color.rgb(100, 300, 300));
                params.weight=10;
               b.setLayoutParams(params);
			//b.setHeight(50);
			//	b.setWidth(50);
				// b.setOnClickListener(this);
				tr.addView(b);
				// b.setImageResource(R.drawable.button_bg);
			} // for
			layout.addView(tr);

		} // for
	//	lm.setBackgroundResource(R.drawable.steel_bg);
		//lm.addView(layout);
		tv1 = (TextView) findViewById(R.id.scrmp1);

		tv2 = (TextView) findViewById(R.id.scrmp2);





		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		//mConversationArrayAdapter = new ArrayAdapter<String>(this,R.layout.message);
		//mConversationView = (ListView) findViewById(R.id.in);
		//mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		//mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		//mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		//mSendButton = (Button) findViewById(R.id.button_send);
	/*	mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);

			}
		});*/
		TableLayout T =layout;
		for (int y = 0; y < T.getChildCount(); y++) {
			if (T.getChildAt(y) instanceof TableRow) {
				TableRow R = (TableRow) T.getChildAt(y);
				for (int x = 0; x < R.getChildCount(); x++) {
					View V = R.getChildAt(x); // In our case this will be each button on the grid
					V.setOnClickListener(new PlayOnClick(y, x));
				}
			}
		}
		resetButtons();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}
	private class PlayOnClick implements View.OnClickListener {

		private int x = 0;
		private int y = 0;

		public PlayOnClick(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void onClick(View view) {
			if (view instanceof Button) {
				Button B = (Button) view;
				b[x][y] = turn ? 0 : 1;
				String ms = Integer.toString(x) + "/" + Integer.toString(y);
				sendMessage(ms);
				movecounter++;
				B.setText(turn ? "O" : "X");
				B.setEnabled(false);
				if(turn)
				{
					score2+=ch3(b,x,y);
					tv2.setText("O: "+score2);

				}
				else
				{
					score1+=ch3(b,x,y);
					tv1.setText("X: "+score1);
				}
				TableLayout T = layout;
				if (T.getChildAt(x-1) instanceof TableRow) {
					TableRow R = (TableRow) T.getChildAt(x-1);
					if (R.getChildAt(x-1) instanceof Button) {
						Button q = (Button) R.getChildAt(y);
						q.setEnabled(true);
					}
				}
				if(mChatService!=null) {
					touchable(false);
				}
				turn = !turn;

				int w = ch(b,x,y);
				if(w==1)
				{
					Intent i = new Intent(BluetoothChat.this,winner.class);
					Bundle b = new Bundle();
					b.putString("w","Player X win");
					i.putExtras(b);
					startActivity(i);
					finish();
				}
				else
				if(w==0)
				{
					Intent i = new Intent(BluetoothChat.this,winner.class);
					Bundle b = new Bundle();
					b.putString("w","Player O win");
					i.putExtras(b);
					startActivity(i);
					finish();
				}
				else
				{
					if(movecounter==64)
					{
						Intent i = new Intent(BluetoothChat.this,winner.class);
						Bundle b = new Bundle();
						String s=null;
						if(score1>score2) {
							s = "Player X won"+Integer.toString(score1);
						}
						else
						if(score1<score2) {
							s = "Player O won: " + Integer.toString(score2);
						}
						else
							s="Draw";
						b.putString("w",s);
						i.putExtras(b);
						startActivity(i);
						finish();
					}
				}



			}

		}

	}
	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	public void  sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			//mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	/*private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};*/

	private final void setStatus(int resId) {
		 ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}
	public void resetButtons() {
		TableLayout T = layout;
		for (int y = 0; y < T.getChildCount(); y++) {
			if (T.getChildAt(y) instanceof TableRow) {
				TableRow R = (TableRow) T.getChildAt(y);
				for (int x = 0; x < R.getChildCount(); x++) {
					if (R.getChildAt(x) instanceof Button) {
						Button B = (Button) R.getChildAt(x);
						b[y][x] = -1;
						// B.setText("OX");
						B.setEnabled(false);
						score1=0;
						score2=0;
						movecounter=0;
						tv1.setText("X: "+score1);
						tv2.setText("O:"+score2);
					}
				}
			}
		}
		if (T.getChildAt(7) instanceof TableRow) {
			TableRow R = (TableRow) T.getChildAt(7);
			for (int x = 0; x < R.getChildCount(); x++) {
				if (R.getChildAt(x) instanceof Button) {
					Button B = (Button) R.getChildAt(x);
					// B.setText("OX");
					B.setEnabled(true);
				}

			}


		}
		touchable(true);
	}
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					String snd=null;
					if(!turn)
					{
						snd = "You have X";
					}
					else
					{
						snd="You have O";
					}
					handshake(snd);
				//	mConversationArrayAdapter.clear();
					resetButtons();

					break;
				case BluetoothChatService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				/*byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);*/
				break;
			case MESSAGE_READ:
				touchable(true);
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				//mConversationArrayAdapter.add(mConnectedDeviceName + ":  "+ readMessage);
				if(readMessage.contains("/")) {
					int x = Integer.parseInt(readMessage.split("/")[0]);
					int y = Integer.parseInt(readMessage.split("/")[1]);

					b[x][y] = turn ? 0 : 1;
					movecounter++;
					TableLayout T = layout;
					if (T.getChildAt(x) instanceof TableRow) {
						TableRow R = (TableRow) T.getChildAt(x);
						if (R.getChildAt(x) instanceof Button) {
							Button B = (Button) R.getChildAt(y);
							B.setText(turn ? "O" : "X");
							B.setEnabled(false);
						}
					}

					if (turn) {
						score2 += ch3(b, x, y);
						tv2.setText("O: " + score2);

					} else {
						score1 += ch3(b, x, y);
						tv1.setText("X: " + score1);
					}
					if (T.getChildAt(x - 1) instanceof TableRow) {
						TableRow R = (TableRow) T.getChildAt(x - 1);
						if (R.getChildAt(x - 1) instanceof Button) {
							Button q = (Button) R.getChildAt(y);
							q.setEnabled(true);
						}
					}

					turn = !turn;

					int w = ch(b, x, y);
					if (w == 1) {
						Intent i = new Intent(BluetoothChat.this, winner.class);
						Bundle b = new Bundle();
						b.putString("w", "Player X win");
						i.putExtras(b);
						startActivity(i);
						finish();
					} else if (w == 0) {
						Intent i = new Intent(BluetoothChat.this, winner.class);
						Bundle b = new Bundle();
						b.putString("w", "Player O win");
						i.putExtras(b);
						startActivity(i);
						finish();
					} else {
						if (movecounter == 64) {
							Intent i = new Intent(BluetoothChat.this, winner.class);
							Bundle b = new Bundle();
							String s = null;
							if (score1 > score2) {
								s = "Player X won" + Integer.toString(score1);
							} else if (score1 < score2) {
								s = "Player O won: " + Integer.toString(score2);
							} else
								s = "Draw";
							b.putString("w", s);
							i.putExtras(b);
							startActivity(i);
							finish();
						}
					}

				}
				else
				{
					Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_LONG).show();
						if(readMessage.contains("X"))
						{
							turn = false;
						}
						else
						if(readMessage.contains("O"))
						{
							turn = true;
						}


				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	private void handshake(String snd) {
		sendMessage(snd);
	}

	public void touchable(boolean t)
{
	TableLayout T = layout;
	for (int y = 0; y < T.getChildCount(); y++) {
		if (T.getChildAt(y) instanceof TableRow) {
			TableRow R = (TableRow) T.getChildAt(y);
			for (int x = 0; x < R.getChildCount(); x++) {
				if (R.getChildAt(x) instanceof Button) {
					Button B = (Button) R.getChildAt(x);
				         B.setClickable(t);
				}
			}
		}
	}
}
	private void defineturn(Intent d) {
		char c = d.getCharExtra("symbol",'N');
		if(c=='X')
		{
			turn = false;
		}
		else
			if(c=='O')
			{
				turn = true;
			}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
				defineturn(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;

		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}
	int ch(int b[][],int x,int y)
	{
		if(y>-1 && y+3<bordsize && b[x][y]==b[x][y+1]&&b[x][y+1]==b[x][y+2]&&b[x][y+2]==b[x][y+3]) // *RRR
			return b[x][y];
		if(y-1>-1 &&y+2<bordsize && b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1] &&b[x][y+1]==b[x][y+2])   // l*RR
			return b[x][y];
		if(y-2>-1 && y+1<bordsize && b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1])   // ll*R
			return b[x][y];
		if(y-3>-1 && y<bordsize && b[x][y-3]==b[x][y-2]&&b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y])    // LLL*
			return b[x][y];
		if(x+3<bordsize && y+3<bordsize && b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2]&&b[x+2][y+2]==b[x+3][y+3])  // *LLL
			return b[x][y];
		if(x-1>-1&&y-1>-1&&x+2<bordsize&&y+2<bordsize && b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2])  // H*LL
			return b[x][y];
		if(x-2>-1&&y-2>-1&&x+1<bordsize&&y+1<bordsize &&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1])  //HH*L
			return b[x][y];
		if(x-3>-1&&y-3>-1  &&b[x-3][y-3]==b[x-2][y-2]&&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y])  //HHH*
			return b[x][y];
		if(x+3<bordsize&&y-3>-1 &&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2]&&b[x+2][y-2]==b[x+3][y-3])  //*DDD
			return b[x][y];
		if(x-1>-1&&y+1<bordsize&&x+2<bordsize&&y-2>-1 &&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2])  //U*DD
			return b[x][y];
		if(x-2>-1&&y+2<bordsize&&x+1<bordsize&&y-1>-1 &&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]) //UU*D
			return b[x][y];
		if(x-3>-1&&y+3<bordsize  &&b[x-3][y+3]==b[x-2][y+2]&&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y])  //UUU*
			return b[x][y];
		if(x+3<bordsize && b[x][y]==b[x+1][y]&&b[x+1][y]==b[x+2][y]&&b[x+2][y]==b[x+3][y])  //VVVV
			return b[x][y];


		return -1;
	}
	int ch3(int b[][], int x, int y) {
		int k=0;
		if (y + 1 < bordsize && y - 1 > -1 && (b[x][y + 1] == b[x][y]) && (b[x][y - 1] == b[x][y])) //left right 1 step
			k++;
		if (y + 1 < bordsize && y + 2 < bordsize && (b[x][y + 1] == b[x][y] && b[x][y + 2] == b[x][y]))   //right 2 step
			k++;
		if (y - 1 > -1 && y - 2 > -1 && (b[x][y - 1] == b[x][y] && b[x][y - 2] == b[x][y]))   //left 2 step
			k++;
		if (x + 1 < bordsize && x + 2 < bordsize && b[x + 1][y] == b[x][y] && b[x + 2][y] == b[x][y])    //down 2 step
			k++;
		if (x - 1 > -1 && y + 1 < bordsize && x + 1 < bordsize && y - 1 > -1 && b[x - 1][y + 1] == b[x][y] && b[x + 1][y - 1] == b[x][y])  //diag/ 1step up down
			k++;
		if (x - 1 > -1 && y - 1 > -1 && x + 1 < bordsize && y + 1 < bordsize && b[x - 1][y - 1] == b[x][y] && b[x + 1][y + 1] == b[x][y])  //diag\ 1step up down
			k++;
		if (x - 1 > -1 && y + 1 < bordsize && x - 2 > -1 && y + 2 < bordsize && b[x - 1][y + 1] == b[x][y] && b[x - 2][y + 2] == b[x][y])  //diag/ 2step up
			k++;
		if (x + 1 < bordsize && y - 1 > -1 && x + 2 < bordsize && y - 2 > -1 && b[x + 1][y - 1] == b[x][y] && b[x + 2][y - 2] == b[x][y])  //diag/ 2step down
			k++;
		if (x + 1 < bordsize && y + 1 < bordsize && x + 2 < bordsize && y + 2 < bordsize && b[x + 1][y + 1] == b[x][y] && b[x + 2][y + 2] == b[x][y])  //diag\ 2step down
			k++;
		if (x - 1 > -1 && y - 1 > -1 && x - 2 > -1 && y - 2 > -1 && b[x - 1][y - 1] == b[x][y] && b[x - 2][y - 2] == b[x][y])  //diag\ 2step
			k++;


		return k;
	}

}
