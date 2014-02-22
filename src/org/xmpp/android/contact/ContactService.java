package org.xmpp.android.contact;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import org.xmpp.android.contact.roster.Query;
import org.xmpp.android.shared.stanzas.IqStanza;
import org.xmpp.android.shared.stanzas.PresenceStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class ContactService extends Service {
	public static final String ACTION_INCOMING_PRESENCE = ContactService.class.getName() + ".INCOMING_PRESENCE";
	public static final String ACTION_INCOMING_ROSTER = ContactService.class.getName() + ".INCOMING_ROSTER";
	private static final String TAG = "XMPP/ContactService";

	static {
		Query.register();
	}

	public static boolean intentTypeMatches(Intent intent, Stanza.StanzaType type) {
		if (!intent.hasExtra("stanza")) {
			return false;
		}
		Parcelable parcelable = intent.getParcelableExtra("stanza");
		if (!(parcelable instanceof XmppStanza)) {
			return false;
		}
		Stanza stanza = ((XmppStanza) parcelable).encapsulate();
		if (!(stanza instanceof PresenceStanza)) {
			return false;
		}
		return true;
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "handling intent: "+intent);
		if (intent.getAction().equals(ACTION_INCOMING_PRESENCE)) {
			ContactHelper.storePresence(this, (PresenceStanza) ((XmppStanza) intent.getParcelableExtra("stanza"))
					.encapsulate());
		} else if (intent.getAction().equals(ACTION_INCOMING_ROSTER)) {
			XmppStanza stanza = (XmppStanza) intent.getParcelableExtra("stanza");
			Log.d(TAG, stanza.buildNoChildTag());
			IqStanza iq = (IqStanza) (stanza).encapsulate();
			ContactHelper.storeRosterItems(this, iq.getTo(), ((Query) iq.getSubStanza()).getItems());
		}
		return START_STICKY;
	}

	public static class PresenceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			assert intentTypeMatches(intent, PresenceStanza.TYPE);
			Intent forwardIntent = new Intent(intent);
			forwardIntent.setAction(ACTION_INCOMING_PRESENCE);
			forwardIntent.setClass(context, ContactService.class);
			context.startService(forwardIntent);
		}
	}

	public static class RosterReceiver extends BroadcastReceiver {

		static {
			Query.register();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "handling roster: " + intent);
			assert intentTypeMatches(intent, IqStanza.TYPE);
			IqStanza iq = (IqStanza) ((XmppStanza) intent.getParcelableExtra("stanza")).encapsulate();
			if (iq.getIqType() == IqStanza.Type.result && (iq.getSubStanza() instanceof Query)) {
				Intent forwardIntent = new Intent(intent);
				forwardIntent.setAction(ACTION_INCOMING_ROSTER);
				forwardIntent.setClass(context, ContactService.class);
				context.startService(forwardIntent);
			} else if (iq.getSubStanza() != null) {
				Log.d(TAG,
						"iqtype: " + iq.getIqType() + ", subType: " + iq.getSubStanza().getStanzaType() + ", subClass: " +
								iq.getSubStanza().getClass());
			} else {
				Log.d(TAG, "no sub stanza provided: " + iq);
			}
		}
	}
}
