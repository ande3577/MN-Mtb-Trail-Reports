package org.dsanderson.morctrailreport;

import org.dsanderson.morctrailreport.R;

import org.dsanderson.xctrailreport.application.ReportListCreator;
import org.dsanderson.xctrailreport.core.android.LoadReportsTask;
import org.dsanderson.xctrailreport.core.android.TrailReportList;
import org.dsanderson.xctrailreport.core.android.TrailReportPrinter;
import org.dsanderson.android.util.AndroidProgressBar;
import org.dsanderson.android.util.Dialog;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

public class morcTrailReportActivity extends SherlockListActivity {

	private SherlockListActivity context = this;
	private TrailReportList trailReports;
	private TrailReportFactory factory = new TrailReportFactory(context);
	ReportListCreator listCreator = new ReportListCreator(factory);
	private String appName;
	private TrailReportPrinter printer;
	private TrailReportButtonHandler buttonHandler;

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
		buttonHandler = new TrailReportButtonHandler(this, trailReports);

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
			buttonHandler.openPreferencesMenu();
		}
			break;
		case R.id.refresh:
			refresh(true);
			break;
		case R.id.aboutMenuItem: {
			buttonHandler.openAbout();
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

		new LoadReportsTask(this, factory, listCreator, trailReports, factory.getTrailInfoList(), printer, new AndroidProgressBar(this)).execute();
	}

	public void onAllReportsButtonClicked(View view) {
		buttonHandler.onAllReportsButtonClick(view);
	}
	
	public void onMoreButtonClicked(View view) {
		buttonHandler.onMoreButtonClicked(view);
	}

}
