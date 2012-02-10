/**
 * @author David S Anderson
 *
 *
 * Copyright (C) 2012 David S Anderson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dsanderson.xctrailreport;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.dsanderson.xctrailreport.core.CompoundXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 */
public class CompoundXmlPullParser extends CompoundXmlParser {

	public CompoundXmlPullParser() {
		super();
	}

	public CompoundXmlPullParser(String name) {
		super(name);
	}

	public CompoundXmlPullParser(String name, String text) {
		super(text);
	}

	@Override
	public CompoundXmlPullParser copy() {
		CompoundXmlPullParser newParser = new CompoundXmlPullParser(getName());
		newParser.setText(getText());
		for (CompoundXmlParser parser : getParsers()) {
			newParser.addParser(parser);
		}
		return newParser;
	}

	@Override
	public void parse(Reader reader) throws XmlPullParserException, IOException {
		XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
		parserFactory.setNamespaceAware(false);
		XmlPullParser parser = parserFactory.newPullParser();

		parser.setInput(reader);

		parse(parser, "");
	}

	private void parse(XmlPullParser parser, String target)
			throws XmlPullParserException, IOException {

		int eventType = parser.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				setName(null);
			} else if (eventType == XmlPullParser.START_TAG) {
				if (target.length() > 0) {
					if (parser.getName().compareTo(getTopLevelTag(target)) == 0) {
						setName(getTopLevelTag(target));
						enterChildTag(parser, getRemainingTag(target));
					}
				} else {
					CompoundXmlPullParser newParser = new CompoundXmlPullParser(
							parser.getName());
					parser.next();
					newParser.parse(parser, "");
					addParser(newParser);
				}
			} else if (eventType == XmlPullParser.TEXT) {
				if (!parser.isWhitespace()
						&& (getName().compareTo(getTopLevelTag(target)) == 0 || target
								.length() == 0))
					setText(getText() + parser.getText());
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().compareTo(getName()) == 0) {
					break;
				}
			}

			eventType = parser.next();
		}
	}

	public void write(Writer writer) throws Exception {
		XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
		parserFactory.setNamespaceAware(false);
		XmlSerializer serializer = parserFactory.newSerializer();

		serializer.setOutput(writer);
		write(serializer);
	}

	private void write(XmlSerializer serializer) throws Exception {
		
	}

	private void enterChildTag(XmlPullParser parser, String target)
			throws XmlPullParserException, IOException {
		CompoundXmlPullParser newParser = new CompoundXmlPullParser(parser.getName());
		parser.next();
		newParser.parse(parser, target);
		addParser(newParser);
	}
}
