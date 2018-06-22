package modelgen.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import modelgen.data.model.LPNModel;
import modelgen.data.model.LPNPlace;
import modelgen.data.model.LPNTransition;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.stage.StageDataRule;
import modelgen.data.state.IState;
import modelgen.data.state.IStateTimeless;
import modelgen.data.state.StateSymbolic;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.processor.synthesis.LPNConstructor;
import modelgen.shared.Logger;

public class LPNSynthesisFSM {
    protected String ERROR_PREFIX = "LPNSynthesisFSM error.";
    protected String DEBUG_PREFIX = "LPNSynthesisFSM debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    public LPNSynthesisFSM() {
        // TODO Auto-generated constructor stub
    }
    
    public List<LPNModel> processData(List<StageDataRule> signalsRules) {
        try {
            List<LPNModel> models = new ArrayList<>();

            if (signalsRules == null) {
                Logger.debugPrintln("No rules to construct model", DEBUG_LEVEL);
                return null;
            }

            for (StageDataRule signalRules: signalsRules) {
                String signalName = signalRules.getName();

                LPNModel model = new LPNModel();
                LPNConstructor modelCreator = new LPNConstructor();
                model = modelCreator.processData(signalRules);

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

}
