package modelgen.flow;

import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.manager.ManagerLowCost;
import modelgen.processor.discretization.DiscretizerFactory;

public class DiscretizeData extends Stage<StageDataRaw, StageDataState> implements IStage<StageDataRaw, StageDataState> {
    public DiscretizeData() {
        dataManager = new ManagerLowCost<StageDataRaw, StageDataState>();
        processorFactory = new DiscretizerFactory();

        ERROR_PREFIX = "Stage: DiscretizeData error.";
        DEBUG_PREFIX = "Stage: DiscretizeData debug.";
        PD_PREFIX    = "DiscretizeData_";
    }
}
