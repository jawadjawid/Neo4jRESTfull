package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Neo4JConnector {

    private Driver driver;
    private String uriDb;

    public Neo4JConnector() {
        uriDb = "bolt://localhost:11002";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","secret"));
    }

    public void insertActor(String name, String actorId) {
        try (Session session = driver.session()){
            session.writeTransaction(tx -> tx.run("MERGE (a:Actor {name: $x, actorId: $y})",
                    parameters("x", name, "y", actorId)));
            session.close();
        }
    }

    public void printBook(String author, String title)
    {
        try (Session session = driver.session())
        {
            try (Transaction tx = session.beginTransaction()) {
                Result node_boolean = tx.run("RETURN EXISTS( (:Author {author: $x})"
                                + "-[:WROTE]-(:Title {title: $y}) ) as bool"
                        ,parameters("x", author, "y", title) );
                if (node_boolean.hasNext()) {
                    System.out.println(author + " wrote " + title);
                }
            }
        }
    }

    public void close() {
        driver.close();
    }
}

