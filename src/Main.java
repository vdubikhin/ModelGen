import javax.swing.*;

import modelFSM.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

class Main {
    public static void DumpData(String name, ArrayList<Double> timeArray, ArrayList<Double> dataArray, ArrayList<Double> dataArrayGroup) {
        FileWriter file;
        try {
            file = new FileWriter(name + ".txt");
            file.write("Time, Data, Group\n");
            
            for(int i=0; i < timeArray.size(); i++)
            {
                String dataStr = timeArray.get(i).toString()  + ", " + dataArray.get(i).toString() + ", " + dataArrayGroup.get(i).toString() + "\n";
                file.write(dataStr);
            }
            
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public static void DumpDataEvents(String name, ArrayList<DerivCalc.Event> eventArray) {
        FileWriter file;
        try {
            file = new FileWriter(name + "_events" + ".txt");
            file.write("Event, Min, Max\n");
            
            for(int i=0; i < eventArray.size(); i++)
            {
                String dataStr = eventArray.get(i).id.toString()  + ", " + eventArray.get(i).start.toString() + ", " + eventArray.get(i).end.toString() + "\n";
                file.write(dataStr);
            }
            
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        String signalName1 = "p_$flow";
        String signalName2 = "power_in";
        String signalName3 = "gp_ack_bus[0]";
        String groupSuffix = "_group";
        String SIMTRACE_GROUPED_SUF = "_simdata";
        
        URL url = Main.class.getClassLoader().getResource("buck_simple.csv");
        
        DerivCalc derivCalc = new DerivCalc();
        derivCalc.ReadFile(url.getPath());
        
        String[] signalNames = {signalName1};//derivCalc.GetSignalNames();
        
        derivCalc.CalcDerivSimple();
        //derivCalc.GroupDataHCA(signalName1);
        derivCalc.GroupDataSimple();
//        derivCalc.FilterGroups();
//        derivCalc.PrintEventsTD(signalName1);
        derivCalc.PrintEvents(signalName1);
        //derivCalc.PrintAllEvents(true, false);
        
        GroupData dataGroup = new GroupData();
        
        for (int i = 0; i < signalNames.length; i++) {
            System.out.println(signalNames[i]);

            
            DumpData(signalNames[i], derivCalc.GetTime(), derivCalc.GetData(signalNames[i]), 
                    derivCalc.GetData(signalNames[i] + groupSuffix));
            DumpDataEvents(signalNames[i], derivCalc.GetEventData(signalNames[i]));

            dataGroup.initSignal(signalNames[i]);
            
            GraphDraw graphDraw = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalNames[i]), 
                    derivCalc.GetData(signalNames[i] + groupSuffix));
            GraphDraw graphDraw2 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalName2), 
                    derivCalc.GetData(signalName2 + groupSuffix));
            GraphDraw graphDraw3 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalName3), 
                    derivCalc.GetData(signalName3 + groupSuffix));
            
            System.out.println(signalNames[i]+SIMTRACE_GROUPED_SUF);
//            JFrame fG = new JFrame(signalNames[i]+SIMTRACE_GROUPED_SUF);
            
            GraphDraw graphDrawGroup = new GraphDraw(dataGroup.getSignalTime(signalNames[i]+SIMTRACE_GROUPED_SUF), 
                    dataGroup.getSignalData(signalNames[i]+SIMTRACE_GROUPED_SUF), 
                    dataGroup.getSignalGroup(signalNames[i]+SIMTRACE_GROUPED_SUF));
            
            GridLayout myLayout = new GridLayout(2,1);
            
            JFrame f = new JFrame(signalNames[i]);
            JScrollPane content = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            content.setBorder(null);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            content.setViewportView(panel);
            panel.add(graphDraw);
            panel.add(graphDraw2);
            panel.add(graphDraw3);
            panel.add(graphDrawGroup);
            
            //f.setLayout(myLayout);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(content, BorderLayout.CENTER);
            f.setSize(1920,1080);
            f.setLocation(200,200);
            f.setVisible(true);
//          f.add(graphDraw);
//          f.add(graphDrawGroup);
            
//            fG.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            fG.add(graphDrawGroup);
//            fG.setSize(400,400);
//            fG.setLocation(200,200);
//            fG.setVisible(true);
        }
        
//        GraphDraw graphDraw1 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalName1), 
//                                                                 derivCalc.GetData(signalName1 + groupSuffix));
//        
//        GraphDraw graphDraw2 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalName2), 
//                                                                  derivCalc.GetData(signalName2 + groupSuffix));
//        
//        GraphDraw graphDraw3 = new GraphDraw(derivCalc.GetTime(), derivCalc.GetData(signalName3), 
//                derivCalc.GetData(signalName3 + groupSuffix));
//        
//        JFrame f = new JFrame(signalName1);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.add(graphDraw1);
//        f.setSize(400,400);
//        f.setLocation(200,200);
//        f.setVisible(true);
//        
//        
//        JFrame f2 = new JFrame(signalName2);
//        f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f2.add(graphDraw2);
//        f2.setSize(400,400);
//        f2.setLocation(200,200);
//        f2.setVisible(true);
//        
//        JFrame f3 = new JFrame(signalName3);
//        f3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f3.add(graphDraw3);
//        f3.setSize(400,400);
//        f3.setLocation(200,200);
//        f3.setVisible(true);
    }
}
