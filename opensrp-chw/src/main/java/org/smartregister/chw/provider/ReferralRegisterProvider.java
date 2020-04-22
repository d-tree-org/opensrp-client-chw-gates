package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.R;
import org.smartregister.chw.referral.fragment.BaseReferralRegisterFragment;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.chw.referral.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.util.Utils.getName;

public class ReferralRegisterProvider implements RecyclerViewProvider<ReferralRegisterProvider.RegisterViewHolder> {
    protected static CommonPersonObjectClient client;
    private final LayoutInflater inflater;
    protected View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public ReferralRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paginationClickListener = paginationClickListener;
        this.onClickListener = onClickListener;
        this.visibleColumns = visibleColumns;
        this.context = context;

    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, ReferralRegisterProvider.RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, final ReferralRegisterProvider.RegisterViewHolder viewHolder) {
        try {
            String fname = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));

            String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
            int age = new Period(new DateTime(dobString), new DateTime()).getYears();

            String patientName = getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));
            viewHolder.patientName.setText(String.format(Locale.getDefault(), "%s, %d", patientName, age));
            viewHolder.textViewGender.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true));
            viewHolder.textViewService.setText(Utils.getValue(pc.getColumnmaps(), "problem", true));
            viewHolder.textViewFacility.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.REFERRAL_HF, true));


            viewHolder.patientColumn.setOnClickListener(onClickListener);
            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(org.smartregister.chw.referral.R.id.VIEW_ID, BaseReferralRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.textReferralStatus.setOnClickListener(onClickListener);
            viewHolder.textReferralStatus.setTag(pc);
            viewHolder.textReferralStatus.setTag(org.smartregister.chw.referral.R.id.VIEW_ID, BaseReferralRegisterFragment.FOLLOW_UP_VISIT);
            viewHolder.registerColumns.setOnClickListener(onClickListener);

            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
            setReferralStatusColor(context, viewHolder.textReferralStatus, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.REFERRAL_STATUS, true));

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        ReferralRegisterProvider.FooterViewHolder footerViewHolder = (ReferralRegisterProvider.FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView.setText(MessageFormat.format(context.getString(org.smartregister.R.string.str_page_info), currentPageCount, totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption filterOption, ServiceModeOption serviceModeOption, FilterOption filterOption1, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
//        implement
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String s, String s1, String s2) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.referral_register_list_row_item, parent, false);
        return new RegisterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof ReferralRegisterProvider.FooterViewHolder;
    }

    private void setReferralStatusColor(Context context, TextView textViewStatus, String status) {
        textViewStatus.setText("FOLLOW UP"); // temporary. to be fixed
        switch (status) {
            case "":
                textViewStatus.setTextColor(context.getResources().getColor(org.smartregister.chw.referral.R.color.alert_in_progress_blue));
                break;
            case Constants.REFERRAL_STATUS.FAILED:
                textViewStatus.setTextColor(context.getResources().getColor(org.smartregister.chw.referral.R.color.alert_urgent_red));
                break;
            case Constants.REFERRAL_STATUS.SUCCESSFUL:
                textViewStatus.setTextColor(context.getResources().getColor(org.smartregister.chw.referral.R.color.alert_complete_green));
                break;
            default:
                break;
        }
    }


    public class RegisterViewHolder extends RecyclerView.ViewHolder {
        public TextView patientName;
        public TextView textViewGender;
        public TextView textReferralStatus;
        public View patientColumn;
        public TextView textViewService;
        public TextView textViewFacility;
        public View registerColumns;
        public View dueWrapper;

        public RegisterViewHolder(View itemView) {
            super(itemView);

            patientName = itemView.findViewById(org.smartregister.chw.referral.R.id.patient_name_age);
            textViewGender = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_gender);
            textReferralStatus = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_referral_status);
            patientColumn = itemView.findViewById(org.smartregister.chw.referral.R.id.patient_column);
            registerColumns = itemView.findViewById(org.smartregister.chw.referral.R.id.register_columns);
            dueWrapper = itemView.findViewById(org.smartregister.chw.referral.R.id.due_button_wrapper);
            textViewService = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_service);
            textViewFacility = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_facility);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView pageInfoView;
        public Button nextPageView;
        public Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
        }
    }

}
