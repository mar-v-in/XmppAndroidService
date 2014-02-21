package org.xmpp.android.connection;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmpp.android.account.AccountHelper;
import org.xmpp.android.connection.resource.Bind;
import org.xmpp.android.connection.session.Session;
import org.xmpp.android.connection.stream.Error;
import org.xmpp.android.connection.stream.Features;
import org.xmpp.android.connection.stream.Stream;
import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.stanzas.IqStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.TextStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class XmppConnection implements Connection {
	private static final String TAG = "XMPP/Connection";
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private XmlPullParser xpp;
	private Stream stream;
	private Thread readingThread;
	private Jid jid;
	private StartTlsNegotiation startTls = new StartTlsNegotiation(this);
	private SaslNegotiation sasl = new SaslNegotiation(this);

	static {
		SaslNegotiation.register();
		Stream.register();
		Features.register();
		Error.register();
		StartTlsNegotiation.register();
		Bind.register();
		Session.register();
	}

	private XmppConnection(Socket socket) throws IOException {
		this.socket = socket;
		updateStreams();
	}

	public static Connection open(Jid jid, final ConnectionListener listener) throws IOException {
		// TODO: DNS for real connection details
		Log.d(TAG, "Opening Connection");
		final XmppConnection xmppConnection = new XmppConnection(new Socket(jid.getServer(), 5222));
		xmppConnection.jid = jid;
		try {
			Log.d(TAG, "Initializing Connection");
			xmppConnection.init();
		} catch (Exception e) {
			try {
				xmppConnection.close();
			} catch (Throwable ignored) {
			}
			throw new IOException(e);
		}

		xmppConnection.readingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Started reading");
				while (!Thread.interrupted()) {
					try {
						Stanza stanza = xmppConnection.readFullStanza();
						if (stanza != null) {
							listener.stanzaRead(stanza);
						}
					} catch (Exception e) {
						xmppConnection.handleException("while reading next xml", e);
					}
				}
			}
		});
		xmppConnection.readingThread.start();
		return xmppConnection;
	}

	public static boolean testJid(Jid jid) {
		Connection connection;
		try {
			connection = open(jid, new ConnectionListener() {
				@Override
				public void stanzaRead(Stanza stanza) {
					//Ignore
				}
			});
			connection.close();
			return true;
		} catch (IOException e) {
			Log.w(TAG, "Test failed", e);
			return false;
		}
	}

	@Override
	public void close() {
		try {
			if (readingThread != null) {
				readingThread.interrupt();
			}
			readingThread = null;
			if (os != null) {
				stream.sendEndTag(this);
				os.close();
			}
			os = null;
			if (is != null) {
				is.close();
			}
			is = null;
		} catch (Throwable e) {
			handleException("while closing connection", e);
		}
	}

	@Override
	public void flush() {
		try {
			os.flush();
		} catch (Throwable e) {
			handleException("while flushing " + os, e);
		}
	}

	@Override
	public Jid getJid() {
		return jid;
	}

	@Override
	public Stream getStream() {
		return stream;
	}

	private void handleException(String info, Throwable throwable) {
		Log.w(TAG, String.format("Exception thrown %s", info), throwable);
	}

	private void handleResourceBind() throws IOException, XmlPullParserException {
		Bind bindFeature = stream.getFeatures().getFeature(Bind.class);
		if (bindFeature != null) {
			IqStanza iq = new IqStanza("set");
			Bind bind = new Bind();
			if (jid.getResource() != null) {
				XmppStanza element = new XmppStanza(null, AccountHelper.KEY_RESOURCE);
				element.addSubElement(new TextStanza(jid.getResource()));
				bind.addSubElement(element);
			}
			iq.asXmppStanza().addSubElement(bind);
			iq.asXmppStanza().pushTag(this);
			Stanza result = readFullStanza();
			if (result instanceof IqStanza) {
				if ("result".equals(((IqStanza) result).getType())) {
					bind = (Bind) result.asXmppStanza().getSubStanzas().get(0);
					this.jid = Jid.of(bind.getJid());
					return;
				}
			}
			throw new RuntimeException("Error while bind: " + result);
		}
	}

	private void handleSession() throws IOException, XmlPullParserException {
		Session sessionFeature = stream.getFeatures().getFeature(Session.class);
		if (sessionFeature != null) {
			IqStanza iq = new IqStanza("set");
			iq.asXmppStanza().addSubElement(new Session());
			iq.asXmppStanza().pushTag(this);
			Stanza result = readFullStanza();
			if (result instanceof IqStanza) {
				if ("result".equals(((IqStanza) result).getType())) {
					return;
				}
			}
			throw new RuntimeException("Error while session: " + result);
		}
	}

	private void init()
			throws IOException, XmlPullParserException {
		initXmppStream();
		if (startTls.isSupported()) {
			startTls.start();
		} else {
			Log.w(TAG, "STARTTLS is not supported. That's evil isn't it?");
		}
		if (sasl.isSupported()) {
			sasl.start();
		} else {
			Log.w(TAG, "SASL is not supported. That's evil isn't it?");
		}
		handleResourceBind();
		handleSession();
	}

	public void initXmppStream() throws IOException, XmlPullParserException {
		new Stream(jid.toString(), jid.getServer()).sendBeginTag(this);
		xpp.next();
		stream = (Stream) readStanzaBegin();
		stream.addSubElement(readFullStanza());
	}

	Stanza readFullStanza() throws IOException, XmlPullParserException {
		xpp.next();
		return readStartedStanza();
	}

	private Stanza readStartedStanza() {
		try {
			Stanza stanza = readStanzaBegin();
			int eventType = xpp.next();
			while ((eventType != XmlPullParser.END_DOCUMENT) && (eventType != XmlPullParser.END_TAG)) {
				if (eventType == XmlPullParser.TEXT) {
					stanza.asXmppStanza().addSubElement(new TextStanza(xpp.getText()));
				} else if (eventType == XmlPullParser.START_TAG) {
					stanza.asXmppStanza().addSubElement(readStartedStanza());
				}
				eventType = xpp.next();
			}
			if (stanza instanceof Error) {
				Log.w(TAG, "Error received: " + ((Error) stanza).getErrorCondition());
			}
			return stanza;
		} catch (Throwable t) {
			handleException("while reading xml", t);
		}
		return null;
	}

	private Stanza readStanzaBegin() {
		try {
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.START_TAG) {
				Log.d(TAG, "Event "+eventType+" when awaiting "+XmlPullParser.START_TAG);
				eventType = xpp.next();
			}
			Map<String, String> attributes = new HashMap<String, String>();
			for (int i = 0; i < xpp.getAttributeCount(); ++i) {
				attributes.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
			}
			Stanza stanza = new XmppStanza(xpp.getNamespace(), xpp.getName(), attributes).encapsulate();
			return stanza;
		} catch (Throwable t) {
			handleException("while reading xml", t);
		}
		return null;
	}

	@Override
	public void send(String string) {
		try {
			//Log.d(TAG, "Send: "+string);
			os.write(string.getBytes(Charset.forName("utf-8")));
		} catch (Throwable e) {
			handleException("while writing to " + os, e);
		}
	}

	private void updateStreams() throws IOException {
		Log.d(TAG, (is == null) ? "Initialize in/out" : "Reset in/out");
		is = socket.getInputStream();
		os = socket.getOutputStream();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}

	Socket getSocket() {
		return socket;
	}

	void resetSocket(Socket socket) throws IOException, XmlPullParserException {
		this.socket = socket;
		updateStreams();
		initXmppStream();
	}
}
