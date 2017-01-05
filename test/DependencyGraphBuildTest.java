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

/**
 *
 * @author martin
 */
public class DependencyGraphBuildTest {

    @Test
    public void testAskedProgram1() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram1()");
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

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("2");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals().size(), 0);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram2() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram2()");
        System.out.println("#1\n1:a :- 2:b\n#2\n2:b :- 3:c\n#3\n3:c :- 1:a");
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
        r.addToBody(new Constant("1:a"));

        p3.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Map<Literal, Set<String>> p1asked = new HashMap<>();
        p1asked.put(new Constant("1:a"), new HashSet<>());
        p1asked.get(new Constant("1:a")).add("3");

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("2");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals(), p1asked);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram3() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram3()");
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

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:d"), new HashSet<>());
        p3asked.get(new Constant("3:d")).add("1");
        p3asked.get(new Constant("3:d")).add("2");

        Map<Literal, Set<String>> p4asked = new HashMap<>();
        p4asked.put(new Constant("4:e"), new HashSet<>());
        p4asked.get(new Constant("4:e")).add("3");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(0, p1.getAskedLiterals().size());
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
        Assert.assertEquals(p4.getAskedLiterals(), p4asked);
    }

    @Test
    public void testAskedProgram4() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram4()");
        System.out.println("#1\n1:a :- 2:b\n1:c :- 3:d\n#2\n2:b :- 3:d\n#3\n3:d :- 4:e\n#4\n4:e :-");
        System.out.println("initprogram: 3");
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

        router.sendMessage(p3.getLabel(), new InitMessage());
        executor.shutdown();

        Map<Literal, Set<String>> p4asked = new HashMap<>();
        p4asked.put(new Constant("4:e"), new HashSet<>());
        p4asked.get(new Constant("4:e")).add("3");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(0, p1.getAskedLiterals().size());
        Assert.assertEquals(0, p2.getAskedLiterals().size());
        Assert.assertEquals(0, p3.getAskedLiterals().size());
        Assert.assertEquals(p4.getAskedLiterals(), p4asked);
    }

    @Test
    public void testAskedProgram5() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram5()");
        System.out.println("#1\n1:a :- 2:b\n#2\n2:b :- 3:c, 1:a\n#3\n3:c :- 2:b");
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
        r.addToBody(new Constant("1:a"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));
        r.addToBody(new Constant("2:b"));

        p3.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Map<Literal, Set<String>> p1asked = new HashMap<>();
        p1asked.put(new Constant("1:a"), new HashSet<>());
        p1asked.get(new Constant("1:a")).add("2");

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");
        p2asked.get(new Constant("2:b")).add("3");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("2");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals(), p1asked);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram6() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram6()");
        System.out.println("#1\n1:a :- 2:b\n#2\n2:b :- 3:c, 1:a\n#3\n3:c :- 2:b");
        System.out.println("initprogram: 2");
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
        r.addToBody(new Constant("1:a"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));
        r.addToBody(new Constant("2:b"));

        p3.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Map<Literal, Set<String>> p1asked = new HashMap<>();
        p1asked.put(new Constant("1:a"), new HashSet<>());
        p1asked.get(new Constant("1:a")).add("2");

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");
        p2asked.get(new Constant("2:b")).add("3");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("2");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals(), p1asked);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram7() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram7()");
        System.out.println("#1\n1:a :- 2:b, 3:c\n#2\n2:b :- 1:a, 3:c\n#3\n3:c :- 1:a, 2:b");
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
        r.addToBody(new Constant("3:c"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("2:b"));
        r.addToBody(new Constant("1:a"));
        r.addToBody(new Constant("3:c"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));
        r.addToBody(new Constant("1:a"));
        r.addToBody(new Constant("2:b"));

        p3.addRule(r);

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        Map<Literal, Set<String>> p1asked = new HashMap<>();
        p1asked.put(new Constant("1:a"), new HashSet<>());
        p1asked.get(new Constant("1:a")).add("2");
        p1asked.get(new Constant("1:a")).add("3");

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:b"), new HashSet<>());
        p2asked.get(new Constant("2:b")).add("1");
        p2asked.get(new Constant("2:b")).add("3");

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("1");
        p3asked.get(new Constant("3:c")).add("2");

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals(), p1asked);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram8() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram8()");
        System.out.println("#1\n1:a :- 1:b\n1:b :-\n#2\n2:b :- 3:c\n#3\n3:c :- ");
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
        r.addToBody(new Constant("1:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("1:b"));

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

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals().size(), 0);
        Assert.assertEquals(p2.getAskedLiterals().size(), 0);
        Assert.assertEquals(p3.getAskedLiterals().size(), 0);
    }

    @Test
    public void testAskedProgram9() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram9()");
        System.out.println("#1\n1:a :- 1:b\n1:b :-\n#2\n2:b :- 3:c\n#3\n3:c :- ");
        System.out.println("initprogram: 2");
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
        r.addToBody(new Constant("1:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("1:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("2:b"));
        r.addToBody(new Constant("3:c"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));

        p3.addRule(r);

        Map<Literal, Set<String>> p3asked = new HashMap<>();
        p3asked.put(new Constant("3:c"), new HashSet<>());
        p3asked.get(new Constant("3:c")).add("2");

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);

        router.sendMessage(p2.getLabel(), new InitMessage());
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals().size(), 0);
        Assert.assertEquals(p2.getAskedLiterals().size(), 0);
        Assert.assertEquals(p3.getAskedLiterals(), p3asked);
    }

    @Test
    public void testAskedProgram10() {
        System.out.println("--------------------------------------------");
        System.out.println("DependencyGraphBuildTest.testAskedProgram10()");
        System.out.println("#1\n1:a :- 1:b\n1:b :- 2:a\n#2\n2:a :- \n#3\n3:c :- 4:d\n#4\n4:d :- ");
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
        r.addToBody(new Constant("1:b"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("1:b"));
        r.addToBody(new Constant("2:a"));

        p1.addRule(r);

        r = new Rule();
        r.setHead(new Constant("2:a"));

        p2.addRule(r);

        r = new Rule();
        r.setHead(new Constant("3:c"));
        r.addToBody(new Constant("4:d"));

        p3.addRule(r);

        r = new Rule();
        r.setHead(new Constant("4:d"));

        p4.addRule(r);

        Map<Literal, Set<String>> p2asked = new HashMap<>();
        p2asked.put(new Constant("2:a"), new HashSet<>());
        p2asked.get(new Constant("2:a")).add("1");

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(p1);
        executor.execute(p2);
        executor.execute(p3);
        executor.execute(p4);

        router.sendMessage(p1.getLabel(), new InitMessage());
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(p1.getAskedLiterals().size(), 0);
        Assert.assertEquals(p2.getAskedLiterals(), p2asked);
        Assert.assertEquals(p3.getAskedLiterals().size(), 0);
        Assert.assertEquals(p4.getAskedLiterals().size(), 0);
    }
}
