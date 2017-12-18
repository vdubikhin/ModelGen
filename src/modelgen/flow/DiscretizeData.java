package modelgen.flow;

import modelgen.manager.ManagerLowCost;
import modelgen.processor.discretization.DataInput;
import modelgen.processor.discretization.DataOutput;
import modelgen.processor.discretization.DiscretizerFactory;

public class DiscretizeData extends Stage<DataInput, DataOutput> implements IStage<DataInput, DataOutput> {
    public DiscretizeData() {
        dataManager = new ManagerLowCost<DataInput, DataOutput>();
        processorFactory = new DiscretizerFactory();
    }
}
