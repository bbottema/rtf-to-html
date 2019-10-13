package org.bbottema.rtftohtml.impl;

class RTF2HTMLException extends RuntimeException {
	public RTF2HTMLException(@SuppressWarnings("SameParameterValue") final String msg, final Exception cause) {
		super(msg, cause);
	}
}