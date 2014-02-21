package org.xmpp.android.shared.stanzas;

import android.os.Parcel;
import android.os.Parcelable;

public interface Stanza {

	XmppStanza asXmppStanza();

	StanzaType getStanzaType();

	final class StanzaType implements Parcelable {
		private final String namespace;
		private final String element;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(this.namespace);
			dest.writeString(this.element);
		}

		private StanzaType(Parcel in) {
			this.namespace = in.readString();
			this.element = in.readString();
		}
		public static final Parcelable.Creator<StanzaType> CREATOR = new Parcelable.Creator<StanzaType>(){

			public StanzaType createFromParcel(Parcel source) {
				return new StanzaType(source);
			}

			public StanzaType[] newArray(int size) {
				return new StanzaType[size];
			}
		};

		public String getNamespace() {
			return this.namespace;
		}

		public String getElement() {
			return this.element;
		}

		@java.lang.Override
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof Stanza.StanzaType)) return false;
			final StanzaType other = (StanzaType)o;
			final java.lang.Object this$namespace = this.getNamespace();
			final java.lang.Object other$namespace = other.getNamespace();
			if (this$namespace == null ? other$namespace != null : !this$namespace.equals(other$namespace)) return false;
			final java.lang.Object this$element = this.getElement();
			final java.lang.Object other$element = other.getElement();
			if (this$element == null ? other$element != null : !this$element.equals(other$element)) return false;
			return true;
		}

		@java.lang.Override
		public int hashCode() {
			final int PRIME = 277;
			int result = 1;
			final java.lang.Object $namespace = this.getNamespace();
			result = result * PRIME + ($namespace == null ? 0 : $namespace.hashCode());
			final java.lang.Object $element = this.getElement();
			result = result * PRIME + ($element == null ? 0 : $element.hashCode());
			return result;
		}

		@java.lang.Override
		public java.lang.String toString() {
			return "Stanza.StanzaType(namespace=" + this.getNamespace() + ", element=" + this.getElement() + ")";
		}

		public StanzaType(final String namespace, final String element) {
			this.namespace = namespace;
			this.element = element;
		}
	}
}
