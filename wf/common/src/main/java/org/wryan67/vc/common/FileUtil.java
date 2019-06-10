package org.wryan67.vc.common;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;


public class FileUtil {
    private static Logger logger = Logger.getLogger(FileUtil.class);

    public static boolean cpFiles(String source, String target) {

        logger.info("cpFiles: source="+source+" target="+target);

        File src=new File(source);
        File tgt=new File(target);

        if (!src.exists()) {
            logger.info("cpFiles: source is not a folder");
            return false;
        }

        if (src.isDirectory() && tgt.isDirectory()) {
            return cpFolder(src,tgt);
        }

        logger.info("cpFiles: unknown error");

        return false;

//		try {
//			String cmd="cp "+source+" "+target;
//			Process p=Runtime.getRuntime().exec(cmd);
//			int RET=p.waitFor();
//			if (RET!=0) {
//				BufferedReader reader=new BufferedReader(new InputStreamReader(p.getErrorStream()));
//				String line=reader.readLine();
//				logger.error("cmd="+cmd);
//				while(line!=null) {
//					logger.error(line);
//					line=reader.readLine();
//				}
//			}
//			return RET;
//		}
//		  catch(InterruptedException e2) {} catch (IOException e) {
//			e.printStackTrace();
//			return 9;
//		}
//		return 9;
    }

    private static boolean cpFolder(File src, File tgt) {
        logger.info("cpFolder src="+src.getName()+" tgt="+tgt.getName());
        if (src.isDirectory()) {
            if (!tgt.exists()) {
                tgt.mkdir();
            }
            for (String file:src.list()) {
                cpFolder(new File(src,file),new File(tgt,file));
            }
        } else {
            if (src.isFile()) {
                try {
                    InputStream input = new FileInputStream(src);
                    OutputStream output = new FileOutputStream(tgt);

                    byte[] buffer = new byte[2097152];

                    int len;
                    while ((len = input.read(buffer)) > 0){
                        output.write(buffer, 0, len);
                    }

                    input.close();
                    output.close();

                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage(),e);
                    return false;
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                    return false;
                }

            }
        }
        return false;
    }

    public static boolean rmFolder(String name) {
        logger.info("rmFolder name="+name);
        File folder=new File(name);
        if (!folder.exists())		return true;
        if (!folder.isDirectory())	{
//			logger.info("rmFolder name "+name+" is not a folder");
            return false;
        }

        for (String file:folder.list()) {
            if (file.trim().equals("")) continue;


            File node=new File(name,file);
            if (node.isDirectory()) {
                if (!rmFolder(file)) return false;
            } else if (node.isFile()) {
                if (!node.delete()) return false;
            } else {
                return false;
            }
        }

//		logger.info("rmFolder delete");
        return folder.delete();

//		try {
//			File file = new File(name);
//			if (!file.exists()) return true;
//			Process p=Runtime.getRuntime().exec("rm -rf "+name);
//			p.waitFor();
//			if (p.exitValue()!=0) return false;
//		}
//		  catch(InterruptedException e2) {} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
    }
    public static ArrayList<String> fileList(String filePath, Logger logger) {
        BufferedReader source=null;
        try {
            source=new BufferedReader(new FileReader(filePath));
            ArrayList<String> fileList=new ArrayList<String>();
            String line;
            while((line=source.readLine()) != null){
                fileList.add(line);
            }
            source.close();
            return fileList;
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
            return null;
        }
    }

    public static String fileString(String filePath, Logger logger) {
        FileInputStream source=null;
        try {
            File file=new File(filePath);
            byte tmpstr[]=new byte[(int) file.length()];

            source = new FileInputStream(file);
            source.read(tmpstr);

            return new String(tmpstr);
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
            return null;
        } finally {
            if (source!=null) {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String fileString(InputStream inputStream, Logger logger) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder tmpstr = new StringBuilder();
            String line;
            long count=0;
            while ((line = reader.readLine()) != null) {
                if (++count>1) tmpstr.append("\n");
                tmpstr.append(line);
            }
            return tmpstr.toString();
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
            return null;
        } finally {
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static ArrayList<String> fileGrep(String filePath, String regex, Logger logger) {
        BufferedReader source=null;
        try {
            source=new BufferedReader(new FileReader(filePath));
            ArrayList<String> fileList=new ArrayList<String>();
            String line;
            while((line=source.readLine()) != null){
                if (line.matches(regex)) fileList.add(line);
            }
            source.close();
            return fileList;
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
            return null;
        }
    }

    public static boolean cpFile(String source, String target) {
//		logger.info("cpFile: source="+source+" target="+target);

        File src=new File(source);
        File tgt=new File(target);

        if (!src.isFile()) return false;
        if (!tgt.isDirectory()) return false;

        File dst=new File(target,src.getName());


        try {
            InputStream input = new FileInputStream(src);
            OutputStream output = new FileOutputStream(dst);

            dst.setExecutable(src.canExecute());

            byte[] buffer = new byte[2097152];

            int len;
            while ((len = input.read(buffer)) > 0){
                output.write(buffer, 0, len);
            }

            input.close();
            output.close();

            return true;

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    public static void overwrite(String output, String fileName, Logger logger) throws IOException {
        FileWriter fstream = new FileWriter(fileName, false);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(output);
        out.close();
        fstream.close();
    }
}
