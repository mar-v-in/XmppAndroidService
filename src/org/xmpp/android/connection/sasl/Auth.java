package org.xmpp.android.connection.sasl;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.TextStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Auth extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "auth");
	public static void register() {
		register(TYPE, Auth.class);
	}

	public Auth(XmppStanza clone) {
		super(clone);
	}

	public Auth(String mechanism, String response) {
		super(XmppNamespaces.NAMESPACE_XMPP_SASL, "auth", buildAttributes(mechanism), buildSubElements(response));
	}

	private static List<Stanza> buildSubElements(String response) {
		List<Stanza> elements = new ArrayList<Stanza>();
		elements.add(new TextStanza(response));
		return elements;
	}

	private static Map<String, String> buildAttributes(String mechanism) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("mechanism", mechanism);
		return attributes;
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
