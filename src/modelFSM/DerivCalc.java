package modelFSM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.FileNotFoundException;
 
public class DerivCalc {
    private String cvsSplitBy;
    private static final int groupNum = 6;
    private enum dataType {DMV, CONTINOUS}
    // TODO: add support for arrays of different data types
    private Map <String, ArrayList<Double>> dataMap;
    private Map <String, ArrayList<Event>> eventMap; 
    private Map <String, dataType> typeMap;
    private String timeKey;
    private String[] dataKey;
    private static final String derivSuffix = "_deriv";
    private static final String derivSuffix2 = "_deriv2";
    private static final String groupSuffix = "_group";
    private static final String groupSuffix2 = "_group2";
    private static final String nextIdentifier = ",";
    private static final Double MAX_DELTA = 0.9;
    private static final int groupFilter = 20;
    
    public DerivCalc() {
        // TODO: init data here
        cvsSplitBy = ",";
        dataMap = new HashMap <String, ArrayList<Double>> ();
        typeMap = new HashMap<> ();
        eventMap = new HashMap<String, ArrayList<Event>> ();
    }
    
    // TODO: add check for initialization
    public ArrayList<Double> GetTime() {
        return  dataMap.get(timeKey);
    }
    
    // TODO: add check for key existance
    public ArrayList<Double> GetData(String key) {
        return  dataMap.get(key);
    }
    
    public ArrayList<Event> GetEventData(String key) {
        return  eventMap.get(key);
    }
    
    public String[] GetSignalNames() {
        return dataKey;
        
    }
    
    public static class Event {
        public Integer id;
        public Double start;
        public Double end;
        
        public Event() {
            this.id = 1;
            this.start = (double) 999999;
            this.end = (double) -999999;
        }
        public Event(Event event) {
            this.id = event.id;
            this.start = event.start;
            this.end = event.end;
        }
        
    }
    
    public void PrintEventsTD(String key) {
        System.out.println("Printing transacation events for: " + key);
        ArrayList<Double> dataArrayGroup = dataMap.get(key+groupSuffix);
        ArrayList<Double> timeArrayGroup = dataMap.get(timeKey);
        ArrayList<Event>  eventList = new ArrayList<>();
        int curGroup = dataArrayGroup.get(0).intValue();
        int curNum = 1;
        Double minTime = timeArrayGroup.get(0);
        Double maxTime = 0.0;
        
        
        for (int i = 0; i < dataArrayGroup.size(); i++) {
            if (curGroup != dataArrayGroup.get(i).intValue()) {
                maxTime = timeArrayGroup.get(i-1);
                // Filtering
                if (curGroup != 0) {
                    //System.out.print(key + "_E" + curGroup + "(" + (maxTime - minTime) + ")" + nextIdentifier);
                    //System.out.println("E" + curGroup + " " + (maxTime - minTime));
                    Event tempEvent = new Event();
                    tempEvent.id = curGroup;
                    tempEvent.start = minTime;
                    tempEvent.end = maxTime;
                    eventList.add(tempEvent);
                }
                minTime = timeArrayGroup.get(i);
                curGroup = dataArrayGroup.get(i).intValue();
                curNum = 1;
            } else {
                curNum++;
            }
        }
        
        System.out.println("Group ");
        boolean firstEvent = false;
        for (int i = 0; i < eventList.size(); i++) {
            Event curEvent = eventList.get(i);
            
            if (curEvent.id == 6) {
                if (firstEvent)
                    System.out.print(" -2");
                firstEvent = true;
                System.out.print("\n"+curEvent.id + " -1");
                continue;
            }
                
            if (firstEvent)
                System.out.print(" " + curEvent.id + " -1");
           
        }
        
        System.out.println();
    }
    
    
    public void PrintEvents(String key) {
        System.out.println("Printing events for: " + key);
        ArrayList<Double> dataArrayGroup = dataMap.get(key+groupSuffix);
        ArrayList<Double> timeArrayGroup = dataMap.get(timeKey);
        int curGroup = dataArrayGroup.get(0).intValue();
        int curNum = 1;
        Double minTime = timeArrayGroup.get(0);
        Double maxTime = 0.0;
        
        
        for (int i = 0; i < dataArrayGroup.size(); i++) {
            if (curGroup != dataArrayGroup.get(i).intValue()) {
                maxTime = timeArrayGroup.get(i-1);
                // Filtering
                if (curGroup != 0)
                    //System.out.print(key + "_E" + curGroup + "(" + (maxTime - minTime) + ")" + nextIdentifier);
                    System.out.println("E" + curGroup + " " + (maxTime - minTime));
                    
                minTime = timeArrayGroup.get(i);
                curGroup = dataArrayGroup.get(i).intValue();
                curNum = 1;
            } else {
                curNum++;
            }
        }
        
        System.out.println();
    }
    
