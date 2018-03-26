package modelgen.processor.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import modelgen.data.ControlType;
import modelgen.data.complex.Mergeable;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;

public class FilterDataByPattern extends FilterDataBase implements IDataProcessor<StageDataState> {
    final private static String PD_WINDOW_SIZE_RATIO = PD_PREFIX + "WINDOW_SIZE_RATIO";
    final private static String PD_PATTERN_SIZE = PD_PREFIX + "PATTERN_SIZE";

    final private Integer VALUE_BASE_COST = 8;
    final private Integer PATTERN_SIZE = 1;
    final private Double WINDOW_SIZE_RATIO = 0.05;

    PropertyInteger patternSize;
    PropertyDouble windowSizeRatio;

    List<IState> filteredStates;

    protected RawDataChunkGrouped inputData;
    protected ControlType inputType;
    protected String inputName;
    protected List<IState> inputStates;

    public FilterDataByPattern() {
        ERROR_PREFIX = "DataProcessor: FilterDataByPattern error. ";
        DEBUG_PREFIX = "DataProcessor: FilterDataByPattern debug. ";

        name = "FilterDataByPattern";

        valueBaseCost.setValue(VALUE_BASE_COST);

        windowSizeRatio = new PropertyDouble(PD_WINDOW_SIZE_RATIO);
        windowSizeRatio.setValue(WINDOW_SIZE_RATIO);

        patternSize = new PropertyInteger(PD_PATTERN_SIZE);
        patternSize.setValue(PATTERN_SIZE);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(windowSizeRatio.getName(), windowSizeRatio);
        moduleProperties.put(patternSize.getName(), patternSize);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public FilterDataByPattern(StageDataState inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
        this.inputStates = inputData.getStates();
    }

    class StateIdPattern extends ArrayList<Integer> {
        private static final long serialVersionUID = -5055706360044518486L;
        Double weight;
        List<IState> windowStates;

        StateIdPattern(List<IState> states) {
            super();
            windowStates = states;
            weight = null;
        }

        StateIdPattern(List<IState> states, Integer stateId) {
            super();
            add(stateId);
            windowStates = states;
            weight = null;
        }

        StateIdPattern(StateIdPattern pattern) {
            super();
            addAll(pattern);
            windowStates = pattern.windowStates;
            weight = null;
        }

        Double weightFunction(double duration) {
            if (size() > 1)
                return duration/(size() - 1);
            else
                return duration;
        }

        Double getWeight() {
            if (weight != null)
                return weight;

            if (windowStates == null || windowStates.isEmpty())
                return null;

            if (isEmpty())
                return null;

            double totalDuration = 0.0;

            int curPatternId = 0;
            int prevPatternId = 0;

            for (IState state: windowStates) {
                if (state.getId().equals(get(curPatternId))) {
                    totalDuration += state.getDuration();
                    prevPatternId = curPatternId;
                    curPatternId = (curPatternId + 1) % size();
                    continue;
                }

                if (state.getId().equals(get(prevPatternId))) {
                    totalDuration += state.getDuration();
                }
            }
            
            weight = weightFunction(totalDuration);
            return weight;
        }

        void addStateId(Integer id) {
            add(id);
            weight = null;
        }

        boolean allSame() {
            if (isEmpty() || size() == 1)
                return true;

            Integer firstId = get(0);

            for (Integer id: this) {
                if (id.compareTo(firstId) != 0)
                    return true;
            }

            return false;
        }

    }

    private List<StateIdPattern> expandPatterns(List<Integer> inputId, List<IState> windowStates) {
        if (inputId == null || inputId.isEmpty()) 
            return null;

        List<StateIdPattern> allPatterns = new ArrayList<>();
        List<StateIdPattern> curPatterns = new ArrayList<>();

        int iteration = 0;

        while (iteration < patternSize.getValue()) {
            List<StateIdPattern> nextPatterns = new ArrayList<>();
            for (Integer id: inputId) {
                if (curPatterns.isEmpty()) {
                    StateIdPattern curPattern = new StateIdPattern(windowStates, id);
                    nextPatterns.add(curPattern);
                } else {
                    for (StateIdPattern pattern: curPatterns) {
                        StateIdPattern curPattern = new StateIdPattern(pattern);
                        curPattern.addStateId(id);
                        nextPatterns.add(curPattern);
                    }
                }
            }

            curPatterns = nextPatterns;
            allPatterns.addAll(curPatterns);
            iteration += 1;
        }

        allPatterns = allPatterns.stream()
                .filter(p -> p.allSame())
                .collect(Collectors.toList());

        return allPatterns;
    }
    
    @Override
    public int processCost() {
        try {
            if (inputStates == null || inputStates.isEmpty())
                return -1;

            if (filteredStates != null)
                return costFunction();

            final int totalEvents = inputStates.size();
            final int windowSize = (int) Math.ceil(totalEvents * windowSizeRatio.getValue());

            filteredStates = new ArrayList<>();

            System.out.println("windowSize: " + windowSize);

            for (int i = 0; i < totalEvents; i++) {
                int lowB = Math.max(0, i - windowSize/2);
                int upB = Math.min(totalEvents, i + windowSize/2);

                List<IState> windowStates = inputStates.subList(lowB, upB);
                List<Integer> windowId = windowStates.stream()
                        .map(s -> s.getId())
                        .distinct()
                        .collect(Collectors.toList());

                List<StateIdPattern> windowPatterns = expandPatterns(windowId, windowStates);

                if (windowPatterns == null || windowPatterns.isEmpty()) {
                    filteredStates = null;
                    return -1;
                }

                StateIdPattern maxPattern = windowPatterns.stream()
                        .max( (p1, p2) -> ( Double.compare(p1.getWeight(), p2.getWeight()) )
                                )
                        .get();

                if (maxPattern.contains(inputStates.get(i).getId()))
                    filteredStates.add(inputStates.get(i));

                System.out.println(maxPattern + " weight: " + maxPattern.getWeight());
            }


            filteredStates = correctStates(inputStates, filteredStates);
            Mergeable.mergeEntries(filteredStates);

            if (filteredStates == null)
                Logger.errorLogger(ERROR_PREFIX + " Failed to filter signal: " + inputName);

            return costFunction();
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    private int costFunction() throws NullPointerException {
        if (filteredStates != null)
            return filteredStates.size() * valueBaseCost.getValue();

        return -1;
    }

    @Override
    public StageDataState processData() {
        try {
            return processData(filteredStates, inputData, inputName, inputType);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}
