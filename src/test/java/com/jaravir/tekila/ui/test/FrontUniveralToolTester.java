package com.jaravir.tekila.ui.test;


import com.jaravir.tekila.ui.util.FrontUniversalTool;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FrontUniveralToolTester {

    private static final Logger log= Logger.getLogger(FrontUniveralToolTester.class);


    @Test
    public void applyPatternTest(){

        assert(FrontUniversalTool.applyPattern("546-56-56")).equals("5465656");
        log.debug(FrontUniversalTool.applyPattern("546-56,-56"));

    }

}
