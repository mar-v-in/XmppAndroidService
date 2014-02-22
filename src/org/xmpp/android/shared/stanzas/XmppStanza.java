package org.xmpp.android.shared.stanzas;

import android.os.Parcel;
import android.os.Parcelable;
import org.xmpp.android.connection.Connection;

import java.lang.reflect.Constructor;
import java.util.*;

public class XmppStanza implements Parcelable, Stanza {
	public static final Creator<XmppStanza> CREATOR = new Creator<XmppStanza>() {
		@Override
		public XmppStanza createFromParcel(Parcel source) {
			StanzaType type = source.readParcelable(StanzaType.class.getClassLoader());
			int attributesSize = source.readInt();
			Map<String, String> attributes = new HashMap<String, String>();
			while (attributesSize-- > 0) {
				attributes.put(source.readString(), source.readString());
			}
			int subElementsSize = source.readInt();
			List<Stanza> subElements = new ArrayList<Stanza>(subElementsSize);
			while (subElementsSize-- > 0) {
				subElements.add(source.<XmppStanza>readParcelable(XmppStanza.class.getClassLoader()).encapsulate());
			}
			return new XmppStanza(type, attributes, subElements);
		}

		@Override
		public XmppStanza[] newArray(int size) {
			return new XmppStanza[size];
		}
	};
	protected final static String XMLNS_ATTR = "xmlns";
	protected static final String XML_LANG_ATTR = "xml:lang";
	protected static final String START_TAG = "<%s%s>";
	protected static final String END_TAG = "</%s>";
	protected static final String NO_CHILD_TAG = "<%s%s />";
	protected static final String ATTRIBUTE_TAG = " %s=\"%s\"";
	private static final String TAG = "XMPP/"+XmppStanza.class.getSimpleName();
	private static Map<StanzaType, Class<? extends Stanza>> known = new HashMap<StanzaType, Class<? extends Stanza>>();
	private final StanzaType type;
	private final Map<String, String> attributes;
	private final List<Stanza> subStanzas;

	static {
		register(TextStanza.TYPE, TextStanza.class);
		register(IqStanza.TYPE, IqStanza.class);
		register(MessageStanza.TYPE, MessageStanza.class);
		register(PresenceStanza.TYPE, PresenceStanza.class);
		register(ErrorStanza.TYPE, ErrorStanza.class);
	}


	protected XmppStanza(String namespace, String element, Map<String, String> attributes, List<Stanza> subStanzas) {
		this(new StanzaType(namespace, element), attributes, subStanzas);
	}

	protected XmppStanza(XmppStanza clone) {
		type = clone.type;
		attributes = new HashMap<String, String>(clone.attributes);
		subStanzas = new ArrayList<Stanza>(clone.subStanzas);
	}

	public XmppStanza(String namespace, String element) {
		this(namespace, element, new HashMap<String, String>());
	}

	public XmppStanza(String namespace, String element, Map<String, String> attributes) {
		this(namespace, element, attributes, new ArrayList<Stanza>());
	}

	public XmppStanza(StanzaType type, Map<String, String> attributes, List<Stanza> subStanzas) {
		this.type = type;
		this.attributes = new HashMap<String, String>(attributes);
		this.subStanzas = new ArrayList<Stanza>(subStanzas);
		assert type.equals(getStanzaType());
	}

	public XmppStanza(StanzaType type) {
		this(type, new HashMap<String, String>());
	}

	public XmppStanza(StanzaType type, Map<String, String> attributes) {
		this(type, attributes, new ArrayList<Stanza>());
	}

	public static String buildBeginTag(String namespace, String element, Map<String, String> attributes) {
		StringBuilder attr = new StringBuilder();
		for (String key : attributes.keySet()) {
			attr.append(String.format(ATTRIBUTE_TAG, key, escapeAttributeValue(attributes.get(key))));
		}
		if (namespace != null) {
			attr.append(String.format(ATTRIBUTE_TAG, XMLNS_ATTR, escapeAttributeValue(namespace)));
		}
		return String.format(START_TAG, element, attr.toString());
	}

	public static String buildEndTag(String namespace, String element) {
		return String.format(END_TAG, element);
	}

