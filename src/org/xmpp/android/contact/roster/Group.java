package org.xmpp.android.contact.roster;

import org.xmpp.android.shared.stanzas.BaseStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Group extends BaseStanza {
	public static final StanzaType TYPE = new StanzaType("group", "jabber:iq:roster");

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
	public String toString() {
		return "Group { "+getName()+" }";
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
