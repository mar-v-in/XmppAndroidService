package org.xmpp.android.account;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import org.xmpp.android.R;
import org.xmpp.android.connection.XmppConnection;
import org.xmpp.android.shared.Jid;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	public static final String ADD_ACCOUNT = "org.xmpp.android.ADD_ACCOUNT";
	private static final String KEY_ADDRESS = AccountManager.KEY_ACCOUNT_NAME;
	private static final String KEY_PASSWORD = AccountManager.KEY_PASSWORD;
	private static final String KEY_RESOURCE = AccountHelper.KEY_RESOURCE;
	private EditText address;
	private EditText password;
	private EditText resource;
	private View loginForm;
	private View loginProgress;
	private Jid jid;
	private LoginTask task;

	private void attemptLogin() {
		if (task != null) {
			return;
		}
		String address = this.address.getText().toString();
		String password = this.password.getText().toString();
		String resource = this.resource.getText().toString();

		EditText todo = null;

		if (password.isEmpty()) {
			this.password.setError(getString(R.string.error_field_required));
			todo = this.password;
		}

		if (address.isEmpty()) {
			this.address.setError(getString(R.string.error_field_required));
			todo = this.address;
		} else if (!address.contains("@")) {
			this.address.setError(getString(R.string.error_invalid_address));
			todo = this.address;
		}

		if (todo != null) {
			todo.requestFocus();
			return;
		}

		jid = Jid.builderOf(address).resource(resource).password(password).build();

		task = new LoginTask();
		showProgress();
		task.execute();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_account);
		address = (EditText) findViewById(R.id.edit_address);
		password = (EditText) findViewById(R.id.edit_password);
		resource = (EditText) findViewById(R.id.edit_resource);
		loginForm = findViewById(R.id.view_login_form);
		loginProgress = findViewById(R.id.view_login_progress);
		if (savedInstanceState != null) {
			address.setText(savedInstanceState.getString(KEY_ADDRESS));
			password.setText(savedInstanceState.getString(KEY_PASSWORD));
			resource.setText(savedInstanceState.getString(KEY_RESOURCE));
		}
		findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptLogin();
			}
		});
		findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	private void showForm() {
		loginProgress.setVisibility(View.GONE);
		loginForm.setVisibility(View.VISIBLE);
	}

	private void showProgress() {
		loginProgress.setVisibility(View.VISIBLE);
		loginForm.setVisibility(View.GONE);
	}

	private class LoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return XmppConnection.testJid(jid);
		}

		@Override
		protected void onCancelled() {
			showForm();
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				Bundle b = AccountHelper.createAccount(AuthenticatorActivity.this, jid);
				setAccountAuthenticatorResult(b);
				setResult(RESULT_OK);
				finish();
			} else {
				password.setError(getString(R.string.error_login_invalid));
				password.requestFocus();
				showForm();
				task = null;
			}
		}
	}

}