	public static String buildNoChildTag(String namespace, String element, Map<String, String> attributes) {
		StringBuilder attr = new StringBuilder();
		for (String key : attributes.keySet()) {
			attr.append(String.format(ATTRIBUTE_TAG, key, escapeAttributeValue(attributes.get(key))));
		}
		if (namespace != null) {
			attr.append(String.format(ATTRIBUTE_TAG, XMLNS_ATTR, escapeAttributeValue(namespace)));
		}
		return String.format(NO_CHILD_TAG, element, attr.toString());
	}

	public static Stanza encapsulate(XmppStanza xmppStanza) {
		if (xmppStanza == null)
			return null;
		synchronized (known) {
			for (Map.Entry<StanzaType, Class<? extends Stanza>> entry : known.entrySet()) {
				if (entry.getKey().equals(xmppStanza.getStanzaType())) {
					return encapsulate(xmppStanza, entry.getValue());
				}
			}
		}
		return xmppStanza;
	}

	private static <T extends Stanza> Stanza encapsulate(XmppStanza xmppStanza, Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getConstructor(XmppStanza.class);
			if (constructor != null) {
				return constructor.newInstance(xmppStanza);
			}
		} catch (Exception e) {
		}
		return xmppStanza;
	}

	public static String escapeAttributeValue(String s) {
		return s.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	public static void register(StanzaType type, Class<? extends Stanza> clazz) {
		synchronized (known) {
			known.put(type, clazz);
			//Log.d(TAG, "registered type " + type + " for " + clazz);
		}
	}

	public void addSubElement(Stanza element) {
		subStanzas.add(element);
	}

	@Override
	public XmppStanza asXmppStanza() {
		return this;
	}

	public String buildBeginTag() {
		return buildBeginTag(type.getNamespace(), type.getElement(), attributes);
	}

	public String buildEndTag() {
		return buildEndTag(type.getNamespace(), type.getElement());
	}

	public String buildNoChildTag() {
		return buildNoChildTag(type.getNamespace(), type.getElement(), attributes);
	}

	@Override
	public int describeContents() {
		return 0; //TODO: Implement
	}

	public Stanza encapsulate() {
		return encapsulate(this);
	}

	protected Stanza findSubStanza(StanzaType type) {
		for (Stanza stanza : subStanzas) {
			if (stanza.getStanzaType().equals(type)) {
				return stanza;
			}
		}
		return null;
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}

	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	public String getElement() {
		return getStanzaType().getElement();
	}

	public String getNamespace() {
		return getStanzaType().getElement();
	}

	public StanzaType getStanzaType() {
		return type;
	}

	public List<Stanza> getSubStanzas() {
		return Collections.unmodifiableList(subStanzas);
	}

	protected String getSubText(StanzaType type) {
		Stanza stanza = findSubStanza(type);
		if (stanza instanceof XmppStanza) {
			return ((XmppStanza) stanza).getSubText();
		}
		return null;
	}

	public String getSubText() {
		if (subStanzas.size() == 1) {
			Stanza stanza = subStanzas.get(0);
			if (stanza instanceof TextStanza) {
				return ((TextStanza) stanza).getText();
			}
		}
		return null;
	}

	public void pushTag(Connection connection) {
		sendTag(connection);
		connection.flush();
	}

	public void sendBeginTag(Connection connection) {
		connection.send(buildBeginTag());
	}

	public void sendEndTag(Connection connection) {
		connection.send(buildEndTag());
	}

	public void sendNoChildTag(Connection connection) {
		connection.send(buildNoChildTag());
	}

	public void sendTag(Connection connection) {
		if (getSubStanzas().size() == 0) {
			sendNoChildTag(connection);
		} else {
			sendBeginTag(connection);
			for (Stanza subElement : getSubStanzas()) {
				subElement.asXmppStanza().sendTag(connection);
			}
			sendEndTag(connection);
		}
	}

	@Override
	public String toString() {
		return "XmppStanza{" +
				"type=" + type +
				", attributes=" + attributes +
				", subStanzas=" + subStanzas +
				'}';
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(getStanzaType(), flags);
		dest.writeInt(attributes.size());
		for (String key : attributes.keySet()) {
			dest.writeString(key);
			dest.writeString(attributes.get(key));
		}
		dest.writeInt(subStanzas.size());
		for (Stanza subElement : subStanzas) {
			dest.writeParcelable(subElement.asXmppStanza(), flags);
		}
	}

	public String getSubText(String namespace, String element) {
		return getSubText(new StanzaType(namespace, element));
	}
}
