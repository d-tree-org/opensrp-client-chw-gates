package org.smartregister.chw.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.presenter.MonthlyActivityDashboardPresenter;
import org.smartregister.chw.util.ChartUtil;
import org.smartregister.reporting.contract.ReportContract;
import org.smartregister.reporting.domain.IndicatorQuery;
import org.smartregister.reporting.domain.IndicatorTally;
import org.smartregister.reporting.domain.ReportIndicator;
import org.smartregister.reporting.model.NumericDisplayModel;
import org.smartregister.reporting.view.NumericIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.smartregister.reporting.contract.ReportContract.IndicatorView.CountType.LATEST_COUNT;
import static org.smartregister.reporting.util.ReportingUtil.getIndicatorDisplayModel;

public class MonthlyActivityDashboard extends Fragment implements ReportContract.View, LoaderManager.LoaderCallbacks<List<Map<String, IndicatorTally>>>  {

    private static ReportContract.Presenter presenter;
    private ViewGroup visualizationsViewGroup;
    private List<Map<String, IndicatorTally>> indicatorTallies;

    private static final String HAS_LOADED_SAMPLE_DATA = "has_loaded_sample_data";
    private boolean activityStarted = false;
    private boolean hasLoadedSampleData = true;

    private View spacerView;
    private View titleView;

    MonthlyActivityDashboard(){}

