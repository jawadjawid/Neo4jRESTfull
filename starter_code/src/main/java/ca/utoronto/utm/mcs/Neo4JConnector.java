package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;

import ca.utoronto.utm.mcs.exceptions.BadRequestException;
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

    public void addActor(String name, String actorId) throws Exception, BadRequestException {
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result nameResult = tx.run("MATCH (n {name: $x}) RETURN n"
                        , parameters("x", name ) );
                Result actorIdResult = tx.run("MATCH (n {actorId: $x}) RETURN n"
                        , parameters("x", actorId ) );
                if (nameResult.list().size() == 0 && actorIdResult.list().size() == 0){
                    tx.run("MERGE (a:Actor {name: $x, actorId: $y})",
                            parameters("x", name, "y", actorId));
                    tx.commit();
                    session.close();
                } else{
                    throw new BadRequestException();
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

