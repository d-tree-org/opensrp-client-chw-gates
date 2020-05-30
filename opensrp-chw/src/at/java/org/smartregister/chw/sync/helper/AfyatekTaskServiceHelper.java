package org.smartregister.chw.sync.helper;

import org.smartregister.CoreLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.TaskRepository;
import org.smartregister.sync.helper.TaskServiceHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AfyatekTaskServiceHelper extends TaskServiceHelper {
    protected static AfyatekTaskServiceHelper instance;

    public AfyatekTaskServiceHelper(TaskRepository taskRepository) {
        super(taskRepository);
    }

    public static AfyatekTaskServiceHelper getInstance() {
        if (instance == null) {
            instance = new AfyatekTaskServiceHelper(CoreLibrary.getInstance().context().getTaskRepository());
        }
        return instance;
    }

    @Override
    protected List<String> getLocationIds() {
        List<String> res = new ArrayList<>();
        AllSharedPreferences allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
        String locationId =  allSharedPreferences.fetchUserLocalityId(allSharedPreferences.fetchRegisteredANM());
        res.add(locationId);
        return res;
    }

    @Override
    protected Set<String> getPlanDefinitionIds() {
        Set<String> res = new HashSet<>();
        res.add("5270285b-5a3b-4647-b772-c0b3c52e2b71");
        return res;
    }
}
