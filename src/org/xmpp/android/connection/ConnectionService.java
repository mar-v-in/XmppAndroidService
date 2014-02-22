package org.xmpp.android.connection;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.xmpp.android.Manifest;
import org.xmpp.android.account.AccountHelper;
import org.xmpp.android.contact.Roster;
import org.xmpp.android.shared.IConnectionService;
import org.xmpp.android.shared.stanzas.IqStanza;
import org.xmpp.android.shared.stanzas.PresenceStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.io.IOException;

public class ConnectionService extends Service {
	private static final String TAG = "XMPP/ConnectionService";
	private Connection connection;
	private Thread connectionThread;

	public IBinder onBind(Intent intent) {
		return new Interface();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if ((connection == null) || (connectionThread == null)) {
			final Account[] accounts = AccountHelper.getAccounts(this);
			if (accounts.length != 1) {
				Log.d(TAG, "can handle only one account for now");
				return START_NOT_STICKY;
			}
			connectionThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						connection = XmppConnection
								.open(AccountHelper.buildJid(ConnectionService.this, accounts[0]), new Broadcaster());
						new IqStanza(connection.getJid(), "get", new Roster.Query()).asXmppStanza().pushTag(connection);
						new PresenceStanza().asXmppStanza().pushTag(connection);
					} catch (IOException e) {
						Log.w(TAG, e);
					}
				}
			});
			connectionThread.start();
		}
		return START_STICKY;
	}

	private class Broadcaster implements ConnectionListener {

		@Override
		public void stanzaRead(Stanza stanza) {
			Intent i = new Intent("org.xmpp.android.INCOMING_STANZA");
			i.putExtra("stanza", stanza.asXmppStanza());
			i.addCategory("org.xmpp.android.stanza." + stanza.getStanzaType().getElement());
			Log.d(TAG, "sending Intent: " + i);
			sendBroadcast(i, Manifest.permission.READ_STANZA);
		}
	}

	private class Interface extends IConnectionService.Stub {

		private void enforceCallingPermission(String permission) throws RemoteException {
			if (checkCallingPermission(permission) == PackageManager.PERMISSION_DENIED) {
				throw new RemoteException("Required permission " + permission + "not found on " + getCallingUid());
			}
		}

		@Override
		public void send(XmppStanza xmppStanza) throws RemoteException {
			Stanza stanza = xmppStanza.encapsulate();
			enforceCallingPermission(Manifest.permission.SEND_STANZA);
			stanza.asXmppStanza().pushTag(connection);
		}
	}
}
