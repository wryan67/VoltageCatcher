package org.wryan67.vc.database.util;


import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Fetch {
    private static Logger logger=Logger.getLogger(Fetch.class);

    private final EntityManagerFactory emf;

    public Fetch(EntityManagerFactory emf) {
        this.emf=emf;
    }

    @SuppressWarnings("unchecked")
    public <RecordType> List<RecordType> all(String namedQuery, Logger logger) {
        return (List<RecordType>)hibernate.getHibernateObject(namedQuery, emf, logger);
    }
    @SuppressWarnings("unchecked")
    public <RecordType> List<RecordType> rows(String namedQuery,QueryParms parms, Logger logger) {
        return (List<RecordType>)hibernate.getHibernateObject(namedQuery, parms, emf, logger);
    }

    @SuppressWarnings("unchecked")
    public <RecordType> RecordType row(String namedQuery,String parameterName,Object parameterVal, Logger logger) {
        List<RecordType> l = (List<RecordType>)hibernate.getHibernateObject(namedQuery, parameterName, parameterVal, emf, logger);
        if (l!=null && l.size()==1) {
            return l.get(0);
        } else {
            return null;
        }    }

    @SuppressWarnings("unchecked")
    public <RecordType> RecordType row(String namedQuery,QueryParms parms, Logger logger) {
        List<RecordType> l = (List<RecordType>)hibernate.getHibernateObject(namedQuery, parms, emf, logger);
        if (l!=null && l.size()==1) {
            return l.get(0);
        } else {
            return null;
        }
    }
    @SuppressWarnings("unchecked")
    public <RecordType> RecordType firstRow(String namedQuery,QueryParms parms, Logger logger) {
        List<RecordType> l = (List<RecordType>)hibernate.getHibernateObject(namedQuery, parms, emf, logger);
        if (l!=null && l.size()>=1) {
            return l.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <RecordType> ArrayList<RecordType> rowsAL(String namedQuery,String parameterName,Object parameterVal, Logger logger) {
        ArrayList<RecordType> results=new ArrayList<RecordType>();
        List<Object> listResults = hibernate.getHibernateObject(namedQuery, parameterName, parameterVal, emf, logger);

        if(listResults != null){
            for (Object obj : listResults) { results.add((RecordType)obj);}
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public <RecordType> RecordType one(Logger logger,String sql) {
        if (sql==null) return null;

        EntityManager em = emf.createEntityManager();
        Query q = em.createNativeQuery(sql);

        RecordType r = (RecordType) q.getSingleResult();
        if (logger!=null) logger.info("Fetch::one:row="+r);
        return r;
    }

    public List<String> getStrings(String sql, boolean commit) {
        if (sql==null) return null;
        List<String> rs=null;
        EntityManager em = emf.createEntityManager();
        if (commit) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            rs=em.createNativeQuery(sql).getResultList();
            tx.commit();
        } else {
            rs=em.createNativeQuery(sql).getResultList();
        }
        em.close();

        return rs;
    }


    public <RecordType> List<RecordType> rows(Connection connection, String className, String sql) {
        Class tableClass=null;
        String lookup= dbCaller.dbPackage+"."+className;

        try {
            tableClass = Class.forName(lookup);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot locate table class ("+lookup+") for table "+className);
            return null;
        }

        ArrayList<RecordType> output = null;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            StringBuffer selectStatement = new StringBuffer(2048);

            selectStatement.append(sql);

//            logger.info("fetchRows::sql="+selectStatement.toString());
            preparedStatement = connection.prepareStatement(selectStatement.toString());

            preparedStatement.setQueryTimeout(4);
            resultSet = preparedStatement.executeQuery();

            int size = resultSet.getFetchSize();
            //          logger.info("resultSet size="+size);
            output = new ArrayList<RecordType>(size);

            ResultSetMetaData md = resultSet.getMetaData();

            while (resultSet.next()) {
                RecordType rt=mapResult(tableClass,md,resultSet);
                output.add(rt);
            }
            return output;

        } catch (SQLException se) {
            logger.info(this.getClass().getName()+" Query threw SQL Exception ");
            logger.info("fetchRows: SQL Exception:"+se.getMessage());
        } finally {
            if (resultSet != null) {try {resultSet.close();} catch (SQLException se) {
                logger.error("Could not close SQL ResultSet: " + se.getMessage() + " Error Code:" + se.getErrorCode());
            }}
            if (preparedStatement != null) {try {preparedStatement.close();} catch (SQLException se) {
                logger.error("Could not close SQL PreparedStatement: " + se.getMessage() + " Error Code:" + se.getErrorCode());
            }}
        }
        //closeConnection(connection);
        return null;
    }



    private <RecordType> RecordType mapResult(Class recordType, ResultSetMetaData md, ResultSet resultSet) throws SQLException {
        RecordType row=null;
        try {
            row=(RecordType) recordType.newInstance();
        } catch (InstantiationException e) {	logger.error("mapResult:"+e.getMessage());
        } catch (IllegalAccessException e) {	logger.error("mapResult:"+e.getMessage());
        }

        Method[] setters = recordType.getMethods();

        for (Method setter:setters) {
            if (setter.getName().startsWith("set")) {
                Class<?>[] parms = setter.getParameterTypes();
                if (parms.length==1) {
                    mapField(parms[0].getName(),setter,row,md,resultSet);
                }
            }
        }

        return row;
    }


    private void mapField(String className, Method setter, Object row, ResultSetMetaData md, ResultSet resultSet) {
        String column=setter.getName().substring(3);
        Object value=null;

        try {
            value=resultSet.getObject(column);
            String c=(value==null)?"n/a":value.getClass().getName();
            if (c.equals("java.sql.Timestamp")) {
                try {
                    value=((Timestamp)value).getTime();
                } catch (Exception e) {
                    logger.error("ojdbc8 not in classpath",e);

                    try {
                        value=((java.sql.Timestamp)value).getTime();
                    } catch (RuntimeException e1) {
                        logger.error("Could not map column " + column, e1);
                        throw e1;
                    }
                }
            }
            if (c.equals("java.math.BigDecimal")) {
                Class<?>[] parms = setter.getParameterTypes();
                if (parms[0].getName().equals("java.lang.Long"))
                    value=new Long(((java.math.BigDecimal)value).longValue());
            }

            setter.invoke(row,value);
            return;
        } catch (SQLException e) {				printError(column,e,value);
        } catch (IllegalArgumentException e) {	printError(column,e,value);
        } catch (IllegalAccessException e) {	printError(column,e,value);
        } catch (InvocationTargetException e) {	printError(column,e,value);
        }
    }

    private void printError(String column, Exception e, Object value) {
        String v=(value==null)?"null":value.toString();
        String c=(value==null)?"n/a":value.getClass().getName();
        logger.error("mapField:Column "+column+"  "+e.getMessage()+" Object Value="+v+" Object Class="+c,e);
    }

    public int updateRows(Connection connection, String tableName, Object oldRecord, String matchClause) {
        StringBuilder sql=new StringBuilder(1024);
        ArrayList<Object> values=new ArrayList<Object>();

        sql.append("update ");
        sql.append(tableName);
        sql.append(" set ");
        buildSets(sql,oldRecord,values);
        sql.append(" where ");
        sql.append(matchClause);

//		sql.append(";");   sdf

        PreparedStatement preparedStatement = null;
        int status=-1;

        try {
            //logger.info("DBCaller.updateRows");

            preparedStatement = connection.prepareStatement(sql.toString());

            int col=1;
            for (Object o:values) {
                String v=(o==null)?"null":o.toString();
                String c=(o==null)?"n/a":o.getClass().getName();
//            	logger.info("setting column "+col+" type="+c+" value="+v);
                preparedStatement.setObject(col++, o);
            }
            status=preparedStatement.executeUpdate();

        } catch (SQLException se) {
            logger.info("dbcaller::updateRows:sql="+sql.toString());
            logger.info("dbcaller::updateRows:values="+ Util.join(values,", "));

            logger.error(this.getClass().getName()+"::updateRows:sql Query threw SQL Exception "+se.getMessage()+" "+" <sql="+sql.toString()+">",se);
        } finally {
//            logger.info("Closing update SQL PreparedStatement: ");

            if (preparedStatement != null) {try {preparedStatement.close();} catch (SQLException se) {
                logger.error("Could not close SQL PreparedStatement: " + se.getMessage() + " Error Code:" + se.getErrorCode());
            }}
        }
        if (status<0) {
            logger.error("SQL failed, unknown reason: "+sql.toString());
        }

        return status;

    }

    private void buildSets(StringBuilder sql, Object oldRecord, ArrayList<Object> values) {
        Method[] getters = oldRecord.getClass().getMethods();

        int count=0;
        for (Method getter:getters) {
            switch (getter.getName()) {
                case "getClass":
                case "getBusinessKey":
                case "getSyncTimestampColumn":
                case "getDelayIfColumn":
                case "getDelayIfMissingRecord":
                case "getTableName":
                case "getData": {
                    continue;
                }
            }


            if (getter.getName().startsWith("get")) {
                Class<?>[] parms = getter.getParameterTypes();
                if (parms.length==0) {
                    Object value=null;
                    try {
                        value=getter.invoke(oldRecord);
                    } catch (IllegalArgumentException e) {		logger.error("IllegalArgumentException",e);
                    } catch (IllegalAccessException e) {		logger.error("IllegalAccessException",e);
                    } catch (InvocationTargetException e) {		logger.error("InvocationTargetException",e);
                    }

                    if (count++>0) sql.append(",");
                    sql.append(" ");
                    sql.append(getter.getName().substring(3));
                    sql.append(" = ? ");

                    values.add(value);
//					sql.append(sqlQuoteValue(getter,oldRecord));
//					sql.append(" ");
                }
            }
        }



    }
}

