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

import org.dsanderson.xctrailreport.core.IUserSettingsSource;
import org.dsanderson.xctrailreport.skinnyski.RegionManager;
import org.dsanderson.xctrailreport.skinnyski.SkinnyskiSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

/**
 * 
 */
public class SkinnyskiSettingsSource implements IUserSettingsSource {
	SharedPreferences preference;
	SkinnyskiSettings settings;

	private static final String regionKeys[] = { "mnMetro", "mnNortheast",
			"mnNorthwest", "mnCentral", "mnSouthern", "wiNorthwest",
			"wiNortheast", "wiSouthwest", "wiSouthwest", "miUP", "ia", "ca",
			"il", "nd", "us" };

	public SkinnyskiSettingsSource(Context context, SkinnyskiSettings settings) {
		this.settings = settings;
		preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference
				.registerOnSharedPreferenceChangeListener(preferenceChangedListener);
	}

	private OnSharedPreferenceChangeListener preferenceChangedListener = new OnSharedPreferenceChangeListener() {

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			int index;
			if ((index = findRegionKey(key)) < regionKeys.length) {
				boolean enabled = sharedPreferences.getBoolean(key,
						key.compareTo(regionKeys[0]) == 0);
				HandleRegionValue(RegionManager.supportedRegions[index],
						enabled);
			}
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IUserSettingsSource#loadUserSettings()
	 */
	public void loadUserSettings() {
		assert (regionKeys.length == RegionManager.supportedRegions.length);
		for (int i = 0; i < regionKeys.length; i++) {
			// only first region is enabled by default
			boolean enabled = preference.getBoolean(regionKeys[i], i == 0);
			HandleRegionValue(RegionManager.supportedRegions[i], enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dsanderson.xctrailreport.core.IUserSettingsSource#saveUserSettings()
	 */
	public void saveUserSettings() {
		// nothing to do, saved by preferences menu
	}

	private void HandleRegionValue(String name, boolean enabled) {
		if (enabled)
			settings.getRegions().add(name);
		else
			settings.getRegions().remove(name);
	}

	private int findRegionKey(String tag) {
		for (int i = 0; i < regionKeys.length; i++) {
			if (tag.compareTo(regionKeys[i]) == 0) {
				return i;
			}
		}
		return regionKeys.length;
	}
}