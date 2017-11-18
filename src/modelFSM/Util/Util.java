package modelFSM.shared;

import java.util.ArrayList;

import modelFSM.data.Event;

public final class Util {

    private Util() {};
    
    static public ArrayList<Event> dataToEvents(String name, ArrayList<Double> timeArray, ArrayList<Integer> dataArrayGroup) {
        // Convert read data to event format
        // Event - duration pair
        try {
            ArrayList<Event> outEvents = new ArrayList<Event>();
            
            Double minTime = timeArray.get(0);
            Double maxTime = timeArray.get(0);
            
            int curGroup = dataArrayGroup.get(0);
            
            for (int i = 0; i < dataArrayGroup.size(); i++) {
                if (curGroup != dataArrayGroup.get(i)) {
                    maxTime = timeArray.get(i-1);
                    Event tempEvent = new Event();
                    
                    //TODO: think on treating 0 group as a special one
                    // Filtering
                //    if (curGroup != 0) {
                        tempEvent.eventId = curGroup;
                        tempEvent.start = minTime;
                        tempEvent.end = maxTime;
                        tempEvent.signalName = name;
                        outEvents.add(tempEvent);
                  //  }
                        
                    minTime = timeArray.get(i);
                    curGroup = dataArrayGroup.get(i);
                }
            }
            
            Event tempEvent = new Event();
            maxTime = timeArray.get(dataArrayGroup.size()-1);
            tempEvent.eventId = curGroup;
            tempEvent.start = minTime;
            tempEvent.end = maxTime;
            tempEvent.signalName = name;
            outEvents.add(tempEvent);
            
            return outEvents;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
