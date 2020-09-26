package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;

import ca.utoronto.utm.mcs.exceptions.BadRequestException;
import ca.utoronto.utm.mcs.exceptions.NotFoundException;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.summary.ResultSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4JConnector {

    private Driver driver;
    private String uriDb;

    public Neo4JConnector() {
        uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","pass"));
    }

    public void addActor(String name, String actorId) throws Exception, BadRequestException {
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result nameResult = tx.run("MATCH (n:actor {Name: $x}) RETURN n"
                        , parameters("x", name ) );
                Result actorIdResult = tx.run("MATCH (n:actor {id: $x}) RETURN n"
                        , parameters("x", actorId ) );
                if (nameResult.list().size() == 0 && actorIdResult.list().size() == 0){
                    tx.run("MERGE (a:actor {Name: $x, id: $y})",
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
    
    public List<String> getActor(String actorId) throws BadRequestException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){
    			
    			Result actorNameResult = tx.run("MATCH (n:actor {id: $x}) RETURN n.Name as name", parameters("x", actorId));
    			List<Record> actorNameRecords = actorNameResult.list();
    			if(actorNameRecords.size() == 0)
    				throw new BadRequestException();
    			
    			Result movieIdsResult = tx.run("MATCH (:actor {id: $x})-[:ACTED_IN]-(m:movie) RETURN collect(m.id) as movies", parameters("x", actorId));
    			List<Record> movieIdsRecords = movieIdsResult.list();
    			
    			String actorName = actorNameRecords.get(0).get("name").toString();
    			String movieIds = movieIdsRecords.get(0).get("movies").toString();
    			
    			List<String> actorData = new ArrayList<String>();
    			actorData.add(actorName);
    			actorData.add(movieIds);
    			tx.commit();
    			session.close();
    			return actorData;
    		}
    	} catch (Exception e){
    		System.out.println("sharmouta");
            throw e;
        }
    }

    public void addMovie(String name, String movieId) throws Exception{
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result nameResult = tx.run("MATCH (n:movie {name: $x}) RETURN n"
                        , parameters("x", name ) );
                Result movieIdResult = tx.run("MATCH (n:movie {id: $x}) RETURN n"
                        , parameters("x", movieId ) );
                if (nameResult.list().size() == 0 && movieIdResult.list().size() == 0){
                    tx.run("MERGE (a:movie {Name: $x, id: $y})",
                            parameters("x", name, "y", movieId));
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

    public void addRelationship(String actorId, String movieId) throws Exception{
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result result = tx.run("MATCH (a:actor {id: $x}),(m:movie {id: $y}) return a, m"
                        , parameters("x", actorId, "y", movieId) );
                if (result.list().size() == 1){
                    Result node_boolean = tx.run("RETURN EXISTS( (:actor {id: $x})"
                                    + "-[:ACTED_IN]-(:movie {id: $y}) ) as bool"
                            ,parameters("x", actorId, "y", movieId) );
                    if ((node_boolean.next().values().toArray()[0].toString() == "FALSE")) {
                        tx.run("MATCH (a:actor {id: $x}),(m:movie {id: $y})\n" +
                                        "MERGE (a)-[r:ACTED_IN]->(m)",
                                parameters("x", actorId, "y", movieId));
                        tx.commit();
                        session.close();
                    }
                    else{
                        throw new BadRequestException();
                    }
                } else{
                    throw new NotFoundException();
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

