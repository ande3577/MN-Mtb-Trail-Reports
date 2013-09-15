package org.dsanderson.morctrailreport;

import org.dsanderson.morctrailreport.R;
import org.dsanderson.morctrailreport.AboutActivity;
import org.dsanderson.morctrailreport.parser.MorcFactory;

import org.dsanderson.xctrailreport.application.ReportListCreator;
import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
import org.dsanderson.xctrailreport.core.TrailInfo;
import org.dsanderson.xctrailreport.core.android.LoadReportsTask;
import org.dsanderson.xctrailreport.core.android.TrailReportList;
import org.dsanderson.xctrailreport.core.android.TrailReportPrinter;
import org.dsanderson.android.util.AndroidIntent;
import org.dsanderson.android.util.AndroidProgressBar;
import org.dsanderson.android.util.Dialog;
import org.dsanderson.android.util.Maps;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class morcTrailReportActivity extends SherlockListActivity {

	private TrailReportList trailReports;
	private TrailReportFactory factory = new TrailReportFactory(this);
	ReportListCreator listCreator = new ReportListCreator(factory);
	private String appName;
	private TrailReportPrinter printer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		appName = getString(R.string.app_name);

		trailReports = (TrailReportList) factory.getTrailReportList();
		printer = new TrailReportPrinter(this, factory,
				ReportDecoratorFactory.getInstance(), trailReports, appName,
				ListEntryFactory.getInstance());
		factory.getUserSettingsSource().loadUserSettings();

		refresh(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSherlock().getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferencesMenuItem: {
			openPreferencesMenu();
		}
			break;
		case R.id.refresh:
			refresh(true);
			break;
		case R.id.aboutMenuItem: {
			openAbout();
		}
			break;
		case R.id.sortBy:
			MenuItem distance = item.getSubMenu().getItem(0).setChecked(false);
			MenuItem date = item.getSubMenu().getItem(1).setChecked(false);
			MenuItem duration = item.getSubMenu().getItem(2).setChecked(false);
			MenuItem condition = item.getSubMenu().getItem(3).setChecked(false);

			switch (factory.userSettings.getSortMethod()) {
			case SORT_BY_DISTANCE:
				distance.setChecked(true);
				break;
			case SORT_BY_DATE:
				date.setChecked(true);
				break;
			case SORT_BY_DURATION:
				duration.setChecked(true);
				break;
			case SORT_BY_CONDITION:
				condition.setChecked(true);
			default:
				break;
			}
			break;
		case R.id.sortByDuration:
		case R.id.sortByDate:
		case R.id.sortByDistance:
		case R.id.sortByCondition:

			String sortMethodString = "";

			switch (item.getItemId()) {
			case R.id.sortByDuration:
				sortMethodString = "sortByDuration";
				break;
			case R.id.sortByDate:
				sortMethodString = "sortByDate";
				break;
			case R.id.sortByDistance:
				sortMethodString = "sortByDistance";
				break;
			case R.id.sortByCondition:
				sortMethodString = "sortByCondition";
				break;
			}

			Editor edit = PreferenceManager.getDefaultSharedPreferences(
					getApplication()).edit();
			edit.putString("sortMethod", sortMethodString);
			edit.commit();
			break;
		default:
			Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	// / Launch Preference activity
	private void openPreferencesMenu() {
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	// / Launch about menu activity
	private void openAbout() {
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}

	// / Launch all reports activity
	private void openAllReports(TrailInfo info) {
		MorcFactory.getInstance().setAllReportsInfo(info);
		MorcFactory.getInstance().setAllReportsDate(trailReports.getTimestamp());
		Intent i = new Intent(this, AllReportActivity.class);
		startActivity(i);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && factory.getUserSettings().getRedrawNeeded()) {
			try {
				factory.getUserSettings().setRedrawNeeded(false);
				printer.printTrailReports();
			} catch (Exception e) {
				e.printStackTrace();
				Dialog dialog = new Dialog(this, e);
				dialog.show();
			}
		}
	}

	// / adding this to prevent rescrolling on orientation changed
	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	protected void onDestroy() {
		trailReports.close();
		super.onDestroy();
	}

	private void refresh(boolean forced) {
		factory.getUserSettings().setForcedRefresh(forced);

		factory.getLocationSource().updateLocation();

		new LoadReportsTask(this, factory, listCreator, trailReports,
				factory.getTrailInfoList(), printer, new AndroidProgressBar(
						this)).execute();
	}

	public void onSortButtonClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Sort Order");

		String initialSortMethod = "";
		switch (factory.userSettings.getSortMethod()) {
		case SORT_BY_DISTANCE:
			initialSortMethod = "sortByDistance";
			break;
		case SORT_BY_DATE:
			initialSortMethod = "sortByDate";
			break;
		case SORT_BY_DURATION:
			initialSortMethod = "sortByDuration";
			break;
		case SORT_BY_CONDITION:
			initialSortMethod = "sortByCondition";
			break;
		}

		String methods[] = getResources().getStringArray(R.array.sortMethods);
		int methodIndex = -1;
		for (int i = 0; i < methods.length; i++) {
			if (initialSortMethod.equals(methods[i])) {
				methodIndex = i;
			}
		}

		builder.setSingleChoiceItems(R.array.sortMethodNames, methodIndex,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Editor edit = PreferenceManager
								.getDefaultSharedPreferences(getApplication())
								.edit();
						String methods[] = getResources().getStringArray(
								R.array.sortMethods);
						String sortMethodString = methods[which];
						edit.putString("sortMethod", sortMethodString);
						edit.commit();
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private TrailInfo infoFromButton(View view) {
		ViewGroup linearLayout = (ViewGroup) view.getParent();
		ViewGroup tableRow = (ViewGroup) linearLayout.getParent();
		ViewGroup table = (ViewGroup) tableRow.getParent();
		ViewGroup parentView = (ViewGroup) table.getParent();
		
		String trailName = ((TextView) parentView
				.findViewById(R.id.trailNameView)).getText().toString();
		return trailReports.find(trailName).getTrailInfo();
	}

	public void onAllReportsButtonClick(View view) {
		TrailInfo info = infoFromButton(view);
		if (info == null)
			return;
		openAllReports(info);
	}

	public void onMapButtonClick(View view) {
		TrailInfo info = infoFromButton(view);
		if (info == null)
			return;
		Maps.launchMap(info.getLocation(), info.getName(),
				info.getSpecificLocation(), this);
	}

	public void onComposeButtonClick(View view) {
		TrailInfo info = infoFromButton(view);
		if (info == null)
			return;
		ISourceSpecificTrailInfo sourceSpecific = info
				.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
		if (sourceSpecific != null) {
			String composeUrl = sourceSpecific.getComposeUrl();
			if (composeUrl != null && composeUrl.length() > 0)
				AndroidIntent.launchIntent(composeUrl, this);
		}
	}

	public void onInfoButtonClick(View view) {
		TrailInfo info = infoFromButton(view);
		if (info == null)
			return;
		ISourceSpecificTrailInfo sourceSpecific = info
				.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
		if (sourceSpecific != null) {
			String trailInfoUrl = sourceSpecific.getTrailInfoUrl();
			if (trailInfoUrl != null && trailInfoUrl.length() > 0)
				AndroidIntent.launchIntent(trailInfoUrl, this);
		}
	}
	
	public void onSettingsButtonClick(View v) {
		openPreferencesMenu();
	}

	public void onRefreshButtonClick(View v) {
		refresh(true);
	}

	public void onHelpButtonClick(View v) {
		openAbout();
	}

}
