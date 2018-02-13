package modelgen.flow;

import modelgen.data.stage.StageDataState;
import modelgen.manager.ManagerLowCost;
import modelgen.processor.filtering.FilterFactory;

public class FilterData extends Stage<StageDataState, StageDataState> implements IStage<StageDataState, StageDataState> {
    public FilterData() {
        dataManager = new ManagerLowCost<StageDataState, StageDataState>();
        processorFactory = new FilterFactory();

        ERROR_PREFIX = "Stage: FilterData error.";
        DEBUG_PREFIX = "Stage: FilterData debug.";
        PD_PREFIX    = "FilterData_";
    }
}
