package com.github.amihalik;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class Example {
    
    private static boolean printQuery = false;

    private static ValueFactory vf = SimpleValueFactory.getInstance();

    private static Statement a_def = createStatement("u:A", "u:t", "u:B");
    private static Statement b_def = createStatement("u:B", "u:w", "u:C");

    private static Statement a_g1 = createStatement("u:A", "u:t", "u:B", "u:G1");
    private static Statement b_g1 = createStatement("u:B", "u:w", "u:C", "u:G1");

    private static Statement a_g2 = createStatement("u:A", "u:t", "u:B", "u:G2");
    private static Statement b_g2 = createStatement("u:B", "u:w", "u:C", "u:G2");

    private static String q_1a = "SELECT ?a ?b WHERE { ?a <u:t> ?b }";
    private static String q_1b = "SELECT ?g ?a ?b WHERE { GRAPH ?g { ?a <u:t> ?b } }";
    private static String q_1c = "SELECT ?a ?b WHERE { GRAPH <u:G1> { ?a <u:t> ?b } }";

    private static String q_2a = "SELECT ?a ?b WHERE { ?a <u:t> ?b . ?b <u:w> ?c }";
    private static String q_2b = "SELECT ?g ?a ?b WHERE { GRAPH ?g { ?a <u:t> ?b . ?b <u:w> ?c } }";

    public static void main(String[] args) throws Exception {

        System.out.println("Single Statements");

        System.out.println("Pattern's Context State // Statement has a context value");
        countQueryResults("not mentioned // yes", q_1a, a_g1);
        countQueryResults("not mentioned // no", q_1a, a_def);

        countQueryResults("has a constant // yes (match)", q_1c, a_g1);
        countQueryResults("has a constant // yes (no match)", q_1c, a_g2);
        countQueryResults("has a constant // no", q_1c, a_def);

        countQueryResults("has a variable // yes", q_1b, a_g1);
        countQueryResults("has a has a variable // no", q_1b, a_def);

        System.out.println();
        System.out.println("Joins");
        countQueryResults("no context // default context", q_2a, a_def, b_def);
        countQueryResults("no context // default and named context", q_2a, a_def, b_g1);
        countQueryResults("no context // same named context", q_2a, a_g1, b_g1);
        countQueryResults("no context // different named context", q_2a, a_g1, b_g2);

        countQueryResults("variable context // default context", q_2b, a_def, b_def);
        countQueryResults("variable context // default and named context", q_2b, a_def, b_g1);
        countQueryResults("variable context // same named context", q_2b, a_g1, b_g1);
        countQueryResults("variable context // different named context", q_2b, a_g1, b_g2);

    }

    public static void countQueryResults(String name, String query, Statement... statements) {
        System.out.println(" ========= Query :: " + name + " ========= ");
        List<BindingSet> results = performQuery(query, statements);
        System.out.println("Result Count :: " + results.size());
        results.stream().forEach(System.out::println);
    }

    public static List<BindingSet> performQuery(String query, Statement... statements) {
        Repository r = createRepo();
        RepositoryConnection rc = r.getConnection();

        rc.add(Arrays.asList(statements));
        QueryResultCollector qrc = new QueryResultCollector();
        TupleQuery tq = rc.prepareTupleQuery(query);

        if (printQuery) {
            System.out.println(tq);
        }

        tq.evaluate(qrc);
        rc.close();
        r.shutDown();
        return qrc.getBindingSets();
    }

    public static Statement createStatement(String s, String p, String o) {
        IRI sub = vf.createIRI(s);
        IRI pre = vf.createIRI(p);
        IRI obj = vf.createIRI(o);
        return vf.createStatement(sub, pre, obj);
    }

    public static Statement createStatement(String s, String p, String o, String g) {
        IRI sub = vf.createIRI(s);
        IRI pre = vf.createIRI(p);
        IRI obj = vf.createIRI(o);
        IRI con = vf.createIRI(g);
        return vf.createStatement(sub, pre, obj, con);
    }

    public static Repository createRepo() {
        SailRepository r = new SailRepository(new MemoryStore());
        r.initialize();
        return r;
    }

}
