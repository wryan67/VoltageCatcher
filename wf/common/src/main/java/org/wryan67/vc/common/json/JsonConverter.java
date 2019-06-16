package org.wryan67.vc.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.common.timing.CommonTiming;
import org.wryan67.vc.common.timing.TimingStats;

import java.io.IOException;

public class JsonConverter {
    private static Logger logger = Logger.getLogger(JsonConverter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final boolean shrink;
    private final boolean dropRootNode;
    private final String rootNodeName[];

    public JsonConverter(boolean dropRootNode, final boolean shrink,  String... rootNodeName) {
        this.dropRootNode=dropRootNode;
        this.rootNodeName=rootNodeName;
        this.shrink=shrink;




    }


    public JsonStatus fromJSON(Object obj, String request, boolean addRootNode) {
        TimingStats start=new TimingStats();

        try {
            String jsonContents=request;
            if (addRootNode) {
                StringBuilder rq=new StringBuilder(request.length()+obj.getClass().getName().length()+10);
                rq.append("{\"");
                rq.append(obj.getClass().getName());
                rq.append("\":");
                rq.append(request);
                rq.append("}");
                jsonContents=rq.toString();
            }

            try {
                mapper.readerForUpdating(obj).readValue(jsonContents);
                return new JsonStatus(0,"success");

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new JsonStatus(9000, "JsonConversion Error " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
                return new JsonStatus(9000, "JsonConversion Error " + e.getMessage());
            }



        } finally {
            if (Util.debug) {
                long end=System.currentTimeMillis();
                CommonTiming.log(start.end(),"fromJSON", obj.getClass().getSimpleName());
            }
        }
    }

    /***
     *
     * This method should NOT be for production class methods.
     *
     * Generic convert of object to JSON using the Jackson library
     *
     * @param object          object to convert
     * @param rootName   should be hard coded.
     * @return Returns JSON string or error message.
     */
    public String toJSON(Object object, String rootName) {
        if (object==null) {
            if (rootName==null) {
                return "{}";
            } else {
                return "{\"" + rootName +"\": {}";
            }
        }

        return JsonConverter.toJson(object,rootName,mapper,shrink);
    }

    public static String toJson(Object object, String rootName, ObjectMapper mapper, boolean shrink) {
        try {
            if (rootName==null) {
                if (shrink) {
                    return mapper.writeValueAsString(object);
                } else {
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                }
            } else {
                return "{\"" + rootName + "\": " + mapper.writeValueAsString(object) + "}";
            }
        } catch (IOException e) {
            if (!Util.isBlankOrNull(rootName) || object==null) {
                return "unable to convert "+rootName+" to JSON";
            } else {
                return "unable to convert "+object.getClass().getSimpleName()+" to JSON";
            }
        }
    }

}
