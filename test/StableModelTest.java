
import core.Constant;
import core.Literal;
import core.Program;
import core.Router;
import core.Rule;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import messages.InitMessage;
import org.junit.Assert;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author martin
 */
public class StableModelTest {

    @Test
    public void testAskedProgram1() {
        System.out.println("StableModelTest.testAskedProgram1()");
        System.out.println("#1\n1:a :- 2:b\n#2\n2:b :- 3:c\n#3\n3:c :-");
        System.out.println("initprogram: 1");
        System.out.println("--------------------------------------------");

        Router router = new Router();

        Program p1 = new Program("1", router);
        Program p2 = new Program("2", router);
        Program p3 = new Program("3", router);

        router.addProgram(p1);
        router.addProgram(p2);
        router.addProgram(p3);

        Rule r = new Rule();
        r.setHead(new Constant("1:a"));
        r.addToBody(new Constant("2:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("2:b"));
        r.addToBody(new Constant("3:c"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));

        p3.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Set<Literal> p1model = new HashSet<>();
        p1model.add(new Constant("1:a"));
        p1model.add(new Constant("2:b"));

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getSmallestModel(), p1model);
    }

    @Test
    public void testAskedProgram2() {
        System.out.println("DependencyGraphBuildTest.testAskedProgram2()");
        System.out.println("#1\n1:a :- 2:b\n1:c :- 3:d\n#2\n2:b :- 3:d\n#3\n3:d :- 4:e\n#4\n4:e :-");
        System.out.println("initprogram: 1");
        System.out.println("--------------------------------------------");

        Router router = new Router();

        Program p1 = new Program("1", router);
        Program p2 = new Program("2", router);
        Program p3 = new Program("3", router);
        Program p4 = new Program("4", router);

        router.addProgram(p1);
        router.addProgram(p2);
        router.addProgram(p3);
        router.addProgram(p4);

        Rule r = new Rule();
        r.setHead(new Constant("1:a"));
        r.addToBody(new Constant("2:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("1:c"));
        r.addToBody(new Constant("3:d"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("2:b"));
        r.addToBody(new Constant("3:d"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:d"));
        r.addToBody(new Constant("4:e"));

        p3.addRule(r);

        r = new Rule();
        r.setHead(new Constant("4:e"));
        p4.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);
        executor.execute(p4);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Set<Literal> p1model = new HashSet<>();
        p1model.add(new Constant("1:a"));
        p1model.add(new Constant("1:c"));
        p1model.add(new Constant("2:b"));
        p1model.add(new Constant("3:d"));

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getSmallestModel(), p1model);
    }
}
