package com.jaravir.tekila.ui.test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.log4j.Logger;

public class GenTest {
    private static final Logger log = Logger.getLogger(GenTest.class);
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Ignore
    @Test
    public void test() {
            String password = "back";
            try {
                    MessageDigest md = null;
                    md = MessageDigest.getInstance("SHA-256");
                    //md = MessageDigest.getInstance("MD5");
                    //md.update(password.getBytes("UTF-8"));
                    md.update(password.getBytes());
                    String res = new String(md.digest(), Charset.forName("UTF-8"));
                    //String res = Base64.encodeBase64String(md.digest());
                    //String res = Hex.encodeHexString(md.digest());
                    log.debug("Hex: " + res);
            } catch (NoSuchAlgorithmException e) {

                    e.printStackTrace();
            }
            /*catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
            }*/
    }

    @Ignore
    @Test
    public void mdTest() {
            /*String pass = "admin";
            try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(pass.getBytes());
                    BigInteger res = new BigInteger(1, md.digest());
                    log.debug("BigInteger: " + res.toString(16));
            }
            catch (Exception ex) {}
            */
    }

}
