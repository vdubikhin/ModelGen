package modelFSM.data.event;

class EventDMV implements EventInfo {
    private Double value;
    public int id;

    EventDMV (double value, int eventId) {
        this.value = value;
        this.id = eventId;
    }
    
    @Override
    public String convertToString() {
        return value.toString();
    }
}
