package org.xmpp.android.contact;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.StatusUpdates;
import android.util.Log;
import org.xmpp.android.account.AccountHelper;
import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.stanzas.PresenceStanza;

import java.util.List;

public class ContactHelper {
	private static final String TAG = "XMPP/ContactHelper";
	private static final String QUERY_AND_3 = "%s=? AND %s=? AND %s=?";
	private static Object contactsLock = new Object();

	private static int contractPresence(PresenceStanza presence) {
		if (presence.getPresenceType() == PresenceStanza.Type.unavailable) {
			return StatusUpdates.OFFLINE;
		}
		if ((presence.getPresenceType() == PresenceStanza.Type.unsubscribed) ||
				(presence.getPresenceType() == PresenceStanza.Type.error)) {
			return StatusUpdates.INVISIBLE;
		}
		if (presence.getShow() == null) {
			return StatusUpdates.AVAILABLE;
		}
		switch (presence.getShow()) {
			case away:
				return StatusUpdates.IDLE;
			case xa:
				return StatusUpdates.AWAY;
			case dnd:
				return StatusUpdates.DO_NOT_DISTURB;
			default:
				return StatusUpdates.AVAILABLE;
		}
	}

	public static long findRawContact(Context context, Account account, Jid jid) {
		Cursor query = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
				new String[]{RawContacts._ID},
				String.format(QUERY_AND_3,
						RawContacts.ACCOUNT_TYPE,
						RawContacts.ACCOUNT_NAME,
						RawContacts.SOURCE_ID),
				new String[]{AccountHelper.ACCOUNT_TYPE, account.name,
						jid.withoutResource()}, null);
		while (query.moveToNext() && !query.isAfterLast()) {
			long id = query.getLong(0);
			query.close();
			return id;
		}
		query.close();
		return -1;
	}

	public static void insertContactFromItem(Context context, Roster.Item item, Account account) {
		if (item == null || item.getJid() == null) return;
		ContentValues values = new ContentValues();
		values.put(RawContacts.ACCOUNT_TYPE, AccountHelper.ACCOUNT_TYPE);
		values.put(RawContacts.ACCOUNT_NAME, account.name);
		values.put(RawContacts.SOURCE_ID, item.getJid().toString());
		Uri result = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		Log.d(TAG, "insertContact: " + item.toString() + " => " + result.toString());
		long id = Long.parseLong(result.getLastPathSegment());
		values = new ContentValues();
		values.put(Im.RAW_CONTACT_ID, id);
		values.put(Im.MIMETYPE,
				Im.CONTENT_ITEM_TYPE);
		values.put(Im.DATA, item.getJid().withoutResource());
		values
				.put(Im.PROTOCOL, Im.PROTOCOL_JABBER);
		result = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
		Log.d(TAG, "insertContact: " + item.toString() + " => " + result.toString());
		values.clear();
		values.put(StructuredName.RAW_CONTACT_ID, id);
		values.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.DISPLAY_NAME, item.getName() != null ? item.getName() : item.getJid().withoutResource());
		result = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
		Log.d(TAG, "insertContact: " + item.toString() + " => " + result.toString());
	}

	public static void storePresence(Context context, PresenceStanza presence) {
		if ((presence.getTo() != null) && (presence.getFrom() != null) &&
				!presence.getFrom().withoutResource().equalsIgnoreCase(presence.getTo().withoutResource())) {
			Account account = AccountHelper.getAccount(context, presence.getTo());
			if (account != null) {
				synchronized (contactsLock) {
					long id = findRawContact(context, account, presence.getFrom());
					if (id == -1) {
						// Ignore
						//insertContactFromPresence(context, presence, account);
					} else {
						updateContactPresence(context, presence, id);
					}
				}
			}
		}
	}

	public static void storeRosterItems(Context context, Jid to, List<Roster.Item> items) {
		if (to != null) {
			Account account = AccountHelper.getAccount(context, to);
			if (account != null) {
				synchronized (contactsLock) {
					for (Roster.Item item : items) {
						long id = findRawContact(context, account, item.getJid());
						if (id == -1) {
							insertContactFromItem(context, item, account);
						} else {
							updateContactFromItem(context, item, id);
						}
					}
				}
			}
		} else {
			Log.w(TAG, "storeRosterItems: to is: " + to);
		}
	}

	private static void updateContactFromItem(Context context, Roster.Item item, long id) {
		Log.d(TAG, "updateContact: " + item.toString());
		//TODO: Implement
	}

	public static void updateContactPresence(Context context, PresenceStanza presence, long id) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(StatusUpdates.PROTOCOL, Im.PROTOCOL_JABBER);
		contentValues.put(StatusUpdates.IM_HANDLE, presence.getFrom().withoutResource());
		contentValues.put(StatusUpdates.IM_ACCOUNT, presence.getTo().withoutResource());
		contentValues.put(StatusUpdates.PRESENCE, contractPresence(presence));
		if ((presence.getStatus() != null) && !presence.getStatus().isEmpty()) {
			contentValues.put(StatusUpdates.STATUS, presence.getStatus());
		} else {
			contentValues.put(StatusUpdates.STATUS, "");
		}
		Uri insert = context.getContentResolver().insert(StatusUpdates.CONTENT_URI, contentValues);
		Log.d(TAG, "updatePresence: " + presence.toString() + " => " + insert.toString());
		//TODO: Implement
	}
}
