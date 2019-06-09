package org.wryan67.vc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;

import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Util {
    private static final FastDateFormat slickDate=FastDateFormat.getInstance("yyyy-MM-dd-HH");

    private static String hostAddress;
    private static String shortHostAddress;
    private static Logger logger = Logger.getLogger(Util.class);
    private static String env;
    public  static boolean debug=false;
    public static int hibernateTimeout=3;
    public static boolean loadtest=false;
    public static boolean logHeaders=false;
    public static boolean logIO=true;
    public static boolean isBatch=false;
    public static Boolean logCommonTiming=true;
    private static boolean vfsInit=false;



    public static int charCount(final String s, final char c) {
        char[] chars=s.toCharArray();
        int found=0;
        for (int i=0; i<chars.length; i++) {
            if (chars[i]==c) found++;
        }
        return found;
    }

    static {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostAddress = inetAddress.getHostName();
            String parts[] = hostAddress.split("\\.");
            shortHostAddress=parts[0];
        } catch (Exception e) {}
    }

    public static String getInstanceName() {
        if (env==null) {
            return initInstance();
        } else {
            return env;
        }
    }


    public static String encodeHTML(String s) {
        if (Util.isBlankOrNull(s)) return "";
        return StringEscapeUtils.escapeHtml4((s));
    }

    public static String encodeURL(String s) {
        if (Util.isBlankOrNull(s)) return "";
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String decodeHTML(String s) {
        if (Util.isBlankOrNull(s)) return "";
        return StringEscapeUtils.unescapeHtml4(s);
    }

    public static String deocdeURL(String s) {
        if (Util.isBlankOrNull(s)) return "";
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }



    private synchronized static String initInstance() {
        if (env==null) {
            env=System.getenv().get("INSTANCE");
        }
        if (env==null) {
            System.out.println("The environment variable INSTANCE is not defined");
            env="dev";
        }
        return env;
    }


    public static String encodePipe(String s) {
        if (s==null) {
            return null;
        } else {
            return s.replace("|", "\\\\p");
        }
    }
    public static String removeCRNL(String s) {
        if (s == null) {
            return null;
        } else {
            return s.replace("\n", "").replace("\r", "");
        }
    }
    public static String scrub(String s) {
        StringBuffer tmpstr = new StringBuffer(s.length()*2);
        byte b[]=s.getBytes();
        for(int i=0; i<b.length; i++) {
            char c=(char) (b[i] & 0xff);

            if( c<0 || c>126 || !isPrintableChar(c)) {
                tmpstr.append(' ');
            } else {
                tmpstr.append(c);
            }
        }
        return tmpstr.toString();
    }
    //    public static String encodeHTML(String s) {
//        StringBuffer tmpstr = new StringBuffer(s.length()*2);
//        byte b[]=s.getBytes();
//        for(int i=0; i<b.length; i++) {
//            char c=(char) (b[i] & 0xff);
//
//            if( c<0 || c>126 || c=='"' || c=='<' || c=='>' || !isPrintableChar(c)) {
//                tmpstr.append("&#");
//                tmpstr.append(""+(int)(c));
//                tmpstr.append(";");
//            } else {
//                tmpstr.append(c);
//            }
//        }
//        return tmpstr.toString();
//    }
    public static boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }
    public static boolean isPrintableChar(byte b) {
        return isPrintableChar((char) b);
    }

    public static String getStackTrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
    public static String getAppserverId() {
        return hostAddress;
    }
    public static String getAppserverIdShortName() {
        return shortHostAddress;
    }

    public static  String getGUID() {
        String uuid = UUID.randomUUID() + "";
        uuid = uuid.replace("-", "").trim();
        return uuid;
    }


    //    @SuppressWarnings("unchecked")
    public static String getClassName(Class c) {
        String FQClassName = c.getName();
        int firstChar;
        firstChar = FQClassName.lastIndexOf ('.') + 1;
        if ( firstChar > 0 ) {
            FQClassName = FQClassName.substring ( firstChar );
        }
        return FQClassName;
    }

    public static String right(String s,int n) {
        if (s==null) return null;
        int l = s.length();
        if (l<=n) return s;
        return s.substring(l-n,l);
    }
    public static String last4(String s) {
        if (s==null) return null;
        int l = s.length();
        if (l<=4) return s;
        return s.substring(l-4,l);
    }

    /**
     * substring s starting in position startTiming (1 is first position) for length characters.
     * @param s
     * @param start
     * @param length
     */
    public static String substr(String s, int start, int length) {
        if (s==null) return null;
        if (start<1) return null;
        int l=s.length();
        if (start>l) return null;
        if (start+length>l) length=l-start;

        return s.substring(start-1,start+length-1);
    }
    public static boolean isBlankOrNull(String s) {
        if (s==null) return true;
        if (s.trim().equals("")) return true;
        return false;
    }
    public static String unixDateFromat2simpleDateFormat(String unixDateFormat) {
        String sdf=unixDateFormat.
                replaceAll("%Y", "yyyy").      // 4 digit year
                replaceAll("%y", "yy").        // 2 digit year
                replaceAll("%m", "MM").        // 2 digit month 01-12
                replaceAll("%d", "dd").        // 2 digit day
                replaceAll("%H", "HH").        // 2 digit hour   00-23
                replaceAll("%M", "mm").        // 2 digit minute 00-59
                replaceAll("%S", "ss").        // 2 digit second 00-60
                replaceAll("%p", "aa").        // AM/PM indicator
                replaceAll("%I", "hh").        // hour 01-12
                replaceAll("%T", "HH:MM:SS").  // time
                replaceAll("%a", "EEE").       // weekday eg Mon
                replaceAll("%b", "MMM").       // 3 letter month
                replaceAll("%j", "DDD")        // day in year 001-366
// 		                          replaceAll("%s", "")         // epoch timestamp (number of seconds since jan 1, 1970
                ;

        return sdf;
    }
    public static boolean stringIn(String target,  String... inValues) {
        if (inValues.length<1) return false;
        for (int i=0;i<inValues.length;++i) {
            if (target==null) {
                if (inValues[i]==null) return true;
            } else {
                if (target.equals(inValues[i])) return true;
            }
        }
        return false;
    }


    public static String join(List inputList, String separator) {
        if (separator==null) return "";
        if (inputList==null) return "";
        if (inputList.size()<1) return "";
        if (inputList.size()==1) {
            if (inputList.get(0)==null) {
                return "";
            } else {
                return inputList.get(0).toString();
            }
        }

        int elementSize=(int)(inputList.get(0).toString().length()*1.2);

        StringBuffer tmpstr=new StringBuffer(inputList.size()*elementSize);

        int i;
        for (i=0;i<inputList.size()-1;++i) {
            if (inputList.get(i)!=null) tmpstr.append(inputList.get(i));
            tmpstr.append(separator);
        }   if (inputList.get(i)!=null) tmpstr.append(inputList.get(i));

        return tmpstr.toString();
    }

    public static String join(Integer inputList[], String separator) {
        if (inputList==null) return null;
        return join(new ArrayList<Object>(Arrays.asList(inputList)),separator);
    }

    public static String join(Double[] inputList, String separator) {
        if (inputList==null) return null;
        return join(new ArrayList<Object>(Arrays.asList(inputList)),separator);
    }

    public static String join(double[] inputList, String separator) {
        if (inputList==null) return null;
        Double doubles[] = new Double[inputList.length];
        for (int i=0;i<inputList.length;++i) {
            doubles[i]=inputList[i];
        }
        return join(new ArrayList<Object>(Arrays.asList(doubles)),separator);
    }



    public static String join(String inputList[], String separator) {
        if (inputList==null) return null;
        return join(new ArrayList<Object>(Arrays.asList(inputList)),separator);
    }

    public static String zeroFillIp(String ip) {
        String octets[]=ip.split("\\.");

        return String.format("%3s.%3s.%3s.%3s",octets[0],octets[1],octets[2],octets[3]).replaceAll(" ","0");
    }

    public static boolean isValidUUID(String uuid) {
        if (Util.isBlankOrNull(uuid)) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String truncateString(String s, int len) {
        if (s==null) return null;
        if (s.length()<=len) return s;
        return s.substring(0,len);
    }

    public static Integer iParse(String s,Integer defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Double dParse(String s, Double defaultValue) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Float fParse(String s, Float defaultValue) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getRootDomain(String host) {
        if (Util.isBlankOrNull(host)) return "";
        return host.replaceAll(".*\\.(?=.*\\.)", "");
    }
    public static String getSubDomain(String host) {
        if (Util.isBlankOrNull(host)) return "";
        String parts[]=host.split("\\.");
        StringBuilder result=new StringBuilder(host.length()+3);
        int start=(parts.length>3)?parts.length-3:0;

        for (int i=start;i<parts.length;++i) {
            if (i>start) result.append(".");
            result.append(parts[i]);
        }
        return result.toString();
    }

    public static boolean matches(String s, String pattern) {
        if (s==null || pattern==null) {
            return (s==pattern);
        } else {
            return s.equals(pattern);
        }
    }

    public static boolean isPrivateIp(String ip) {
        return ip.startsWith("10.");
    }

    public static Object getValueOrDefault(Object bean, String field, String defaultValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (bean==null) {
            return defaultValue;
        }

        try {
            Method getter = bean.getClass().getMethod("get" + field);

            Object value = getter.invoke(bean);

            if (value==null) {
                return defaultValue;
            } else {
                return value;
            }

        } catch (NoSuchMethodException e) {
            logger.error("NoSuchMethodException::bean="+bean.getClass()+" field="+field);
            throw e;
        } catch (InvocationTargetException e) {
            logger.error("InvocationTargetException::bean="+bean.getClass()+" field="+field);
            throw e;
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException::bean="+bean.getClass()+" field="+field);
            throw e;
        }


    }

    /**
     * Chomp removes a suffix from a string (if it exists)
     * @param s
     * @param suffix
     * @return
     *      return null if input is null, otherwise, return string with suffix removed.
     */
    public static String chomp(String s, String suffix) {
        if (Util.isBlankOrNull(s) || Util.isBlankOrNull(suffix)) {
            return s;
        }
        String match=s;
        if (match.endsWith(suffix)) {
            match=match.substring(0,s.length()-suffix.length());
        }
        return match;
    }

    public static String tolower(String s) {
        if (s==null) {
            return null;
        } else {
            return s.toLowerCase();
        }
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (Util.isBlankOrNull(s1) && Util.isBlankOrNull(s2)) return true;
        if (Util.isBlankOrNull(s1) || Util.isBlankOrNull(s2)) return false;

        return s1.toLowerCase().equals(s2.toLowerCase());
    }
    public static boolean equals(String s1, String s2) {
        if (Util.isBlankOrNull(s1) && Util.isBlankOrNull(s2)) return true;
        if (Util.isBlankOrNull(s1) || Util.isBlankOrNull(s2)) return false;

        return s1.equals(s2);
    }

    public static boolean equals(List a1, List a2) {
        if (a1==a2) return true;
        if (a1==null || a2==null) return false;
        if (a1.size()!=a2.size()) return false;

        for (int i=0;i<a1.size();++i) {
            if (((Comparable)a1.get(i)).compareTo(a2.get(i))!=0) return false;
        }

        return true;
    }

    public static int index(String needle, String[] haystack) {
        if (needle==null) return -1;
        if (haystack==null) return -1;

        for (int i = 0; i < haystack.length; i++) {
            if (needle.equals(haystack[i])) {
                return i;
            }
        }
        return -1;
    }


    public static boolean contains(String needle, String[] haystack) {
        if (needle==null) return false;
        if (haystack==null) return false;

        for (String s : haystack) {
            if (needle.equals(s)) {
                return true;
            }
        }
        return false;
    }


    public static boolean contains(String needle, String haystack) {
        if (needle==null) return false;
        if (haystack==null) return false;

        return haystack.contains(needle);
    }

    public static boolean containsIgnoreCase(String needle, String haystack) {
        if (needle==null) return false;
        if (haystack==null) return false;

        return haystack.toLowerCase().contains(needle.toLowerCase().trim());
    }


    public static String getField(String s, String regex, int fieldNumber) {
        if (s==null) {
            return null;
        }
        String parts[]=s.split(regex);

        if (fieldNumber== Integer.MAX_VALUE) {
            return parts[parts.length-1];
        } else {
            return parts[fieldNumber];
        }
    }

    public static String trim(String s) {
        if (s==null) {
            return s;
        } else {
            return s.trim();
        }
    }


    public static int compare(Comparable s1, Comparable s2) {
        if (s1==s2) return 0;
        if (s1==null) return -1;
        if (s2==null) return 1;
        return s1.compareTo(s2);
    }


    public static void flush() {
        int bufferSize=4096;
        StringBuffer sb=new StringBuffer(bufferSize);
        for (int i=0;i<bufferSize/32;++i) {
            sb.append(Util.getGUID());
        }

        logger.info("Force logging flush::"+sb.toString());
    }

    public static String simpleName(String serverName) {
        if (serverName==null) return null;
        String parts[]=serverName.split("\\.");
        return parts[0];
    }

    public static String subnet(String sourceIP) {
        if (sourceIP==null) return null;
        String parts[]=sourceIP.split("\\.");
        StringBuilder subnet=new StringBuilder(sourceIP.length());
        subnet.append(parts[0]);
        subnet.append('.');
        subnet.append(parts[1]);
        subnet.append('.');
        subnet.append(parts[2]);
        subnet.append(".*");

        return subnet.toString();
    }



    public static void addIfNotExist(ArrayList<String> a, String s) {
        if (a!=null && !a.contains(s)) {
            a.add(s);
        }
    }

    public static String join(char separator, String... a) {
        if (a==null || a.length==0) {
            return "";
        }
        if (a.length==1) {
            return a[0];
        }

        int len=0;
        for (int i=0;i<a.length;++i) { len+=a.length; }
        StringBuilder s=new StringBuilder(a.length+len);
        s.append(a[0]);
        for (int i=1; i<a.length; ++i) {
            s.append(separator);
            s.append(a[i]);
        }
        return s.toString();
    }


    /***
     *
     * This method should NOT be for production class methods.  Use JsonConverter for production classes.
     *
     * Generic convert of object to JSON using the Jackson library
     *
     * @param object          object to convert
     * @param rootName   should be hard coded.
     * @return Returns JSON string or error message.
     */
    public static String toJSON(Object object, String rootName) {
        if (object==null) {
            if (rootName==null) {
                return "{}";
            } else {
                return "{\"" + rootName +"\": {}";
            }
        }

        Object m=ADC.get("ObjectMapper");
        if (m==null) {
            m=new ObjectMapper();
            ADC.put("ObjectMapper",m);
        }
        ObjectMapper mapper = (ObjectMapper)m;

        return JsonConverter.toJson(object,rootName,mapper,false);

    }

    public static String getPackageName(String fullClasspath) {
        String parts[]=fullClasspath.split("\\.");
        int n=parts.length;
        if (n<2) {
            return fullClasspath;
        }
        return Util.join(Arrays.copyOf(parts,parts.length-1),".");
    }

    public static long sum(long... values) {
        long sum=0;

        if (values==null) {
            return 0;
        }

        for (int i=0;i<values.length;++i) {
            sum+=values[i];
        }
        return sum;
    }

}
