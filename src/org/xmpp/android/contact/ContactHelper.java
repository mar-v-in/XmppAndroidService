package org.xmpp.android.contact;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import org.xmpp.android.account.AccountHelper;
import org.xmpp.android.contact.roster.Item;
import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.stanzas.PresenceStanza;

import java.util.List;

public class ContactHelper {
	private static final String TAG = "XMPP/ContactHelper";
	private static final String QUERY_AND_3 = "%s=? AND %s=? AND %s=?";
	private static Object contactsLock = new Object();

	private static int contractPresence(PresenceStanza presence) {
		if (presence.getPresenceType() == PresenceStanza.Type.unavailable) {
			return ContactsContract.StatusUpdates.OFFLINE;
		}
		if ((presence.getPresenceType() == PresenceStanza.Type.unsubscribed) ||
			(presence.getPresenceType() == PresenceStanza.Type.error)) {
			return ContactsContract.StatusUpdates.INVISIBLE;
		}
		if (presence.getShow() == null) {
			return ContactsContract.StatusUpdates.AVAILABLE;
		}
		switch (presence.getShow()) {
			case away:
				return ContactsContract.StatusUpdates.IDLE;
			case xa:
				return ContactsContract.StatusUpdates.AWAY;
			case dnd:
				return ContactsContract.StatusUpdates.DO_NOT_DISTURB;
			default:
				return ContactsContract.StatusUpdates.AVAILABLE;
		}
	}

	public static long findRawContact(Context context, Account account, Jid jid) {
		Cursor query = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
														  new String[]{ContactsContract.RawContacts._ID},
														  String.format(QUERY_AND_3,
																		ContactsContract.RawContacts.ACCOUNT_TYPE,
																		ContactsContract.RawContacts.ACCOUNT_NAME,
																		ContactsContract.RawContacts.SOURCE_ID),
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

	public static void insertContactFromItem(Context context, Item item, Account account) {
		if (item == null || item.getJid() == null) return;
		ContentValues contentValues = new ContentValues();
		contentValues.put(ContactsContract.RawContacts.ACCOUNT_TYPE, AccountHelper.ACCOUNT_TYPE);
		contentValues.put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name);
		contentValues.put(ContactsContract.RawContacts.SOURCE_ID, item.getJid().toString());
		contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
						  item.getName() != null ? item.getName() : item.getJid().withoutResource());
		Uri result = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
		Log.d(TAG, "insertContact: " + item.toString() + " => " + result.toString());
		long id = Long.parseLong(result.getLastPathSegment());
		contentValues = new ContentValues();
		contentValues.put(ContactsContract.CommonDataKinds.Im.RAW_CONTACT_ID, id);
		contentValues.put(ContactsContract.CommonDataKinds.Im.MIMETYPE,
						  ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
		contentValues.put(ContactsContract.CommonDataKinds.Im.DATA, item.getJid().withoutResource());
		contentValues
				.put(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER);
		result = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
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

	public static void storeRosterItems(Context context, Jid to, List<Item> items) {
		if (to != null) {
			Account account = AccountHelper.getAccount(context, to);
			if (account != null) {
				synchronized (contactsLock) {
					for (Item item : items) {
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
			Log.w(TAG, "storeRosterItems: to is: "+to);
		}
	}

	private static void updateContactFromItem(Context context, Item item, long id) {
		Log.d(TAG, "updateContact: " + item.toString());
		//TODO: Implement
	}

	public static void updateContactPresence(Context context, PresenceStanza presence, long id) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(ContactsContract.StatusUpdates.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER);
		contentValues.put(ContactsContract.StatusUpdates.IM_HANDLE, presence.getFrom().withoutResource());
		contentValues.put(ContactsContract.StatusUpdates.IM_ACCOUNT, presence.getTo().withoutResource());
		contentValues.put(ContactsContract.StatusUpdates.PRESENCE, contractPresence(presence));
		if ((presence.getStatus() != null) && !presence.getStatus().isEmpty()) {
			contentValues.put(ContactsContract.StatusUpdates.STATUS, presence.getStatus());
		}
		Uri insert = context.getContentResolver().insert(ContactsContract.StatusUpdates.CONTENT_URI, contentValues);
		Log.d(TAG, "updatePresence: " + presence.toString() + " => " + insert.toString());
		//TODO: Implement
	}
}
