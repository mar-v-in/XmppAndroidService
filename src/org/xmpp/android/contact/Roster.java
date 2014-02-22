package org.xmpp.android.contact;

import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.BaseStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Roster as described in RFC 6121 Section 2.
 */
public class Roster {
	public static void register() {
		Query.register();
	}

	public static class Group extends BaseStanza {
		public static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_ROSTER, "group");

		public Group(XmppStanza stanza) {
			super(stanza);
			assert TYPE.equals(stanza.getStanzaType());
		}

		public static void register() {
			XmppStanza.register(TYPE, Group.class);
		}

		public String getName() {
			return stanza.getSubText();
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}

	public static class Item extends BaseStanza {
		public static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_ROSTER, "item");

		public Item(XmppStanza stanza) {
			super(stanza);
			assert TYPE.equals(stanza.getStanzaType());
		}

		public boolean getApproved() {
			if ("true".equalsIgnoreCase(stanza.getAttribute("approved"))) {
				return true;
			}
			return false;
		}

		public Ask getAsk() {
			return Ask.of(stanza.getAttribute("ask"));
		}

		public Jid getJid() {
			return Jid.of(stanza.getAttribute("jid"));
		}

		public String getName() {
			return stanza.getAttribute("name");
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}

		public Subscription getSubscription() {
			return Subscription.of(stanza.getAttribute("subscription"));
		}

		public List<Group> getGroups() {
			List<Group> groups = new ArrayList<Group>();
			for (Stanza sub : stanza.getSubStanzas()) {
				if (sub instanceof Group) {
					groups.add((Group) sub);
				}
			}
			return groups;
		}

		public static void register() {
			XmppStanza.register(TYPE, Item.class);
			Group.register();
		}

		public enum Ask {
			subscribe;

			public static Ask of(String s) {
				if (s == null) {
					return null;
				}
				return valueOf(s);
			}
		}

		public enum Subscription {
			both, from, none, remove, to;

			public static Subscription of(String s) {
				if (s == null) {
					return null;
				}
				return valueOf(s);
			}
		}
	}

	public static class Query extends BaseStanza {
		public static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_ROSTER, "query");

		public Query() {
			this(new XmppStanza(TYPE));
		}

		public Query(XmppStanza stanza) {
			super(stanza);
		}

		public static void register() {
			XmppStanza.register(TYPE, Query.class);
			Item.register();
		}

		public List<Item> getItems() {
			List<Item> items = new ArrayList<Item>();
			for (Stanza sub : stanza.getSubStanzas()) {
				if (sub instanceof Item) {
					items.add((Item) sub);
				}
			}
			return items;
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}

		public String getVer() {
			return stanza.getAttribute("ver");
		}
	}
}
