/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package com.olabini.jescov;

import com.olabini.jescov.generators.Generator;

public class Configuration {
	public static interface Ignore {
		boolean allow(String filename);
	}

	public static class Allow implements Ignore {
		@Override
		public boolean allow(final String filename) {
			return true;
		}
	}

	public static class File implements Ignore {
		private final String filename;

		public File(final String filename) {
			this.filename = filename;
		}

		@Override
		public boolean allow(final String filename) {
			return !this.filename.equals(filename);
		}
	}

	public static class Chained implements Ignore {
		private final Ignore car;
		private final Ignore cdr;

		public Chained(final Ignore car, final Ignore cdr) {
			this.car = car;
			this.cdr = cdr;
		}

		@Override
		public boolean allow(final String filename) {
			if (car.allow(filename)) {
				return cdr.allow(filename);
			} else {
				return false;
			}
		}
	}

	private String jsonOutputFile = "jescov.json.ser";
	private String xmlOutputFile = "coverage.xml";
	private String htmlOutputDir = "coverage-report";
	private boolean jsonOutputMerge = false;
	private Ignore ignore = new Allow();
	private boolean enabled = true;
	private String sourceDirectory = ".";

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(final String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getXmlOutputFile() {
		return xmlOutputFile;
	}

	public void setJsonOutputFile(final String file) {
		this.jsonOutputFile = file;
	}

	public void setXmlOutputFile(final String file) {
		this.xmlOutputFile = file;
	}

	public void setHtmlOutputDir(final String dir) {
		this.htmlOutputDir = dir;
	}

	public String getHtmlOutputDir() {
		return htmlOutputDir;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void ignore(final String filename) {
		ignore(new File(filename));
	}

	public void ignore(final Ignore ignore) {
		this.ignore = new Chained(ignore, this.ignore);
	}

	public String getJsonOutputFile() {
		return this.jsonOutputFile;
	}

	public boolean isJsonOutputMerge() {
		return this.jsonOutputMerge;
	}

	public void setJsonOutputMerge(final boolean merge) {
		this.jsonOutputMerge = merge;
	}

	public boolean allow(final String filename) {
		return ignore.allow(filename);
	}

	private Generator generator;

	public void setGenerator(final Generator generator) {
		this.generator = generator;
	}

	public Generator getGenerator() {
		return this.generator;
	}
}
