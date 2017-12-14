package modelgen.flow;

import modelgen.manager.ManagerLowCost;
import modelgen.processor.discretization.DataInput;
import modelgen.processor.discretization.DataOutput;
import modelgen.processor.discretization.DiscretizerFactory;

public class DescretizeData extends Stage<DataInput, DataOutput> {
    DescretizeData() {
        dataManager = new ManagerLowCost<DataInput, DataOutput>();
        processorFactory = new DiscretizerFactory();
    }
}
