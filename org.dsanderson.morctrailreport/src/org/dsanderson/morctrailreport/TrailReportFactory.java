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
package org.dsanderson.morctrailreport;

import java.util.ArrayList;
import java.util.List;

import org.dsanderson.android.util.CompoundLocationSource;
import org.dsanderson.android.util.CompoundXmlPullParserFactory;
import org.dsanderson.android.util.Dialog;
import org.dsanderson.android.util.DistanceSource;
import org.dsanderson.android.util.GenericDatabase;
import org.dsanderson.android.util.IDatabaseObjectFactory;
import org.dsanderson.android.util.LocationCoder;
import org.dsanderson.android.util.QuickDistanceSource;
import org.dsanderson.android.util.UrlConnection;
import org.dsanderson.util.ICompoundLocationSource;
import org.dsanderson.util.IDialog;
import org.dsanderson.util.IDistanceSource;
import org.dsanderson.util.ILocationCoder;
import org.dsanderson.util.INetConnection;
import org.dsanderson.util.IUserSettingsSource;
import org.dsanderson.xctrailreport.application.DefaultTrailInfoList;
import org.dsanderson.xctrailreport.core.IAbstractFactory;
import org.dsanderson.xctrailreport.core.ISourceSpecificFactory;
import org.dsanderson.xctrailreport.core.ITrailInfoList;
import org.dsanderson.xctrailreport.core.ITrailReportList;
import org.dsanderson.xctrailreport.core.TrailInfoParser;
import org.dsanderson.xctrailreport.core.TrailInfoPool;
import org.dsanderson.xctrailreport.core.TrailReportParser;
import org.dsanderson.xctrailreport.core.UserSettings;
import org.dsanderson.xctrailreport.core.TrailReportPool;
import org.dsanderson.xctrailreport.core.UserSettings.SortMethod;
import org.dsanderson.xctrailreport.core.android.TrailInfoDatabaseFactory;
import org.dsanderson.xctrailreport.core.android.TrailInfoList;
import org.dsanderson.xctrailreport.core.android.TrailReportDatabaseFactory;
import org.dsanderson.xctrailreport.core.android.TrailReportList;

import android.content.Context;

/**
 * 
 */
public class TrailReportFactory implements IAbstractFactory {
	static TrailReportFactory factory = null;
	Context context;
	MorcAndroidFactory morcAndroidFactory = null;
	UrlConnection netConnection = null;
	CompoundLocationSource locationSource = null;
	UserSettings userSettings = null;
	UserSettingsSource settingsSource = null;
	LocationCoder locationCoder = null;
	TrailReportPool trailReportPool = null;
	TrailInfoPool trailInfoPool = null;
	GenericDatabase database = null;
	TrailReportList trailReportList = null;
	TrailInfoList trailInfoList = null;
	TrailInfoDatabaseFactory trailInfoDatabaseFactory = null;
	DefaultTrailInfoList defaultTrailInfoList = null;
	TrailReportReaderFactory trailReportReaderFactory = null;
	TrailReportDatabaseFactory trailReportDatabaseFactory = null;
	MorcSpecificSettings morcSpecificSettings = null;

	public TrailReportFactory(Context context) {
		assert (factory == null);
		factory = this;
		this.context = context;
		morcAndroidFactory = new MorcAndroidFactory(context, this);
		morcAndroidFactory.setEnabled(true);
	}
	
	static public boolean exists() {
		return (factory != null);
	}

