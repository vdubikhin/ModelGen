package modelgen.flow;

import modelgen.manager.ManagerLowCost;
import modelgen.processor.discretization.DataOutput;
import modelgen.processor.filtering.FilterFactory;

public class FilterData extends Stage<DataOutput, DataOutput> implements IStage<DataOutput, DataOutput> {
    public FilterData() {
        dataManager = new ManagerLowCost<DataOutput, DataOutput>();
        processorFactory = new FilterFactory();

        ERROR_PREFIX = "Stage: FilterData error.";
        DEBUG_PREFIX = "Stage: FilterData debug.";
        PD_PREFIX    = "FilterData_";
    }
}
