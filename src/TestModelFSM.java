

import java.net.URL;
import java.util.HashMap;

import modelgen.ModelFSM;
import modelgen.data.ControlType;

public class TestModelFSM {

    public static void main(String[] args) {
        String groupSuffix = "_group";
        HashMap<String, ControlType> signals = new HashMap<String, ControlType>();
        
//        String fileName = "DD_test_v1.csv";
//        signals.put("IdealDampOscNoise", ControlType.OUTPUT);
//        signals.put("IdealNoise", ControlType.OUTPUT);
//        signals.put("IdealTransientNoise", ControlType.OUTPUT);
//        signals.put("Ideal", ControlType.INPUT);
//        signals.put("IdealTransient", ControlType.INPUT);
//        signals.put("LinearNoise", ControlType.INPUT);
//        signals.put("Linear", ControlType.INPUT);

//        String fileName = "ABC_a_v1.csv";
        String fileName = "ABC_v1.csv"; //And gate
//        String fileName = "ABC_v2.csv"; //Internal memory
//        String fileName = "ABC_v3.csv"; //Or gate
//        String fileName = "ABC_v4.csv"; //C-element
//      String fileName = "ABC_v5.csv"; //Toggle
        signals.put("C", ControlType.OUTPUT);
//        signals.put("C", ControlType.INPUT);
        signals.put("A", ControlType.INPUT);
        signals.put("B", ControlType.INPUT);
//        signals.put("B", ControlType.OUTPUT);
        
//        signals.put("p_$flow", ControlType.OUTPUT);
//        signals.put("power_in", ControlType.INPUT);
//        signals.put("gp_ack_bus[0]", ControlType.INPUT);
//        String fileName = "buck_simple.csv";
        
        URL url = TestModelFSM.class.getClassLoader().getResource(fileName);

        ModelFSM modelGenerator = new ModelFSM(url.getPath());
      
        for (String signal: signals.keySet()) {
            modelGenerator.setSignalType(signal, signals.get(signal));
        }

        modelGenerator.processData();
    }
}
