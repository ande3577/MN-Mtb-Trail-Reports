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
package org.dsanderson.xctrailreport.core;

/**
 * 
 */
public interface ISourceSpecificFactory {
	
	///set whether this source is enabled by the user
	void setEnabled(boolean enabled);
	
	///get whether this source is enabled by the user
	boolean getEnabled();
	
	///get the name of this source
	String getSourceName();
	
	///get the default url for composing a report
	String getDefaultComposeUrl();
	
	///get the default url for requesting a new report
	String getDefaultRequestUrl();
	
	///get this sources trail retriever
	IReportRetriever getReportRetriever();
	
	///get the xml key for parsing source specific info
	String getSourceSpecificXmlKey();
	
	///get the parser for saved trail info
	ISourceSpecificInfoParser getSourceSpecificParser();
		
}
