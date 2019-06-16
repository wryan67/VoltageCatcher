package org.wryan67.vc.database.util;

import org.apache.log4j.Logger;
import org.wryan67.vc.common.AppConstants;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.database.tables.OPTIONS;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by wryan on 2/18/2016.
 */
public class dbCaller {
    private static final Logger logger = Logger.getLogger(dbCaller.class);

    public static final String dbPackage= Util.chomp(OPTIONS.class.getName(),"."+ OPTIONS.class.getSimpleName());

    public static EntityManagerFactory emf;
    static {
        try {
            emf = Persistence.createEntityManagerFactory(AppConstants.persistenceUnit);
        } catch (Exception e) {
            logger.error("Persistence unit "+AppConstants.persistenceUnit +" is not available");
        }
    }

    private static final Fetch fetch=new Fetch(dbCaller.emf);



}
