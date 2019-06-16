package org.wryan67.vc.database.util;

public class OracleSequenceDoesNotExist extends RuntimeException {
    String message="ORA-02289 Oracle sequence does not exist";

    OracleSequenceDoesNotExist() {
        super();
    }
    OracleSequenceDoesNotExist(String message) {
        super();
        this.message=message;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
