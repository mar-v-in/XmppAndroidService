package org.xmpp.android.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
	public IBinder onBind(Intent intent) {
		return new Authenticator(this).getIBinder();
	}
}
