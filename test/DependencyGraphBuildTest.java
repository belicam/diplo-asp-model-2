/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import core.Constant;
import core.Program;
import core.Router;
import core.Rule;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import messages.InitMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martin
 */
public class DependencyGraphBuildTest {

    public DependencyGraphBuildTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testBuildGraph1() {
        Router router = new Router();
        
        Program p1 = new Program("1", router);
        Program p2 = new Program("2", router);
        Program p3 = new Program("3", router);
        
        router.addProgram(p1);
        router.addProgram(p2);
        router.addProgram(p3);
        
        Rule r1 = new Rule();
        r1.setHead(new Constant("1:a"));
        r1.addToBody(new Constant("2:b"));

        p1.addRule(r1);
        
        Rule r2 = new Rule();
        r2.setHead(new Constant("2:b"));
        r2.addToBody(new Constant("3:c"));

        p2.addRule(r2);
        
        Rule r3 = new Rule();
        r3.setHead(new Constant("3:c"));
        
        p3.addRule(r3);
                
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);
        
        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();
    }
}