    public static MonthlyActivityDashboard newInstance(){
        MonthlyActivityDashboard fragment = new MonthlyActivityDashboard();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new MonthlyActivityDashboardPresenter(this);

        addIndicators();

        presenter.scheduleRecurringTallyJob();
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void addIndicators(){
        List<ReportIndicator> reportIndicators = new ArrayList<>();
        List<IndicatorQuery> indicatorQueries = new ArrayList<>();

        String currentMonthVisitsQuery = "select count (*) " +
                "from ( " +
                "select distinct(base_entity_id), date(datetime(visit_date/1000, 'unixepoch')) as date_visited " +
                "from visits " +
                "where " +
                "datetime(visit_date/1000, 'unixepoch') > date('now', 'start of month') " +
                "and visit_type in ('ANC Home Visit', 'PNC Home Visit') " +
                "group by base_entity_id, date_visited )";

        String lastMonthVisitsQuery = "select count (*) " +
                "from ( " +
                "select distinct(base_entity_id), date(datetime(visit_date/1000, 'unixepoch')) as date_visited " +
                "from visits " +
                "where " +
                "datetime(visit_date/1000, 'unixepoch') < date('now', 'start of month') " +
                "and datetime(visit_date/1000, 'unixepoch') > date('now', 'start of month', '-1 months') " +
                "and visit_type in ('ANC Home Visit', 'PNC Home Visit') " +
                "group by base_entity_id, date_visited )";

        ReportIndicator currentMonthVisitsIndicator = new ReportIndicator();
        currentMonthVisitsIndicator.setKey("S_IND_004");
        currentMonthVisitsIndicator.setDescription("Visits conducted in the current month");
        reportIndicators.add(currentMonthVisitsIndicator);

        ReportIndicator lastMonthVisitsIndicator = new ReportIndicator();
        lastMonthVisitsIndicator.setKey("S_IND_003");
        lastMonthVisitsIndicator.setDescription("Visits conducted in the last month");
        reportIndicators.add(lastMonthVisitsIndicator);

        IndicatorQuery currentMonthVisitsIndicatorQuery = new IndicatorQuery();
        currentMonthVisitsIndicatorQuery.setIndicatorCode("S_IND_004");
        currentMonthVisitsIndicatorQuery.setDbVersion(0);
        currentMonthVisitsIndicatorQuery.setId(null);
        currentMonthVisitsIndicatorQuery.setQuery(currentMonthVisitsQuery);
        indicatorQueries.add(currentMonthVisitsIndicatorQuery);

        IndicatorQuery lastMonthVisitsIndicatorQuery = new IndicatorQuery();
        lastMonthVisitsIndicatorQuery.setIndicatorCode("S_IND_003");
        lastMonthVisitsIndicatorQuery.setDbVersion(0);
        lastMonthVisitsIndicatorQuery.setId(null);
        lastMonthVisitsIndicatorQuery.setQuery(lastMonthVisitsQuery);
        indicatorQueries.add(lastMonthVisitsIndicatorQuery);

        presenter.addIndicators(reportIndicators);
        presenter.addIndicatorQueries(indicatorQueries);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        spacerView = inflater.inflate(R.layout.report_spacer_view, container, false);
        titleView = inflater.inflate(R.layout.reporting_indicator_title, container, false);
        return inflater.inflate(R.layout.monthly_activities_dashboard_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        visualizationsViewGroup = getView().findViewById(R.id.dashboard_content);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void refreshUI() {
        buildVisualization(visualizationsViewGroup);
    }

    @Override
    public void buildVisualization(ViewGroup mainLayout) {
        mainLayout.removeAllViews();
        createReportViews(mainLayout);
    }

    @Override
    public List<Map<String, IndicatorTally>> getIndicatorTallies() {
        return indicatorTallies;
    }

    @Override
    public void setIndicatorTallies(List<Map<String, IndicatorTally>> indicatorTallies) {
        this.indicatorTallies = indicatorTallies;
    }

    private void createReportViews(ViewGroup mainLayout) {

        mainLayout.addView(getTitleView(getResources().getString(R.string.current_month)));
        NumericDisplayModel currentMonthRegistrations = getIndicatorDisplayModel(LATEST_COUNT, ChartUtil.currentMonthRegistrationsIndicatorKey, R.string.registrations_indicator, indicatorTallies);
        mainLayout.addView(new NumericIndicatorView(getContext(), currentMonthRegistrations).createView());

        NumericDisplayModel currentMonthVisits = getIndicatorDisplayModel(LATEST_COUNT, ChartUtil.currentMonthVisitsIndicatorKey, R.string.visits_indicator, indicatorTallies);
        mainLayout.addView(new NumericIndicatorView(getContext(), currentMonthVisits).createView());

        //Add Space between indicators
        mainLayout.addView(spacerView);

        mainLayout.addView(getTitleView(getResources().getString(R.string.last_month)));
        NumericDisplayModel lastMonthRegistrations = getIndicatorDisplayModel(LATEST_COUNT, ChartUtil.lastMonthRegistrationsIndicatorKey, R.string.registrations_indicator, indicatorTallies);
        mainLayout.addView(new NumericIndicatorView(getContext(), lastMonthRegistrations).createView());

        NumericDisplayModel lastMonthVisits = getIndicatorDisplayModel(LATEST_COUNT, ChartUtil.lastsMonthVisitsIndicatorKey, R.string.visits_indicator, indicatorTallies);
        mainLayout.addView(new NumericIndicatorView(getContext(), lastMonthVisits).createView());

        //PieChartSlice indicator2_1 = getPieChartSlice(LATEST_COUNT, ChartUtil.pieChartYesIndicatorKey, getResources().getString(R.string.target_not_reached), getResources().getColor(android.R.color.holo_red_light), indicatorTallies);
        //PieChartSlice indicator2_2 = getPieChartSlice(LATEST_COUNT, ChartUtil.pieChartNoIndicatorKey, getResources().getString(R.string.target_reached), getResources().getColor(R.color.green_overlay), indicatorTallies);

        //mainLayout.addView(new PieChartIndicatorView(getContext(), getPieChartDisplayModel(addPieChartSlices(indicator2_1, indicator2_2), R.string.monthly_target, R.string.visits_and_registrations)).createView());

    }

    View getTitleView(String titleText){
        View tv = LayoutInflater.from(this.getContext()).inflate(R.layout.reporting_indicator_title,null, false);
        TextView title = tv.findViewById(R.id.indicator_title);
        title.setText(titleText);
        return tv;
    }

    @NonNull
    @Override
    public Loader<List<Map<String, IndicatorTally>>> onCreateLoader(int id, @Nullable Bundle args) {
        hasLoadedSampleData = Boolean.parseBoolean(ChwApplication.getInstance().getContext().allSharedPreferences().getPreference(HAS_LOADED_SAMPLE_DATA));
        if (!activityStarted){
            activityStarted = true;
            return new ReportIndicatorsLoader(getContext(), false);
        }else{
            return new ReportIndicatorsLoader(getContext(), true);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Map<String, IndicatorTally>>> loader, List<Map<String, IndicatorTally>> data) {
        setIndicatorTallies(data);
        refreshUI();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Map<String, IndicatorTally>>> loader) {

    }

    private static class ReportIndicatorsLoader extends AsyncTaskLoader<List<Map<String, IndicatorTally>>> {

        boolean loadedIndicators;

        public ReportIndicatorsLoader(Context context, boolean alreadyLoaded) {
            super(context);
            this.loadedIndicators = alreadyLoaded;
        }

        @Nullable
        @Override
        public List<Map<String, IndicatorTally>> loadInBackground() {
            List<Map<String, IndicatorTally>> empty = new ArrayList<>();
            if (!loadedIndicators) {
                ChwApplication.getInstance().getContext().allSharedPreferences().savePreference(HAS_LOADED_SAMPLE_DATA, "true");
                return presenter.fetchIndicatorsDailytallies();
            }else{
                return empty;
            }
        }
    }

}
