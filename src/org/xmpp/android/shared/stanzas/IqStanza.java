package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.util.RandomTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IqStanza extends BaseStanza {
	public static final XmppStanza.StanzaType TYPE = new XmppStanza.StanzaType(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "iq");
	private static final int ID_LENGTH = 32;

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
		super(new XmppStanza(TYPE, buildAttributes(from, type), asList(subStanza)));
	}

	private static <T> List<T> asList(T t) {
		List<T> tList = new ArrayList<T>();
		tList.add(t);
		return tList;
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

	public String getId() {
		return stanza.getAttribute("id");
	}

	public Stanza getSubStanza() {
		if (stanza.getSubStanzas().size() == 1) {
			return stanza.getSubStanzas().get(0);
		}
		return null;
	}

	public Jid getTo() {
		return Jid.of(stanza.getAttribute("to"));
	}

	public String getType() {
		return stanza.getAttribute("type");
	}
}