	static public TrailReportFactory getInstance() {
		assert (factory != null);
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getTrailInfoParser()
	 */
	public TrailInfoParser newTrailInfoParser() {
		return new TrailInfoParser(CompoundXmlPullParserFactory.getInstance(),
				factory.getTrailInfoPool(), getSourceSpecificFactories());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#newTrailReportParser()
	 */
	public TrailReportParser newTrailReportParser() {
		return new TrailReportParser(
				CompoundXmlPullParserFactory.getInstance(),
				getTrailReportPool());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getNetConnection()
	 */
	public INetConnection getNetConnection() {
		if (netConnection == null) {
			netConnection = new UrlConnection();
		}
		return netConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dsanderson.xctrailreport.core.IAbstractFactory#getLocation()
	 */
	public ICompoundLocationSource getLocationSource() {
		if (locationSource == null) {
			locationSource = new CompoundLocationSource(context, false,
					"Minneapolis, MN");
		}
		return locationSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getUserSettingsSource
	 * ()
	 */
	public IUserSettingsSource getUserSettingsSource() {
		if (settingsSource == null) {
			settingsSource = new UserSettingsSource(context, this,
					getMorcSpecificSettings());
		}
		return settingsSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getDirectionsSource()
	 */
	public IDistanceSource getDistanceSource() {
		switch (getUserSettings().getDistanceMode()) {
		default:
		case FULL:
			return new DistanceSource(getNetConnection());
		case QUICK:
			return new QuickDistanceSource();
		case DISABLED:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dsanderson.xctrailreport.core.IAbstractFactory#getErrorDialog()
	 */
	public IDialog newDialog(Exception e) {
		e.printStackTrace();
		Dialog dialog = new Dialog(context, e);
		return dialog;
	}

	public IDialog newDialog(String string) {
		return new Dialog(context, string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dsanderson.xctrailreport.core.IAbstractFactory#getUserSettings()
	 */
	public UserSettings getUserSettings() {
		if (userSettings == null) {
			userSettings = new UserSettings();
			userSettings
					.setSortMethod(UserSettings.SortMethod.SORT_BY_DISTANCE);
		}

		return userSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getLocationCoder()
	 */
	public ILocationCoder getLocationCoder() {
		return new LocationCoder(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getTrailReportFactory
	 * ()
	 */
	public TrailReportPool getTrailReportPool() {
		if (trailReportPool == null)
			trailReportPool = new TrailReportPool();
		return trailReportPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getTrailInfoPool()
	 */
	public TrailInfoPool getTrailInfoPool() {
		if (trailInfoPool == null)
			trailInfoPool = new TrailInfoPool();
		return trailInfoPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getSourceSpecificFactory
	 * ()
	 */
	public List<ISourceSpecificFactory> getSourceSpecificFactories() {
		List<ISourceSpecificFactory> factories = new ArrayList<ISourceSpecificFactory>();
		factories.add(morcAndroidFactory);
		return factories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getSourceSpecificFactory
	 * (java.lang.String)
	 */
	public ISourceSpecificFactory getSourceSpecificFactory(String sourceName) {
		for (ISourceSpecificFactory factory : getSourceSpecificFactories()) {
			if (factory.getSourceName().compareTo(sourceName) == 0) {
				return factory;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getTrailReportList()
	 */
	public ITrailReportList getTrailReportList() {
		if (trailReportList == null) {
			trailReportList = new TrailReportList(context,
					new TrailReportDatabaseFactory(
							factory.getTrailReportPool(),
							getTrailInfoDatabaseFactory()),
					TrailReportDatabaseFactory.DATABASE_NAME,
					Integer.parseInt(context
							.getString(R.integer.databaseVersion)));
		}
		
		try {
			trailReportList.open();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return (ITrailReportList) trailReportList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#getTrailInfoList()
	 */
	public ITrailInfoList getTrailInfoList() {
		if (trailInfoList == null)
			trailInfoList = new TrailInfoList(
					(TrailReportList) getTrailReportList(),
					getTrailReportPool(), getTrailInfoPool(),
					getDefaultTrailInfoList());
		return trailInfoList;
	}

	public ITrailInfoList getDefaultTrailInfoList() {
		if (defaultTrailInfoList == null) {
			defaultTrailInfoList = new DefaultTrailInfoList(this,
					getReportReaderFactory());
		}
		return defaultTrailInfoList;
	}

	private TrailInfoDatabaseFactory getTrailInfoDatabaseFactory() {
		if (trailInfoDatabaseFactory == null) {
			List<IDatabaseObjectFactory> sourceSpecificFactories = new ArrayList<IDatabaseObjectFactory>();
			sourceSpecificFactories.add(new MorcDatabaseFactory(
					morcAndroidFactory));
			trailInfoDatabaseFactory = new TrailInfoDatabaseFactory(
					factory.getTrailInfoPool(), sourceSpecificFactories);
		}
		return trailInfoDatabaseFactory;
	}

	private TrailReportReaderFactory getReportReaderFactory() {
		if (trailReportReaderFactory == null) {
			trailReportReaderFactory = new TrailReportReaderFactory(context);
		}
		return trailReportReaderFactory;
	}

	public TrailReportDatabaseFactory getTrailReportDatabaseFactory() {
		if (trailReportDatabaseFactory == null) {
			trailReportDatabaseFactory = new TrailReportDatabaseFactory(
					getTrailReportPool(), getTrailInfoDatabaseFactory());
		}
		return trailReportDatabaseFactory;

	}

	public MorcSpecificSettings getMorcSpecificSettings() {
		if (morcSpecificSettings == null)
			morcSpecificSettings = new MorcSpecificSettings();
		return morcSpecificSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#filterReports(org.
	 * dsanderson.xctrailreport.core.ITrailReportList)
	 */
	public void filterReports(ITrailReportList trailReports) {

		TrailReportList reports = (TrailReportList) trailReports;
		// apply default filters
		reports.filter(getUserSettings());

		MorcSpecificSettings morcSettings = getMorcSpecificSettings();
		if (morcSettings.getConditionsFilterEnabled()) {
			String filterString = "";
			if (morcSettings.getDryEnabled())
				filterString += "(" + TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Dry')";
			if (morcSettings.getTackyEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Tacky')");
			if (morcSettings.getDampEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Damp')");
			if (morcSettings.getWetEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Wet')");
			if (morcSettings.getWetDoNotRideEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Wet Do Not Ride')");
			if (morcSettings.getClosedEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY
						+ "=='Closed')");
			if (morcSettings.getOtherEnabled())
				filterString = addOrString(filterString, "("
						+ TrailReportDatabaseFactory.COLUMN_SUMMARY + "=='')");

			if (filterString.length() > 0)
				reports.addFilter("(" + filterString + ")");
		}

	}

	private String addOrString(String string1, String string2) {
		String stringOut = string1;
		if (stringOut.length() > 0)
			stringOut += "||";
		stringOut += string2;
		return stringOut;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IAbstractFactory#sortReports(org.dsanderson
	 * .xctrailreport.core.ITrailReportList)
	 */
	public void sortReports(ITrailReportList trailReports) {
		UserSettings settings = getUserSettings();
		TrailReportList reports = (TrailReportList) trailReports;

		if (settings.getSortMethod() == SortMethod.SORT_BY_CONDITION) {
			reports.clearSortOrder();
			reports.addSortOrder(
					TrailReportDatabaseFactory.COLUMN_SUMMARY_PRIORITY, true);
			reports.addSortOrder(TrailReportDatabaseFactory.COLUMN_DATE, false);
			reports.addSortOrder(TrailInfoDatabaseFactory.COLUMN_DURATION,
					false);
			reports.addSortOrder(TrailInfoDatabaseFactory.COLUMN_DISTANCE,
					false);
			reports.addSortOrder(TrailInfoDatabaseFactory.COLUMN_NAME, true);
			reports.addSortOrder(TrailReportDatabaseFactory.COLUMN_PHOTOSET,
					false);
		} else {
			trailReports.sort(settings);
		}
	}
}
