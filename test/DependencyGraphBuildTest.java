/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import core.Constant;
import core.Literal;
import core.Program;
import core.Router;
import core.Rule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import messages.InitMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author martin
 */
public class DependencyGraphBuildTest {

    static Router router;
    static Program p1;
    static Program p2;
    static Program p3;

    public DependencyGraphBuildTest() {
    }

    @BeforeClass
    public static void setUpClass() throws InterruptedException {
        router = new Router();

        p1 = new Program("1", router);
        p2 = new Program("2", router);
        p3 = new Program("3", router);

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

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            
        }
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

    @Test
    public void testAskedFromProgram1() {
        Assert.assertEquals(p1.getAskedLiterals().size(), 0);
    }

    @Test
    public void testAskedFromProgram2() {
        Map<Literal, List<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new ArrayList<>());
        p2asked.get(new Constant("2:b")).add("1");

        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
    }

    @Test
    public void testAskedFromProgram3() {
        Map<Literal, List<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new ArrayList<>());
        p3asked.get(new Constant("3:c")).add("2");

        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }
}
