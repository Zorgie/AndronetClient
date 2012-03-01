package com.inet.andronet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AndronetLogin extends Activity {
	// Static fields
	
	private static int TIMEOUT_MILLISECONDS = 10000;
	
	// Fields.
	private String mUsername;
	private String mPass;
	private Socket mSocket;
	private String mServerAddress;
	private int mServerPort;
	private boolean mLoggedIn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		//new TimeTest().execute();

		mServerAddress = "85.225.75.73";
		mServerPort = 8082;

		initButtons();
	}

	/**
	 * Enables appropriate actions for each button.
	 */
	public void initButtons() {
		// Initiate the buttons from the GUI xml.
		Button loginBtn = (Button) this.findViewById(R.id.loginbutton);
		Button regBtn = (Button) this.findViewById(R.id.regbutton);

		// Initiate the text fields from the GUI xml.

		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText emailText = (EditText) AndronetLogin.this.findViewById(R.id.emailinput);
				EditText passText = (EditText) AndronetLogin.this.findViewById(R.id.passinput);
				mUsername = emailText.getText().toString();
				mPass = passText.getText().toString();
				if (connect(mServerAddress, mServerPort))
					Toast.makeText(AndronetLogin.this, "Connected!", 0).show();
				else
					Toast.makeText(AndronetLogin.this, "Connection failed.", 0).show();
				login();
			}
		});

		regBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText emailText = (EditText) AndronetLogin.this.findViewById(R.id.emailinput);
				EditText passText = (EditText) AndronetLogin.this.findViewById(R.id.passinput);
				mUsername = emailText.getText().toString();
				mPass = passText.getText().toString();
				if (connect(mServerAddress, mServerPort))
					Toast.makeText(AndronetLogin.this, "Connected!", 0).show();
				else
					Toast.makeText(AndronetLogin.this, "Connection failed.", 0).show();
				login();
			}
		});
	}

	private void enterChat() {
		if (!this.mLoggedIn)
			return;
		Connection.mSocket = mSocket;
		Intent intent = new Intent(this, AndronetClientActivity.class);
		startActivity(intent);
	}

	private boolean connect(String host, int port) {
		try {
			if (mSocket == null)
				mSocket = new Socket(host, port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void login() {
		if (mSocket == null)
			return;
		try {
			// Sends login command
			PrintStream out = new PrintStream(mSocket.getOutputStream());
			out.flush();
			out.println("LOGIN");
			out.println(mUsername);
			out.println(mPass);

			// Fire an async task to wait for response.
			new BGLogin().execute();
		} catch (IOException e) {
		}
	}

	private class BGLogin extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			String inputMessage;
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				long start = System.currentTimeMillis();
				while ((inputMessage = in.readLine()) != null && System.currentTimeMillis()-start < TIMEOUT_MILLISECONDS) {
					if (inputMessage.equals("LOGIN")) {
						inputMessage = in.readLine();
						if (inputMessage.equals("true")) {
							mLoggedIn = true;
							return true;
						}
						return false;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if(result){
				mLoggedIn = true;
				Toast.makeText(AndronetLogin.this, "Login successful!", 0).show();
				enterChat();
			}else
				Toast.makeText(AndronetLogin.this, "Login failed.", 0).show();
		}
	}
}
