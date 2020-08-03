package org.smartregister.chw.intent;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.smartregister.CoreLibrary;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.CommonRepository;

public class UpdateChildToAdolescent extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private CommonRepository commonRepository;

    public UpdateChildToAdolescent() {
        super("UpdateChildToAdolescent");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        commonRepository = CoreLibrary.getInstance().context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        return super.onStartCommand(intent, flags, startId);
    }
}
