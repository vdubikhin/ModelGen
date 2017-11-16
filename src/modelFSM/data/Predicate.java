package modelFSM.data;

public class Predicate extends ArrayListWrapper<Event> {
    private static final long serialVersionUID = -2414928492298841779L;
    private final int predicateId;
    
    public Predicate(int id) {
        super();
        predicateId = id;
    }
    
    public Predicate(Predicate copy) {
        super(copy);
        this.predicateId = copy.predicateId;
    }
    
    public int getId() {return predicateId;}
}
