package org.xmpp.android.contact;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {
	private static final String TAG = "XMPP/ContactSyncAdapter";

	public ContactSyncAdapter(Context context) {
		super(context, true);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
							  SyncResult syncResult) {
		Log.d(TAG, "onPerformSync: "+account);
	}
}
