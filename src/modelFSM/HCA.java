package modelFSM;

import java.util.*;

import modelFSM.DerivCalc.Event;

public class HCA {
    
    private static class Cluster {
        public Double center;
        public Double min, max;
        public HashSet<Cluster> childArray;
        public HashMap<Integer, Double> dataPoints;
        public int id;
        public static int idGlobal = 0;
        
        // Initial cluster - singleton
        public Cluster(Double center, Integer dataPoint, Double dataTime){
            this.center = center;
            id = idGlobal;
            idGlobal += 1;
            min = center;
            max = center;
            childArray = new HashSet <> ();
            dataPoints = new HashMap <> ();
            dataPoints.put(dataPoint, dataTime);
        }
        
        // Merge two clusters
        public Cluster(Cluster A, Cluster B) {
            center = (A.center + B.center)/2;
            min = Math.min(A.min, B.min);
            max = Math.max(A.max, B.max);
            id = idGlobal;
            idGlobal += 1;
            
            childArray = new HashSet <> ();
            childArray.add(A);
            childArray.add(B);
            
            dataPoints = new HashMap <> ();
            dataPoints.putAll(A.dataPoints);
            dataPoints.putAll(B.dataPoints);
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
    
    public static ArrayList<Double> AggClustering(ArrayList<Double> inputData, ArrayList<Double> timeData, Double maxDelta, ArrayList<Event> eventsInfo) {
        Cluster.idGlobal = 0;
        
        // Sort Data from min to max
        Map<Integer, Double> localDataMap = new HashMap<> (); 

        for (int i = 0; i < inputData.size(); i++) {
            localDataMap.put(i, inputData.get(i));
        }
        
        SortedSet<Map.Entry<Integer, Double>> sortedData = entriesSortedByValues(localDataMap);
        
        
        ArrayList<Cluster> clusterArrayPos = new ArrayList<> ();
        ArrayList<Cluster> clusterArrayNeg = new ArrayList<> ();
        
        // Create singletons
        Iterator<Map.Entry<Integer, Double>> itr = sortedData.iterator();
        while (itr.hasNext()) {
            Map.Entry<Integer, Double> entry = itr.next();
            Double time = null;
            
            // TODO: check that array size is big enough
            if (entry.getKey() == 0)
                time = timeData.get(1)-timeData.get(0);
            
            if (entry.getKey() == inputData.size() - 1)
                time = timeData.get(entry.getKey())-timeData.get(entry.getKey() - 1);
            
            if (time == null)
                time = ( timeData.get(entry.getKey() + 1) - timeData.get(entry.getKey() - 1) ) / 2;
            
            Cluster tempCluster = new Cluster(entry.getValue(), entry.getKey(), time);
            
            if (entry.getValue() >= 0)
                clusterArrayPos.add(tempCluster);
            else
                clusterArrayNeg.add(tempCluster);
        }
        
        // Form dendrogram
        Cluster rootPos =  FormDendrogram(clusterArrayPos);
        Cluster rootNeg =  FormDendrogram(clusterArrayNeg);
        
        // Traverse dendrogram via BFS
        System.out.println("Pos dendrogram");
        HashSet<Cluster> finalLyaer = LayerSearch(rootPos, maxDelta);
        System.out.println("");
        System.out.println("Neg dendrogram");
        finalLyaer.addAll(LayerSearch(rootNeg, maxDelta));
        System.out.println("");
        
        ArrayList<Double> inputDataGroup = new ArrayList<> ();
        
        // Initialize return group array with group 0
        for (int i = 0; i < inputData.size(); i++) {
            inputDataGroup.add(0.0);
        }
        
        Double clNum = 1.0;
        for (Cluster cl : finalLyaer) {
            Event tempEvent = new Event();
            tempEvent.id = clNum.intValue();
            tempEvent.start = cl.min;
            tempEvent.end = cl.max;
            eventsInfo.add(tempEvent);
            for (int i : cl.dataPoints.keySet())
                inputDataGroup.set(i, clNum);
            
            clNum += 1.0;
        }
        
        return inputDataGroup;
    }
    
    private static HashSet<Cluster> LayerSearch(Cluster root, Double maxDelta) {
        HashSet<Cluster> curLayer = root.childArray;
        HashSet<Cluster> finalLayer = new HashSet<>();
        int layerNum = 0;
        
//        System.out.println("Layer: " + layerNum); 
//        System.out.println("Id: " + root.id + " center: " + root.center + " min: " + root.min + " max: " + root.max + " points num: " + root.dataPoints.size());
        layerNum += 1;
        
        
        while(!curLayer.isEmpty()) {
            HashSet<Cluster> tempLayer = new HashSet<>();
            HashSet<Cluster> singleLayer = new HashSet<>();
            boolean layerFine = true;
            
//            System.out.println("Layer: " + layerNum);
            
            for (Cluster cl : curLayer) {
                Double delta = Math.abs((cl.max-cl.min)/cl.center/2);
//                System.out.println("Id:: " + cl.id + " center: " + cl.center + " min: " + cl.min + " max: " + cl.max + " delta: " + delta + " points num: " + cl.dataPoints.size());
                
                if (cl.childArray.isEmpty())
                    singleLayer.add(cl);
                
                if ( delta > maxDelta ) {
                    layerFine = false;
                    tempLayer.addAll(cl.childArray);
                } else {
                    singleLayer.add(cl);
                }
            }
            
            if (layerFine) {
//                System.out.println("Layer is fine");
//                System.out.println("");
                // use tempLayer for further calculations
                Double clusterAvg = 0.0;
                Double clusterAvgS = 0.0;
                Double clusterAvgD = 0.0;
//                int clusterTotal = 0;
                System.out.println("Layer: " + layerNum);
                for (Cluster cl : curLayer) {
                    Double delta = Math.abs((cl.max-cl.min)/cl.center/2);
                    Double clD = 0.0;
                    
                    for (Double d : cl.dataPoints.values())
                        clD += d;
                    
                    System.out.println("Id:: " + cl.id + " center: " + cl.center + " min: " + cl.min + " max: " 
                            + cl.max + " delta: " + delta + " points num: " + cl.dataPoints.size() + " duration: " + clD);
                    
                    clusterAvgD += clD;
                    clusterAvgS += cl.dataPoints.size();
                    
//                    clusterTotal++;
                }
                clusterAvg = clusterAvgD/clusterAvgS;
                System.out.println("\nAverage filter: " + clusterAvg + "\n" );
                
                for (Cluster cl : curLayer) {
                    Double delta = Math.abs((cl.max-cl.min)/cl.center/2);
                    Double clD = 0.0;
                    for (Double d : cl.dataPoints.values())
                        clD += d;
                    //if (clD >= clusterAvg) {
                        System.out.println("Id:: " + cl.id + " center: " + cl.center + " min: " + cl.min + " max: " 
                                + cl.max + " delta: " + delta + " points num: " + cl.dataPoints.size() + " duration: " + clD);
                        finalLayer.add(cl);
                    //}
                }
                
                break;
            }
            
            if (!tempLayer.isEmpty())
                tempLayer.addAll(singleLayer);
                
            layerNum += 1;
            curLayer = tempLayer;
        }
        
        return finalLayer;
    }
    
    private static Cluster FormDendrogram(ArrayList<Cluster> clusterArray) {
        while (clusterArray.size() != 1) {
            // Find two closest clusterts
            Double minDistance = Double.POSITIVE_INFINITY;
            int index = 0;
            
            for (int i = 0; i < clusterArray.size() - 1; i++) {
                double distance;
                distance = Math.abs(clusterArray.get(i).center - clusterArray.get(i + 1).center);
                
                if (distance < minDistance) {
                    index = i;
                    minDistance = distance;
                }
            }
            
            // Merge clusters
            Cluster tempCluster = new Cluster (clusterArray.get(index), clusterArray.get(index + 1));
            clusterArray.set(index, tempCluster);
            clusterArray.remove(index + 1);
        }
        
        Cluster root = clusterArray.get(0);
        
        return root;
    }
    
}


