import java.net.URL;
import java.util.ArrayList;

import modelFSM.AnalyzeData;
import modelFSM.DerivCalc;
import modelFSM.DiscretizeData;
import modelFSM.data.ControlType;


public class Main2 {

    public static void main(String[] args) {
        String groupSuffix = "_group";
        String signalNameOut1 = "C";
        String signalNameIn1 = "A";
        String signalNameIn2 = "B";
        String fileName = "ABC_a_v1.csv";
        URL url = Main2.class.getClassLoader().getResource(fileName);
        
        
        DerivCalc derivCalc = new DerivCalc();
        derivCalc.ReadFile(url.getPath());
        derivCalc.CalcDerivSimple();
        derivCalc.GroupDataSimple();
        
        AnalyzeData analyzeData = new AnalyzeData();
        
        ArrayList<Double> dataArrayGroupDouble = derivCalc.GetData(signalNameOut1 + groupSuffix);
        ArrayList<Integer> dataArrayGroup = new ArrayList<Integer>();
        
        for (Double value: dataArrayGroupDouble) {
            dataArrayGroup.add(value.intValue());
        }
        
        analyzeData.addSignal(ControlType.OUTPUT, signalNameOut1, derivCalc.GetTime(), derivCalc.GetData(signalNameOut1), dataArrayGroup);
        
        dataArrayGroupDouble = derivCalc.GetData(signalNameIn1 + groupSuffix);
        dataArrayGroup = new ArrayList<Integer>();
        
        for (Double value: dataArrayGroupDouble) {
            dataArrayGroup.add(value.intValue());
        }
        
        analyzeData.addSignal(ControlType.INPUT, signalNameIn1, derivCalc.GetTime(), derivCalc.GetData(signalNameIn1), dataArrayGroup);
        
        dataArrayGroupDouble = derivCalc.GetData(signalNameIn2 + groupSuffix);
        dataArrayGroup = new ArrayList<Integer>();
        
        for (Double value: dataArrayGroupDouble) {
            dataArrayGroup.add(value.intValue());
        }
        
//        analyzeData.addSignal(ControlType.INPUT, signalNameIn2, derivCalc.GetTime(), derivCalc.GetData(signalNameIn2), dataArrayGroup);
        
        analyzeData.addSignal(ControlType.INPUT, signalNameIn2, derivCalc.GetTime(), derivCalc.GetData(signalNameIn2));
        
        analyzeData.initDataRules(signalNameOut1);
        analyzeData.analyzeDataRules(signalNameOut1);
        
//        System.out.println("DetectRuleCSC: " + analyzeData.detectRuleCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//
//        System.out.println("DetectRuleCSC: " + analyzeData.detectRuleCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//        
//        System.out.println("DetectRuleCSC: " + analyzeData.detectRuleCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//
//        System.out.println("DetectRuleCSC: " + analyzeData.detectRuleCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//        
//        System.out.println("DetectFullCSC: " + analyzeData.detectFullCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//        
//        System.out.println("DetectFullCSC: " + analyzeData.detectFullCSC(signalNameOut1));
//        System.out.println("ResolveCSC: " + analyzeData.resolveCSC(signalNameOut1));
//        
//        System.out.println("DetectFullCSC: " + analyzeData.detectFullCSC(signalNameOut1));
        // TODO: final method to extract model rules
//        analyzeData.analyzeDataRules(signalNameOut1);
//        
//        GraphDraw graphDraw = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalNameIn1), 
//                derivCalc.GetData(signalNameIn1 + groupSuffix));
//        GraphDraw graphDraw2 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalNameIn2), 
//                derivCalc.GetData(signalNameIn2 + groupSuffix));
//        GraphDraw graphDraw3 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalNameOut1), 
//                derivCalc.GetData(signalNameOut1 + groupSuffix));
//        
//        JFrame f = new JFrame(fileName);
//        JScrollPane content = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        content.setBorder(null);
//
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        content.setViewportView(panel);
//        panel.add(graphDraw);
//        panel.add(graphDraw2);
//        panel.add(graphDraw3);
//        
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.add(content, BorderLayout.CENTER);
//        f.setSize(1920,1080);
//        f.setLocation(200,200);
//        f.setVisible(true);
    }
    
}
