package org.wryan67.vc.database.util;

/**
 * Created by wryan on 4/2/2015.
 */
public interface AppStatus {
    public boolean isTransactionsAlive();
    public void setTransactionsAlive(boolean status);

    public void startX();
    public void stopX();
}
