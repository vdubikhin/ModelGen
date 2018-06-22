

import java.net.URL;
import java.util.HashMap;

import modelgen.ModelFSM;
import modelgen.data.ControlType;

public class TestModelFSM {

    public static void main(String[] args) {
        String groupSuffix = "_group";
        HashMap<String, ControlType> signals = new HashMap<String, ControlType>();

//        String fileName = "DD_test_v1.csv";
//        String fileName = "SCI_test_gen.csv";
//        signals.put("IdealDampOscNoise", ControlType.OUTPUT);
//        signals.put("IdealNoise", ControlType.OUTPUT);
//        signals.put("IdealTransientNoise", ControlType.OUTPUT);
//        signals.put("Ideal", ControlType.INPUT);
//        signals.put("IdealTransient", ControlType.INPUT);
//        signals.put("LinearNoise", ControlType.OUTPUT);
//        signals.put("Linear", ControlType.INPUT);

        String fileName = "Memristor4.csv";
//        String fileName = "VoltageDrop1.csv";
        signals.put("VoltageDrop", ControlType.INPUT);
        signals.put("InputVoltage", ControlType.INPUT);
        signals.put("Resistance", ControlType.OUTPUT);

//        signals.put("VoltageDrop", ControlType.OUTPUT);
//        signals.put("InputVoltage", ControlType.INPUT);
//        signals.put("Resistance", ControlType.INPUT);
        
//        String fileName = "ABC_a_v1.csv";
//        String fileName = "ABC_v1.csv"; //And gate
//        String fileName = "ABC_v2.csv"; //Internal memory
//        String fileName = "ABC_v3.csv"; //Or gate
//        String fileName = "c_element_digital2.csv"; //C-element
//      String fileName = "ABC_v5.csv"; //Toggle
//        signals.put("C", ControlType.OUTPUT);
//        signals.put("C", ControlType.INPUT);
//        signals.put("A", ControlType.INPUT);
//        signals.put("B", ControlType.INPUT);
//        signals.put("A", ControlType.OUTPUT);

//        String fileName = "buck_simple.csv";
//        signals.put("p_$flow", ControlType.OUTPUT);
//        signals.put("power_in", ControlType.INPUT);
//        signals.put("gp_ack_bus[0]", ControlType.INPUT);

//      String fileName = "c_element_analog_reduced2.csv";
//      signals.put("C", ControlType.OUTPUT);
//      signals.put("C", ControlType.INPUT);
//      signals.put("rc1_out", ControlType.OUTPUT);
//      signals.put("rc1_out_input", ControlType.INPUT);
//      signals.put("rc1_out", ControlType.INPUT);
//      signals.put("rc2_out", ControlType.OUTPUT);
//      signals.put("rc2_out_input", ControlType.INPUT);
//      signals.put("rc2_out", ControlType.INPUT);
//      signals.put("A", ControlType.INPUT);
//      signals.put("B", ControlType.INPUT);
//      signals.put("A", ControlType.OUTPUT);
//      signals.put("B", ControlType.OUTPUT);
        
//        String fileName = "sine_counter.csv";
//        signals.put("CounterPos", ControlType.OUTPUT);
//        signals.put("Sine", ControlType.INPUT);
        
        URL url = TestModelFSM.class.getClassLoader().getResource(fileName);

        ModelFSM modelGenerator = new ModelFSM(url.getPath());
      
        for (String signal: signals.keySet()) {
            modelGenerator.setSignalType(signal, signals.get(signal));
        }

        modelGenerator.processData();
    }
}
