package modelgen.processor.discretization;


import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Logger;

public class DiscretizerFactory extends DataProcessorFactory<StageDataRaw, StageDataState>
                                implements IDataProcessorFactory<StageDataRaw, StageDataState> {
    public DiscretizerFactory() {
        try {
            processorClasses.put(DiscretizeDataByValues.class.newInstance().getName(), DiscretizeDataByValues.class);
            processorClasses.put(DiscretizeDataByDerivativeCluster.class.newInstance().getName(), DiscretizeDataByDerivativeCluster.class);
            processorClasses.put(DiscretizeDataByStabilityCluster.class.newInstance().getName(), DiscretizeDataByStabilityCluster.class);
            processorClasses.put(DiscretizeDataDummy.class.newInstance().getName(), DiscretizeDataDummy.class);
            inputDataClass = StageDataRaw.class;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create processor factory.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }
}
