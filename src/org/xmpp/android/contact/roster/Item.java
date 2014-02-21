package org.xmpp.android.contact.roster;

import org.xmpp.android.shared.stanzas.BaseStanza;
import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.ArrayList;
import java.util.List;

public class Item extends BaseStanza {
	public static final StanzaType TYPE = new StanzaType("item", "jabber:iq:roster");

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

	public String getAsk() {
		return stanza.getAttribute("ask");
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Item {");
		if (getJid() != null) {
			sb.append(" jid=").append(getJid().toString());
		}
		if (getAsk() != null) {
			sb.append(" ask=").append(getAsk());
		}
		if (getName() != null) {
			sb.append(" name=\"").append(getName()).append("\"");
		}
		if (getSubscription() != null) {
			sb.append(" subscription=").append(getSubscription().name());
		}
		sb.append(" groups=[");
		for (Group group : getGroups()) {
			sb.append(" \"").append(group).append("\"");
		}
		return sb.append(" ] }").toString();
	}

	public enum Subscription {
		none, to, from, both;

		public static Subscription of(String s) {
			if (s == null) {
				return null;
			}
			return valueOf(s);
		}
	}
}
