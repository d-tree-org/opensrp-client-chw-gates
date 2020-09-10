package org.smartregister.chw.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.dao.AdolescentDao;
import org.smartregister.chw.util.AdolescentUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClients;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.DateUtil;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class UpdateChildToAdolescentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private CommonRepository commonRepository;

    public UpdateChildToAdolescentService() {
        super("UpdateChildToAdolescent");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        this.commonRepository = CoreLibrary.getInstance().context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {

            List<CommonPersonObject> clients = this.commonRepository.allcommon();
            for (CommonPersonObject client: clients) {
                if (!AdolescentDao.isAdolescentMember(client.getCaseId()) && !AncDao.isANCMember(client.getCaseId()) && !PNCDao.isPNCMember(client.getCaseId())) {
                    String dob = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, true);
                    float age = AdolescentUtils.getAgeInYearsAndDecimalPlaces(dob);

                    if (age >= 13 && age <= 19) {
                        AdolescentDao.addAdolescentMember(client);
                    }
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }

    }

}
