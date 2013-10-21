package at.punkt.lod2.util;

import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
import eu.lod2.rsine.queryhandling.EvaluationPostponedException;
import org.openrdf.model.Model;

public class TestPersistAndNotifyProvider extends PersistAndNotifyProvider {

    private boolean success;

    @Override
    public void persistAndNotify(Model changeSet, boolean notifyOnlyLocal) {
        try {
            super.persistAndNotify(changeSet, notifyOnlyLocal);
            success = true;
        }
        catch (EvaluationPostponedException e) {
            success = false;
        }
    }

    public boolean getSuccess() {
        return success;
    }
}