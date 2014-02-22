package org.xmpp.android.contact.roster;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.BaseStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.ArrayList;
import java.util.List;

public class Query extends BaseStanza {
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
