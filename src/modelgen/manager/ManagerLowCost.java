package modelgen.manager;

import java.util.List;

import modelgen.data.stage.IStageData;
import modelgen.processor.IDataProcessor;

public class ManagerLowCost<T extends IStageData, S extends IStageData> extends DataManager<T, S> 
    implements IDataManager<T, S> {
    public ManagerLowCost() {
        ERROR_PREFIX = "DataManager: ManagerLowCost error.";
        DEBUG_PREFIX = "DataManager: ManagerLowCost debug.";
    }

    @Override
    protected void sortDataProcessors(List<IDataProcessor<S>> processorsToSort) throws NullPointerException {
        processorsToSort.sort((proc1, proc2) -> Double.compare(proc1.processCost(), proc2.processCost()));
    }
}