    private void PrintDMV(String key, Integer group) {
        
        String suffix;
        switch (group) {
            case 0: suffix = "-";
                break;
            case 1: suffix = "+";
                break;
            default:  suffix = "_E" + group;
                break;
        }
        System.out.print(key + suffix + nextIdentifier);
        
        
        
    }
    
    public void PrintAllEvents(boolean supressAnalog, boolean printFirst) {
        System.out.println("Printing all events");
        
        int maxSize = dataMap.get(timeKey).size();
        HashMap<String, Integer> curGroup = new HashMap <> ();
        
        for (int k = 0; k < dataKey.length; k++) {
            String key = dataKey[k];
            
            if (supressAnalog && typeMap.get(key) == dataType.CONTINOUS)
                continue;
            
            Integer group = dataMap.get(key+groupSuffix).get(0).intValue();
            curGroup.put(key, group);
            
            if (printFirst) {
                if (typeMap.get(key) == dataType.DMV)
                    PrintDMV(key, group);
                else
                    System.out.print(key + "_E" + group + nextIdentifier);
            }
        }
        
        for (int i = 0; i < maxSize; i++) {
            for (String key: curGroup.keySet()) {
                 
                ArrayList<Double> groupArr = dataMap.get(key+groupSuffix);
                Integer nextGroup = groupArr.get(i).intValue();
                if (nextGroup != curGroup.get(key).intValue()) {
                    
                    if (typeMap.get(key) == dataType.DMV)
                        PrintDMV(key, nextGroup);
                    else
                        System.out.print(key + "_E" + nextGroup + nextIdentifier);
                    
                    curGroup.put(key, nextGroup);
                }
            }
        }
        
        System.out.println();
    }
    
    public void FilterGroups() {
        for (int k = 0; k < dataKey.length; k++) {
            String key = dataKey[k];

            ArrayList<Double> dataArrayGroup = dataMap.get(key+groupSuffix);
            int curGroup = dataArrayGroup.get(0).intValue();
            int curNum = 1;
            int prevGroupPos = 0;
            
            for (int i = 0; i < dataArrayGroup.size(); i++) {
                if (curGroup != dataArrayGroup.get(i).intValue()) {
                    // Filtering
                    if (curNum <= groupFilter)
                        for (int j = prevGroupPos; j < i; j++)
                            dataArrayGroup.set(j, 0.0);
                        
                    curGroup = dataArrayGroup.get(i).intValue();
                    curNum = 1;
                    prevGroupPos = i;
                } else {
                    curNum++;
                }
            }
            
        }
        
    }
    
    public void GroupDataHCA(String key) {
        String key_suffix = key;
        if (typeMap.get(key) == dataType.CONTINOUS) {
            key_suffix += derivSuffix;
        }
        
        System.out.println("Calculating group for: " + key_suffix); 
        ArrayList<Double> inputData = dataMap.get(key_suffix);
        
        ArrayList<Double> dataArrayGroup = new ArrayList<Double> ();
        
        if (typeMap.get(key) == dataType.CONTINOUS) {
            ArrayList<Event> tempEventArr = new ArrayList<DerivCalc.Event>();
            dataArrayGroup = HCA.AggClustering(inputData, dataMap.get(timeKey), MAX_DELTA, tempEventArr);
            eventMap.put(key, tempEventArr);
        }
        
        if (typeMap.get(key) == dataType.DMV) {

            for (int i = 0; i < inputData.size(); i ++)
                dataArrayGroup.add(inputData.get(i));

        }
        
        dataMap.put(key.replace(derivSuffix,"")+groupSuffix, dataArrayGroup);
    }
    
    
    public void GroupDataHCA() {
        for (int k = 0; k < dataKey.length; k++) {
            String key = dataKey[k];
            GroupDataHCA(key);
        }
    }
    
    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
    
