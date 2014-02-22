package org.xmpp.android;

import org.junit.Assert;
import org.junit.Test;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmppStanzaTest {
	/*
	@Test
	public void testEscapeAttributeValue() throws Exception {
		Assert.assertEquals(XmppStanza.escapeAttributeValue("<anxml elem=\"can do\" much='here' />"),
							"&lt;an:xml elem=&quot;can do&quot; much=&apos;here&apos; /&gt;");
	}

	@Test
	public void testSingleAttributedBeginTag() throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("blub", "7");
		Assert.assertEquals(XmppStanza.buildBeginTag("each", "one", attributes), "<eachone blub=\"7\">");

		attributes = new HashMap<String, String>();
		attributes.put("good", "enc=&od>e\"d");
		DummyXmppStanza dummyXmppElement = new DummyXmppStanza("must", "work", attributes);
		Assert.assertEquals(dummyXmppElement.buildBeginTag(), "<mustwork good=\"enc=&amp;od&gt;e&quot;d\">");
	}

	@Test
	public void testBuildBeginTagSimple() throws Exception {
		Assert.assertEquals(XmppStanza.buildBeginTag("some", "other", new HashMap<String, String>()), "<some:other>");
		DummyXmppStanza dummyXmppElement = new DummyXmppStanza("one", "more");
		Assert.assertEquals(dummyXmppElement.buildBeginTag(), "<onemore>");
	}

	@Test
	public void testBuildEndTag() throws Exception {
		Assert.assertEquals(XmppStanza.buildEndTag("abc", "def"), "</abc:def>");
		DummyXmppStanza dummyXmppElement = new DummyXmppStanza("test", "blub");
		Assert.assertEquals(dummyXmppElement.buildEndTag(), "</test:blub>");
	}*/

	private static class DummyXmppStanza extends XmppStanza {

		protected DummyXmppStanza(String namespace, String element, Map<String, String> attributes,
								  List<Stanza> subElements) {
			super(namespace, element, attributes, subElements);
		}

		protected DummyXmppStanza(String namespace, String element) {
			super(namespace, element);
		}

		public DummyXmppStanza(String namespace, String element, Map<String, String> attributes) {
			super(namespace, element, attributes);
		}
	}
}
