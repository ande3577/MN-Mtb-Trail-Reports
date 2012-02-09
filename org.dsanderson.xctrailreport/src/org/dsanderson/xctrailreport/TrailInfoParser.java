package org.dsanderson.xctrailreport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.dsanderson.xctrailreport.core.TrailInfo;
import org.dsanderson.xctrailreport.core.ITrailInfoParser;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class TrailInfoParser implements ITrailInfoParser {
	InputStream inputStream;

	// names of the XML tags
	static final String TRAIL_INFO = "trailInfo";
	static final String TRAIL = "trail";
	static final String NAME = "name";
	static final String CITY = "city";
	static final String STATE = "state";
	static final String LOCATION = "location";

	static final String SKINNYSKI_SEARCH_TERM = "skinnyskiSearchTerm";
	static final String SKINNYSKI_TRAIL_INDEX = "skinnyskiTrailIndex";

	static final String THREE_RIVERS_SEARCH_TERM = "threeRiversSearchTerm";

	List<TrailInfo> trailInfo = new ArrayList<TrailInfo>();

	public TrailInfoParser() {
	}

	protected InputStream getInputStream() {
		return inputStream;
	}

	public void parse() throws Exception {
		CompoundTagParser tagParser = new CompoundTagParser();
		tagParser.setInput(new BufferedReader(
				new InputStreamReader(inputStream)));
		tagParser.parse("trailInfo:trails");

		List<CompoundTagParser> trailInfoParser = tagParser.getParsers();

		for (CompoundTagParser parser : trailInfoParser) {
			TrailInfo info = new TrailInfo();
			info.setName(parser.getText(NAME));
			info.setCity(parser.getText(CITY));
			info.setLocation(parser.getText(LOCATION));
			info.setSkinnyskiSearchTerm(parser.getText(SKINNYSKI_SEARCH_TERM));
			String indexString = parser.getText(SKINNYSKI_TRAIL_INDEX);
			if (indexString.length() > 0)
				info.setskinnyskiTrailIndex(Integer.parseInt(indexString));
			info.setState(parser.getText(STATE));
			info.setThreeRiversSearchTerm(parser
					.getText(THREE_RIVERS_SEARCH_TERM));
			trailInfo.add(info.copy());
		}

	}

	public List<TrailInfo> getTrailInfo() {
		return trailInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.ITrailInfoParser#SetInputStream(java
	 * .io.InputStream)
	 */
	public void SetInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
}