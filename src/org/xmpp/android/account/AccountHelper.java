package org.xmpp.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import org.xmpp.android.shared.Jid;

public class AccountHelper {
	private static final String TAG = "XMPP/AccountHelper";

	public static final String ACCOUNT_TYPE = "org.xmpp";
	public static final String KEY_RESOURCE = "resource";

	public static Jid buildJid(Context context, Account account) {
		AccountManager am = AccountManager.get(context);
		return Jid.builderOf(account.name).password(am.getPassword(account))
				  .resource(am.getUserData(account, KEY_RESOURCE)).build();
	}

	public static Bundle createAccount(Context context, Jid jid) {
		Bundle b = new Bundle();
		b.putString(KEY_RESOURCE, jid.getResource());
		Account account = new Account(jid.withoutResource(), ACCOUNT_TYPE);
		AccountManager accountManager = AccountManager.get(context);
		accountManager.addAccountExplicitly(account, jid.getPassword(), b);
		b = new Bundle();
		b.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
		b.putString(AccountManager.KEY_ACCOUNT_NAME, jid.withoutResource());
		return b;
	}

	public static Account getAccount(Context context, Jid jid) {
		Account[] accounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
		for (Account account : accounts) {
			if (account.name.equalsIgnoreCase(jid.withoutResource())) {
				return account;
			}
		}
		Log.w(TAG, "no account for jid: "+jid);
		return null;
	}

	public static Account[] getAccounts(Context context) {
		return AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
	}
}
