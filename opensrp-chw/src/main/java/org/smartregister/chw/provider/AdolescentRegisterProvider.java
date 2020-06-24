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
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.referral.fragment.BaseReferralRegisterFragment;
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

public class AdolescentRegisterProvider implements RecyclerViewProvider<AdolescentRegisterProvider.RegisterViewHolder> {

    protected static CommonPersonObjectClient client;
    private final LayoutInflater inflater;
    protected View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public AdolescentRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener,
                                      Set visibleColumns) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;
        this.context = context;
        this.visibleColumns = visibleColumns;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, final AdolescentRegisterProvider.RegisterViewHolder viewHolder) {
        try {
            String fname = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));

            String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
            String age = org.smartregister.family.util.Utils.getTranslatedDate(org.smartregister.family.util.Utils.getDuration(dobString), context);

            String patientName = getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));
            viewHolder.patientName.setText(patientName);
            viewHolder.textViewAge.setText("Age: " + age);

            setAddressAndGender(pc, viewHolder);


            viewHolder.patientColumn.setOnClickListener(onClickListener);
            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(org.smartregister.chw.referral.R.id.VIEW_ID, BaseReferralRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.textReferralStatus.setOnClickListener(onClickListener);
            viewHolder.textReferralStatus.setTag(pc);
            viewHolder.textReferralStatus.setTag(org.smartregister.chw.referral.R.id.VIEW_ID, BaseReferralRegisterFragment.FOLLOW_UP_VISIT);
            viewHolder.registerColumns.setOnClickListener(onClickListener);

            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void setAddressAndGender(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String address = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true);
        String gender_key = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.GENDER, true);
        String gender = "";
        if (gender_key.equalsIgnoreCase("Male")) {
            gender = context.getString(R.string.male);
        } else if (gender_key.equalsIgnoreCase("Female")) {
            gender = context.getString(R.string.female);
        }
        fillValue(viewHolder.textViewAddressAndGender, address + " \u00B7 " + gender);
    }

    protected static void fillValue(TextView v, String value) {
        if (v != null) {
            v.setText(value);
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        AdolescentRegisterProvider.FooterViewHolder footerViewHolder = (AdolescentRegisterProvider.FooterViewHolder) viewHolder;
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
    public RegisterViewHolder createViewHolder(ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.adapter_adolescent_register_list_row, viewGroup, false);
        return new RegisterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup viewGroup) {
        View view = inflater.inflate(org.smartregister.malaria.R.layout.smart_register_pagination, viewGroup, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof AdolescentRegisterProvider.FooterViewHolder;
    }

    public class RegisterViewHolder extends RecyclerView.ViewHolder {
        public TextView patientName;
        public TextView textViewAge;
        public TextView textViewAddressAndGender;
        public TextView textReferralStatus;
        public View patientColumn;
        public View registerColumns;
        public View dueWrapper;

        public RegisterViewHolder(View itemView) {
            super(itemView);

            patientName = itemView.findViewById(R.id.textview_adolescent_name_age);
            textViewAge = itemView.findViewById(R.id.text_adolescent_age);
            textViewAddressAndGender = itemView.findViewById(R.id.text_view_address_gender);
            //textReferralStatus = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_referral_status);
            patientColumn = itemView.findViewById(R.id.adolescent_column);
            registerColumns = itemView.findViewById(R.id.register_columns);
            //dueWrapper = itemView.findViewById(org.smartregister.chw.referral.R.id.due_button_wrapper);
            //textViewService = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_service);
            //textViewFacility = itemView.findViewById(org.smartregister.chw.referral.R.id.text_view_facility);
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
