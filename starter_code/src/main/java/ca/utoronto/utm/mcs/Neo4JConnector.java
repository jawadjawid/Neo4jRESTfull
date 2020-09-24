package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.List;

public class Neo4JConnector {

    private Driver driver;
    private String uriDb;

    public Neo4JConnector() {
        uriDb = "bolt://localhost:11002";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","secret"));
    }

    public void insertActor(String name, String actorId) throws Exception{
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result result = tx.run("MATCH (n {name: $x}) RETURN n"
                        , parameters("x", name ) );
                if (result.list().size() == 0){
                    tx.run("MERGE (a:Actor {name: $x, actorId: $y})",
                            parameters("x", name, "y", actorId));
                    tx.commit();
                    session.close();
                }
            }
        }catch (Exception e){
            throw e;
        }
    }

    public void close() {
        driver.close();
    }
}

