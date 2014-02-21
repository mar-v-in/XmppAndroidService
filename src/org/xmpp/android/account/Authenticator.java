package org.xmpp.android.account;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Authenticator extends AbstractAccountAuthenticator {

	private Context context;

	public Authenticator(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
							 String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		final Intent intent = new Intent(context, AuthenticatorActivity.class);
		intent.setAction(AuthenticatorActivity.ADD_ACCOUNT);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
			throws NetworkErrorException {
		return null; //TODO: Implement
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null; //TODO: Implement
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
							   Bundle options) throws NetworkErrorException {
		return null; //TODO: Implement
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null; //TODO: Implement
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
			throws NetworkErrorException {
		return null; //TODO: Implement
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
									Bundle options) throws NetworkErrorException {
		return null; //TODO: Implement
	}
}
