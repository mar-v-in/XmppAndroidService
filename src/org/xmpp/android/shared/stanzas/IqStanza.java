package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.util.RandomTools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IqStanza extends CommonStanza {
	public static final XmppStanza.StanzaType TYPE = new XmppStanza.StanzaType(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "iq");
	private static final int ID_LENGTH = 32;

	public enum Type {
		get, set, result, error;

		public static Type of(String s) {
			if (s == null) {
				return null;
			}
			return valueOf(s);
		}
	}

	public IqStanza(XmppStanza stanza) {
		super(stanza);
		assert TYPE.equals(stanza.getStanzaType());
	}

	public IqStanza(String type) {
		super(new XmppStanza(TYPE, buildAttributes(type)));
	}

	public IqStanza(Jid from, String type) {
		super(new XmppStanza(TYPE, buildAttributes(from, type)));
	}

	public IqStanza(Jid from, String type, Stanza subStanza) {
		super(new XmppStanza(TYPE, buildAttributes(from, type), Arrays.asList(subStanza)));
	}

	private static Map<String, String> buildAttributes(Jid from, String type) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("type", type);
		attributes.put("from", from.toString());
		attributes.put("id", RandomTools.generateAlnumString(ID_LENGTH));
		return attributes;
	}

	private static Map<String, String> buildAttributes(String type) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("type", type);
		attributes.put("id", RandomTools.generateAlnumString(ID_LENGTH));
		return attributes;
	}

	public Stanza getSubStanza() {
		if (getIqType() != Type.error) {
			if (stanza.getSubStanzas().size() == 1) {
				return stanza.getSubStanzas().get(0);
			}
		} else {
			for (Stanza stanza : this.stanza.getSubStanzas()) {
				if (!stanza.getStanzaType().getElement().equals("error")) {
					return stanza;
				}
			}
		}
		return null;
	}

	public Type getIqType() {
		return Type.of(getType());
	}
}
