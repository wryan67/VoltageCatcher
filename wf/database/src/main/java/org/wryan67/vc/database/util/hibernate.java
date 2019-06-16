package org.wryan67.vc.database.util;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.wryan67.vc.common.Util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class hibernate {
    private static Logger logger = Logger.getLogger(hibernate.class);


    private static SynchronizedLong reasonCode=new SynchronizedLong(0L);

    private static InitialContext   initialContext = null;
    private static DataSource       dataSource = null;

    private static final String localTxDatasourceName = "java:/IDManagementDS";


    public static Connection openConnection() throws NamingException, SQLException {
        if (Util.debug) {
            logger.info("DBCaller::opening connection");
        }

        Connection  connection = null;

        try {
            if (initialContext==null) {
                initialContext = new InitialContext();
            }
            if (dataSource==null) {
                dataSource = (DataSource) initialContext.lookup(localTxDatasourceName);
                dataSource.setLoginTimeout(3);
            }
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            if (Util.debug) {
                logger.info("connection.autoCommit="+connection.getAutoCommit());
            }
        } catch (NamingException ne) {
            logger.info("dbcaller open connection threw Naming Exception "+localTxDatasourceName);
            throw ne;
        } catch (SQLException se) {
            logger.info("dbcaller open connection threw SQL Exception "+localTxDatasourceName);
            logger.info("openConnection SQL Exception:"+se.getMessage());
            throw se;
        }

        return connection;
    }

    public static <RecordType> List<RecordType> getHibernateObject(String namedQuery,QueryParms parms, EntityManagerFactory emf, int timeout, Logger logger) {
        EntityManager em = null;
        List<RecordType> results = null;
        try {
            em = emf.createEntityManager();

            Query qry;
            qry = em.createNamedQuery(namedQuery);

            for (String parm: parms.keySet()) {
                qry.setParameter(parm, parms.get(parm));
            }

            qry.setHint("org.hibernate.timeout", timeout);
            results = qry.getResultList();
        } catch (Exception e) {
            logger.error("getHibernateObject("+namedQuery+"): "+e.getMessage(),e);
        } finally {
            if (em!=null) {
                em.clear();
//                em.close();
            }
        }
        return results;

    }

    @SuppressWarnings("unchecked")
    public static <RecordType> List<RecordType> getHibernateObject(String namedQuery,QueryParms parms, EntityManagerFactory emf, Logger logger) {
        return  getHibernateObject( namedQuery, parms,  emf, Util.hibernateTimeout, logger);
    }

    @SuppressWarnings("unchecked")
    public static <RecordType> List<RecordType> getHibernateObject(String namedQuery, EntityManagerFactory emf, Logger logger) {
        EntityManager em = null;
        List<RecordType> results = null;
        try {
            em = emf.createEntityManager();
            Query qry = em.createNamedQuery(namedQuery);
            qry.setHint("org.hibernate.timeout",  Util.hibernateTimeout);
            results = qry.getResultList();
        } catch (Exception e) {
            logger.error("getHibernateObject(" + namedQuery + "): " + e.getMessage(), e);
            try {
            } catch (Exception e1) {
                logger.error("Exception", e1);
            }
        } finally {
            if (em!=null) {
                em.clear();
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public static <RecordType> List<RecordType> getHibernateObject(String namedQuery,String parameterName,Object parameterVal, EntityManagerFactory emf, Logger logger) {
        EntityManager em = null;
        List<RecordType> results = null;
        try {
            em = emf.createEntityManager();

            Query qry = em.createNamedQuery(namedQuery);
            qry.setParameter(parameterName, parameterVal);

            qry.setHint("org.hibernate.timeout",  Util.hibernateTimeout);
            results = qry.getResultList();
        } catch (Exception e) {
            logger.error("getHibernateObject("+namedQuery+"): "+e.getMessage(),e);
            try {
            } catch (Exception e1) {
                logger.error("Exception",e1);
            }
        } finally {
            if (em!=null) {
                em.clear();
            }
        }
        return results;
    }
    public static boolean removeHibernateObj(Object toRemove, EntityManagerFactory emf, Logger logger){
        EntityManager em=null;
        boolean result = false;
        try{
            em = emf.createEntityManager();
            EntityTransaction et = em.getTransaction();
            et.begin();
            toRemove = em.merge(toRemove);
            em.remove(toRemove);
            et.commit();
            result = true;
        }catch(Exception e){
            logger.error("Could not remove Hibernate Obj");
        } finally {
            if (em!=null) {
                em.clear();
            }
        }

        return result;
    }

    public static boolean insertHibernateObject(Object obj, EntityManagerFactory emf, UpdateType type, Logger logger) {
        boolean saved = false;

        EntityTransaction et = null;

        if (emf==null) {
            logger.error("insert: emf is null");
        }

        EntityManager em = emf.createEntityManager();

        try {
            et = em.getTransaction();
            et.begin();

            try {
                em.persist(obj);
            } catch (Exception e) {
                if (et != null) {
                    et.rollback();
                }
                throw e;
            }
            et.commit();
            saved=true;

        } catch (Exception e) {
            if (type.equals(UpdateType.hard))
                logger.error("saveHibernateException exception "+e.getMessage(),e);
            if (et != null) {
                et.rollback();
                saved=false;
            }
        } finally {
            if (em!=null) {
                em.clear();
            }
        }

        return saved;
    }

    public static void upsert(Object obj, EntityManagerFactory emf, UpdateType type, Logger logger) {
        boolean stat=false;
        try {
            stat = hibernate.insertHibernateObject(obj, emf, type, logger);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            Throwable t=e.getCause();
            while (t!=null) {
                logger.warn("caused by:"+ t.getMessage());
                t=t.getCause();
            }
        }

        if (stat==false) {
            hibernate.updateHibernateObject(obj, emf, logger);
        }
    }


    public static boolean updateHibernateObject(Object obj, EntityManagerFactory emf, Logger logger) {
        Boolean result = false;
        EntityTransaction et = null;

        if (emf==null) {
            logger.error("insert: emf is null");
        }

        EntityManager em=null;
        if(obj != null){
            try {
                em = emf.createEntityManager();
                et = em.getTransaction();
                et.begin();

                try {
                    em.merge(obj);

                } catch (Exception e) {
                    if (et != null) {
                        et.rollback();
                    }
                    logger.error("FAILED TO UPDATE HIBERNATE TABLE");
                }
                et.commit();
                result=true;
            } catch (Exception e) {
                logger.error("Unable to exec Hibernate update statement. e:" + e.getMessage(),e);
                if (et != null) {
                    try {
                        et.rollback();
                    } catch (Exception ex) {
                        logger.fatal("Unable to rollback hibernate update sql statement. e:" + ex.getMessage());
                    }
                }
            } finally {
//                logger.error("stopService Hibernate update statement.");
                if (em!=null) {
                    em.close();
                }
            }
        }else{
            logger.error("Hibernate obj is null");
        }
        return result;
    }

    public static long getReasonCode() {
        long reason=reasonCode.get();
//	  logger.error("hibernate:getting reason code="+reason);
        return reason;
    }

    public static void setReasonCode(long reason) {
//	  logger.error("hibernate:setting reason code="+reason);
        reasonCode.set(reason);
    }

    public static ResponseStatus sanityCheck(EntityManagerFactory emf, AppStatus appStatus, CrudStatus crudStatusBean) {
        boolean status=true;
        ResponseStatus reply=new ResponseStatus();
        String dbstat=null;

        try {
            dbstat = new Fetch(emf).<String>one(null, "select AX_PROPERTY_VALUE status from CONFIG_PROPERTIES_TB where AX_REQUEST_TYPE = 'system' and AX_PROPERTY_NAME = 'dbStatus'");
        } catch (RuntimeException e) {
        } catch (Exception e) {}

        if (dbstat!=null && dbstat.equals("green")) {
            if (getReasonCode()==100) startTransactions(appStatus);
        } else {
            status=false;
            String msg=null;
            if (dbstat==null) {
                msg="db is down via sanityCheck";
            } else {
                msg="db is down via config_properties_tb value '"+dbstat+"'";
            }
            reply.addMessage(100,msg);
            stopTransactions(msg,100,appStatus);
        }

        if (appStatus==null || !appStatus.isTransactionsAlive()) {
            status=false;
            reply.addMessage(101,"transactions are not active");
        }

        reply.addMessage(10, (String)MDC.get("instance"));
        reply.addMessage(11, (String)MDC.get("service"));

        if (getReasonCode()==0 || getReasonCode()==201) {
            String crudStatus=crudStatusBean.getCrudStatus();
            if (crudStatus.equals("online")) {
                if (getReasonCode()==201) startTransactions(appStatus);
                reply.addMessage(200,"crud server reports connected ok");
            } else {
                status=false;
                String msg="crud server reports offline";
                reply.addMessage(201,msg);
                stopTransactions(msg,201,appStatus);
            }
        }

        if (status) {
            reply.setStatus(0);
            reply.setMessage("online");
        } else {
            reply.setStatus(9999);
            reply.setMessage("offline");
        }

        return reply;
    }

    private synchronized static void startTransactions(AppStatus appStatus) {
        setReasonCode(0);
        if (!appStatus.isTransactionsAlive()) {
            logger.error("sanity check is restarting transactions");
            appStatus.startX();
        }
    }

    private synchronized static void stopTransactions(String msg,long reasonCode,AppStatus appStatus) {
        setReasonCode(reasonCode);

        if (appStatus.isTransactionsAlive()) {
            logger.error(msg);
            logger.error("sanity check is stopping transactions");
            appStatus.stopX();
        }
    }


    public static boolean insert(Object obj, EntityManagerFactory emf) {
        if (obj==null) return false;

        if (emf==null) {
            logger.error("insert: emf is null");
            return false;
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(obj);
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception e1) {
                throw new RuntimeException("Rollback failed",e);
            }
            throw e;
        }
        tx.commit();

        return true;
    }


    public static boolean insert(Object obj, EntityManagerFactory emf, Logger logger) {
        if (obj==null) return false;

        try {
            if (emf==null) {
                logger.error("insert: emf is null");
            }
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                em.persist(obj);
            } catch (Exception e) {
                logger.error("Unable to insert row::"+e.getMessage());
                logger.error(Util.getStackTrace(e));
                tx.rollback();
                return false;
            }
            tx.commit();
        } catch (Exception e) {
            logger.error("Unable to insert row::"+e.getMessage());
            logger.error(Util.getStackTrace(e));
            return false;
        }

        return true;
    }

    public static boolean update(String sql, EntityManagerFactory emf, Logger logger) {
        if (sql==null) return false;

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query q = em.createNativeQuery(sql);
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            logger.error("Unable to insert row::"+e.getMessage());
        }

        return false;
    }
    public static long getSequenceId(EntityManagerFactory emf) throws OracleSequenceDoesNotExist {
//       hibernate.update("select globseq.nextval from dual",logger);
//       BigInteger i;

        Fetch fetch=new Fetch(emf);

        try {
            return fetch.<BigDecimal>one(logger,"select globseq.nextval from dual").longValue();
        } catch (RuntimeException e) {
            int count=0;
            for (Throwable cause = e;cause!=null && count<30;cause = cause.getCause(),++count) {
                logger.error("cause.getMessage()="+cause.getMessage());
                if (cause.getMessage().contains("ORA-02289")) {
                    throw new OracleSequenceDoesNotExist("Oracle sequence 'globseq' does not exist");
                }
            }
            throw e;
        }

    }


    public static int exec(String sql, EntityManagerFactory emf) {
        if (sql==null) return -1;

        if (emf==null) {
            logger.error("insert: emf is null");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        int rs=em.createNativeQuery(sql).executeUpdate();
        tx.commit();
        em.close();
        return rs;
    }
}