    public void GroupDataSimple() {
        for (int k = 0; k < dataKey.length; k++) {
            String key = dataKey[k];
            String key2 = dataKey[k];
            if (typeMap.get(dataKey[k]) == dataType.CONTINOUS) {
                key += derivSuffix;
                key2 += derivSuffix2;
            }

            System.out.println("Calculating group for: " + key); 
            ArrayList<Double> dataArray = dataMap.get(key);
            ArrayList<Double> dataArray2 = dataMap.get(key2);
            Map<Integer, Double> localDataMap = new HashMap<> (); 
//            TreeMap<Double, Integer> dataArraySorted = new TreeMap<> ();
            ArrayList<Double> dataArrayGroup  = new ArrayList<Double> ();
            ArrayList<Double> dataArrayGroup2  = new ArrayList<Double> ();
            
            //calc groups for 2nd deriv
            for (int i = 0; i < dataArray2.size(); i++) {
                if (dataArray2.get(i) > 0)
                    dataArrayGroup2.add(1.0);
                
                if (dataArray2.get(i) == 0)
                    dataArrayGroup2.add(3.0);
                
                if (dataArray2.get(i) < 0)
                    dataArrayGroup2.add(2.0);
            }
            // Copy data array
            for (int i = 0; i < dataArray.size(); i++) {
//                dataArraySorted.put(dataArray.get(i), i);
                dataArrayGroup.add(1.0);
                localDataMap.put(i, dataArray.get(i));
            }
            
            SortedSet<Map.Entry<Integer, Double>> sortedData = entriesSortedByValues(localDataMap);
            
            
//            int numPerGroup = dataArraySorted.size()/groupNum + (groupNum - 1);
//            Double curGroup = 1.0; 
//            int curPos = 0; 
//            for (Double i : dataArraySorted.keySet()) {
//                
//                if (curPos > numPerGroup*curGroup) {
//                    curGroup += 1.0;
//                }
//                    
//                dataArrayGroup.set(dataArraySorted.get(i), curGroup);
//                curPos++;
//            }
            
            Double curGroup = 1.0;
            
            if (typeMap.get(dataKey[k]) == dataType.CONTINOUS) {
                Event tempEvent = new Event();

                int numPerGroup = sortedData.size()/groupNum + (groupNum - 1);
                 
                int curPos = 0;
                Iterator<Map.Entry<Integer, Double>> itr = sortedData.iterator();
                while (itr.hasNext()) {
                    Map.Entry<Integer, Double> entry = itr.next();
                    tempEvent.start = Math.min(tempEvent.start, entry.getValue());
                    tempEvent.end = Math.max(tempEvent.end, entry.getValue());
                    
                    if (curPos > numPerGroup*curGroup) {
                        curGroup += 1.0;
                        ArrayList<Event> tempEventArr = eventMap.get(dataKey[k]);
                        if (tempEventArr == null) {
                            tempEventArr = new ArrayList<DerivCalc.Event>();
                        }
                        tempEventArr.add(new Event(tempEvent));
                        eventMap.put(dataKey[k], tempEventArr);
                        tempEvent.id = tempEvent.id + 1;
                        tempEvent.start = (double) 999999;
                        tempEvent.end = (double) -999999;
                    }
                    
                    dataArrayGroup.set(entry.getKey(), curGroup);
                    curPos++;
                }
                ArrayList<Event> tempEventArr = eventMap.get(dataKey[k]);
                tempEventArr.add(new Event(tempEvent));
                eventMap.put(dataKey[k], tempEventArr);
            }
            
            if (typeMap.get(dataKey[k]) == dataType.DMV) {
                Iterator<Map.Entry<Integer, Double>> itr = sortedData.iterator();
                while (itr.hasNext()) {
                    Map.Entry<Integer, Double> entry = itr.next();
                    
                    dataArrayGroup.set(entry.getKey(), entry.getValue());
                    curGroup = entry.getValue() + 1;
                  }
            }
            
            
            // Min Max Avg Total
            Double[][] statArr = new Double[groupNum][4];
            for (int i = 0; i < groupNum; i++) {
                statArr[i][0] = Double.MAX_VALUE;
                statArr[i][1] = -Double.MAX_VALUE;
                statArr[i][2] = 0.0;
                statArr[i][3] = 0.0;
            }
            
            for (int i = 0; i < dataArrayGroup.size(); i++) {
                int group = dataArrayGroup.get(i).intValue();
                if (typeMap.get(dataKey[k]) == dataType.CONTINOUS)
                    group -= 1;
                
                Double curValue = dataArray.get(i);
                
                if(curValue > statArr[group][1])
                    statArr[group][1] = curValue;
                    
                if(curValue < statArr[group][0])
                    statArr[group][0] = curValue;

                statArr[group][2] += curValue;
                statArr[group][3] += 1.0;
            }
            
            for (int i = 0; i < curGroup; i++) {
                statArr[i][2] =   statArr[i][2]/ statArr[i][3];
                System.out.println("Group: " + (i+1) + " Min: " + statArr[i][0] + " Max: " + 
                                    statArr[i][1] + " Avg: " +statArr[i][2]  + " Total: " + statArr[i][3]);
            }
            
            dataMap.put(key.replace(derivSuffix,"")+groupSuffix, dataArrayGroup);
            dataMap.put(key2.replace(derivSuffix2,"")+groupSuffix2, dataArrayGroup2);
        }
    }
    
