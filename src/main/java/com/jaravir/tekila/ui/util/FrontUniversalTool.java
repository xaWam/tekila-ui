package com.jaravir.tekila.ui.util;


import org.apache.log4j.Logger;

/**
 *
 * @author musaaliyev
 *
 */
public class FrontUniversalTool {

    private static final Logger log=Logger.getLogger(FrontUniversalTool.class);

    public static String applyPattern(String str){
        return str.replaceAll("[-+.^:,]","");
    }
}
