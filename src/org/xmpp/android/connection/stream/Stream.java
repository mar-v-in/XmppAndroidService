package org.xmpp.android.connection.stream;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.HashMap;
import java.util.Map;

public class Stream extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_STREAMS, "stream");
	private static final String FROM_ATTR = "from";
	private static final String TO_ATTR = "to";
	private static final String VERSION_ATTR = "version";
	private static final String DEFAULT_VERSION = "1.0";
	private static final String DEFAULT_LANG = "en";

	public Stream(String from, String to) {
		this(from, to, DEFAULT_VERSION, DEFAULT_LANG);
	}

	public Stream(String from, String to, String version, String lang) {
		super(TYPE.getNamespace(), TYPE.getElement(), buildAttributes(from, to, version, lang));
	}

	public Stream(XmppStanza clone) {
		super(clone);
	}

	private static Map<String, String> buildAttributes(String from, String to, String version, String lang) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(FROM_ATTR, from);
		attributes.put(TO_ATTR, to);
		attributes.put(VERSION_ATTR, version);
		attributes.put(XML_LANG_ATTR, lang);
		return attributes;
	}

	@Override
	public String buildBeginTag() {
		StringBuilder attr = new StringBuilder();
		for (String key : getAttributes().keySet()) {
			attr.append(String.format(ATTRIBUTE_TAG, key, escapeAttributeValue(getAttributes().get(key))));
		}
		attr.append(String.format(ATTRIBUTE_TAG, XMLNS_ATTR, XmppNamespaces.NAMESPACE_JABBER_CLIENT));
		attr.append(String.format(ATTRIBUTE_TAG, "xmlns:stream", XmppNamespaces.NAMESPACE_JABBER_STREAMS));
		return String.format(START_TAG, "stream:stream", attr.toString());
	}

	@Override
	public String buildEndTag() {
		return String.format(END_TAG, "stream:stream");
	}

	public static void register() {
		register(TYPE, Stream.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