    // TODO: check that data array has been initialized first
    public void CalcDerivSimple () {
        ArrayList<Double> timeArray = dataMap.get(timeKey);
        
        for (int k = 0; k < dataKey.length; k++) {
            String key = dataKey[k];
                            
            ArrayList<Double> dataArray = dataMap.get(key);
            ArrayList<Double> derivArray = new  ArrayList<Double> ();
            ArrayList<Double> deriv2Array = new  ArrayList<Double> ();
            Double derivPoint, derivPoint2; 
            System.out.println("Calculating derivative for: " + key );
            for (int i = 0; i < timeArray.size(); i++) {
   
                // TODO: Check delta time is not zero
                // Most left point
                if (i == 0) {
                    derivPoint = (dataArray.get(i+1) - dataArray.get(i))/(timeArray.get(i+1) - timeArray.get(i));
                    derivPoint2 = (dataArray.get(i+2) -2*dataArray.get(i+1) + dataArray.get(i))/Math.pow((timeArray.get(i+1) - timeArray.get(i)), 2);
                    derivArray.add(derivPoint);
                    deriv2Array.add(derivPoint2);
                    continue;
                }
                
                // Most right point
                if (i == dataArray.size() - 1) { 
                    derivPoint = (dataArray.get(i) - dataArray.get(i-1))/(timeArray.get(i) - timeArray.get(i-1));
                    derivPoint2 = (dataArray.get(i-2) -2*dataArray.get(i-1) + dataArray.get(i))/Math.pow((timeArray.get(i) - timeArray.get(i-1)), 2);
                    derivArray.add(derivPoint);
                    deriv2Array.add(derivPoint2);
                    continue;
                }
                
                // Middle points
                derivPoint = (dataArray.get(i+1) - dataArray.get(i-1))/(timeArray.get(i+1) - timeArray.get(i-1))/2;
                derivPoint2 = (dataArray.get(i+1) -2*dataArray.get(i) + dataArray.get(i-1))/Math.pow((timeArray.get(i+1) - timeArray.get(i)), 2);
                derivArray.add(derivPoint);
                deriv2Array.add(derivPoint2);
            }
            
            dataMap.put(key+derivSuffix, derivArray);
            dataMap.put(key+derivSuffix2, deriv2Array);
          }
          
    }
    
    // TODO: check that columns have equal size
    // TODO: check that rows are equal to header row
    public void ReadFile(String fileName) {
        BufferedReader br = null;
        String line = "";
    
        try {
            boolean firstLine = true;
            String[] csvHeader = null;
            br = new BufferedReader(new FileReader(fileName));
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    csvHeader = line.split(cvsSplitBy);
                    for (int i = 0; i < csvHeader.length; i++) {
                        // Simulation time
                        if (i == 0)
                            timeKey = csvHeader[i];
                            
                        ArrayList<Double> tempArrList = new  ArrayList<Double> ();
                        dataMap.put(csvHeader[i], tempArrList);
                        typeMap.put(csvHeader[i], dataType.DMV);
                    }
                    System.out.println(dataMap.keySet());
                } else {
                    String[] rawData = line.split(cvsSplitBy);
                    for (int i = 0; i < rawData.length; i++) {
                        ArrayList<Double> tempArrList = dataMap.get(csvHeader[i]);
                        try {
                           Double tempValue = Double.parseDouble(rawData[i]);
                           tempArrList.add(tempValue);
                           
                           if (tempValue % 1 != 0) {
                               typeMap.put(csvHeader[i], dataType.CONTINOUS);
                           }
                           
                        } catch(NumberFormatException e) {
                            System.out.println("DerivCalc: Read data is not of type double: " + i + " " + rawData[i]);
                            System.out.println(line);
                            e.printStackTrace();
                        }
                        dataMap.put(csvHeader[i], tempArrList);
                    }
                }
                
                firstLine = false;
            }
            
            dataKey = new String[dataMap.size() - 1];
            int keyNum = 0;
            for (String key : dataMap.keySet()) {
                if (!key.equals(timeKey)) {
                    dataKey[keyNum] = key;
                    keyNum++;
                }
            }
            

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        
    }
}
