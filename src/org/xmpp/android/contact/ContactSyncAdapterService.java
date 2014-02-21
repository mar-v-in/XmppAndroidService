package org.xmpp.android.contact;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ContactSyncAdapterService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return new ContactSyncAdapter(this).getSyncAdapterBinder();
	}
}
