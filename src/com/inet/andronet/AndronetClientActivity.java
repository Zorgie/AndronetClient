package com.inet.andronet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import android.app.TabActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class AndronetClientActivity extends TabActivity {
	private EditText mInput;
	private TextView mDisplay;
	private TextView mUserlistDisplay;
	private Button mSendButton;
	private ScrollView mScroll;
	private PrintStream mOutput;
	private String mUsername;
	private MessageReceiver mMsgIn;
	private TabHost mTabHost;

	// public MyHandler mReceiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTabHost = getTabHost();

		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("Chat").setContent(R.id.chatlayout));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("User list").setContent(R.id.userlayout));

		mTabHost.setCurrentTab(0);

		getTabWidget().getChildAt(1).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mTabHost.setCurrentTab(1);
				mOutput.println("USERLIST");
			}
		});
		init();

		mInput = (EditText) findViewById(R.id.chatinput);
		mDisplay = (TextView) findViewById(R.id.display);
		mUserlistDisplay = (TextView) findViewById(R.id.userlist_display);
		mSendButton = (Button) findViewById(R.id.sendbutton);
		mScroll = (ScrollView) findViewById(R.id.scroll);

		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				send("" + mInput.getText());
				mInput.setText("");
			}
		});
	}

	private void init() {
		try {
			mOutput = new PrintStream(Connection.mSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mMsgIn = new MessageReceiver() {
			{
				execute();
			}
		};
	}

	public void receive(String message) {
		if (mDisplay.getText() != "") {
			mDisplay.setText(mDisplay.getText() + "\n" + message);
		} else {
			mDisplay.setText(message);
		}
		mScroll.post(new Runnable() {
			public void run() {
				mScroll.fullScroll(View.FOCUS_DOWN);
			}

		});
	}

	private void updateUserlist(String userlist) {
		mUserlistDisplay.setText(userlist);
	}

	public void send(String message) {
		mOutput.println("SEND");
		mOutput.println(message);
		mOutput.println("STOPSEND");
		mOutput.flush();
		mOutput.println("USERLIST");
	}
	


	private class MessageReceiver extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			String command;
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(Connection.mSocket.getInputStream()));
				while (true) {
					command = in.readLine();
					if (command.equals("SEND")) {
						StringBuilder sb = new StringBuilder();
						String username = in.readLine();
						String message = in.readLine();
						sb.append(username + ": " + message);
						while (!((message = in.readLine()).equals("STOPSEND"))) {
							sb.append(message);
						}
						publishProgress(new String[] { "SEND", sb.toString() });
					} else if (command.equals("USERLIST")) {
						StringBuilder userlist = new StringBuilder();
						String message;
						while (!((message = in.readLine()).equals("STOPUSERLIST"))) {
							userlist.append(message + "\n");
						}
						publishProgress(new String[] { "USERLIST", userlist.toString() });
					}
				}
			} catch (IOException e) {
				return false;
			}
		}

		protected void onProgressUpdate(String... progress) {
			String command = progress[0];
			if (command.equals("SEND"))
				receive(progress[1]);
			else if (command.equals("USERLIST")) {
				updateUserlist(progress[1]);
			}
		}

		protected void onPostExecute(Boolean result) {
			Toast.makeText(AndronetClientActivity.this, "Disconnected.", 0).show();
		}

	}
}