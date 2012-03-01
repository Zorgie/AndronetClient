package com.inet.andronet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AndronetLogin extends Activity {
	// Static fields

	private static int TIMEOUT_MILLISECONDS = 10000;

	// Fields.
	private String mUsername;
	private String mPass;
	private String mServerAddress;
	private int mServerPort;
	private boolean mLoggedIn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mServerAddress = "130.229.163.91";
		mServerPort = 8081;

		initButtons();
	}

	/**
	 * Enables appropriate actions for each button.
	 */
	public void initButtons() {
		// Initiate the buttons from the GUI xml.
		Button loginBtn = (Button) this.findViewById(R.id.loginbutton);
		Button regBtn = (Button) this.findViewById(R.id.regbutton);
		Button serverBtn = (Button) this.findViewById(R.id.changeserverbutton);

		// Initiate the text fields from the GUI xml.

		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText emailText = (EditText) AndronetLogin.this.findViewById(R.id.emailinput);
				EditText passText = (EditText) AndronetLogin.this.findViewById(R.id.passinput);
				mUsername = emailText.getText().toString();
				mPass = passText.getText().toString();
				new BGLogin().execute("LOGIN");
			}
		});

		regBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText emailText = (EditText) AndronetLogin.this.findViewById(R.id.emailinput);
				EditText passText = (EditText) AndronetLogin.this.findViewById(R.id.passinput);
				mUsername = emailText.getText().toString();
				mPass = passText.getText().toString();
				new BGLogin().execute("REGISTER");
			}
		});

		serverBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeServer();
			}
		});
	}

	private void enterChat() {
		if (!this.mLoggedIn)
			return;
		Intent intent = new Intent(this, AndronetClientActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Connection.mSocket != null) {
			try {
				PrintStream out = new PrintStream(Connection.mSocket.getOutputStream());
				out.println("LOGOUT");
			} catch (IOException e) {
			}
		}
	}

	private void changeServer() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New server");
		alert.setMessage("Enter the address of the server");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setText(mServerAddress);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				mServerAddress = value;
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

	private class BGLogin extends AsyncTask<String, String, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			String cmd = params[0];
			// Connects
			try {
				Connection.mSocket = new Socket(mServerAddress, mServerPort);
				publishProgress(new String[] { "TOAST", "Connected!" });
			} catch (Exception e) {
				publishProgress(new String[] { "TOAST", "Connection failed." });
				return false;
			}

			// Sends request
			if (Connection.mSocket == null)
				return false;
			try {
				// Sends login command
				PrintStream out = new PrintStream(Connection.mSocket.getOutputStream());
				out.println(cmd);
				out.println(mUsername);
				out.println(mPass);
			} catch (Exception e) {
			}

			String inputMessage;
			try { 
				BufferedReader in = new BufferedReader(new InputStreamReader(Connection.mSocket.getInputStream()));
				while (true) {
					if (in.ready()) {
						inputMessage = in.readLine();
						if (inputMessage.equals("LOGIN") || inputMessage.equals("REGISTER")) {
							inputMessage = in.readLine();
							if (inputMessage.equals("true")) {
								mLoggedIn = true;
								return true;
							}
							return false;
						} else {
							return false;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress.length < 2)
				return;
			String cmd = progress[0];
			String details = progress[1];
			if (cmd.equals("TOAST")) {
				Toast.makeText(AndronetLogin.this, details, 0).show();
			}
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				mLoggedIn = true;
				Toast.makeText(AndronetLogin.this, "Login successful!", 0).show();
				enterChat();
			} else {
				try {
					// Always be polite.
					PrintStream out = new PrintStream(Connection.mSocket.getOutputStream());
					out.println("LOGOUT");
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(AndronetLogin.this, "Login failed.", 0).show();
			}
		}
	}
}
