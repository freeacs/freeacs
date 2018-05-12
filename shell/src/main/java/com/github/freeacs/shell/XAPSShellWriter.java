package com.github.freeacs.shell;

import java.io.OutputStream;
import java.io.PrintWriter;

public class XAPSShellWriter extends PrintWriter {

	public XAPSShellWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

}
