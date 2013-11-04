package eu.lod2.rsine.registrationservice;

public class Condition {

    private String askQuery;
    private boolean expectedResult;

    public Condition(String askQuery, boolean expectedResult) {
        this.askQuery = askQuery;
        this.expectedResult = expectedResult;
    }

    public String getAskQuery() {
        return askQuery;
    }

    public boolean getExpectedResult() {
        return expectedResult;
    }
}
