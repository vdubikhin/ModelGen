package modelgen.data.state;

import modelgen.data.ArrayListWrapper;

public class StatePattern extends ArrayListWrapper<IState> {
    private static final long serialVersionUID = -2414928492298841779L;
    private final int patternId;
    
    public StatePattern(int id) {
        super();
        patternId = id;
    }
    
    public StatePattern(StatePattern copy) {
        super(copy);
        this.patternId = copy.patternId;
    }
    
    public int getId() {return patternId;}
}
