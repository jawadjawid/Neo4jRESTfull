package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;

import ca.utoronto.utm.mcs.exceptions.BadRequestException;
import ca.utoronto.utm.mcs.exceptions.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.List;

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
                        , parameters("x", actorId ));
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
    
    public String getActor(String actorId) throws BadRequestException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){
    			Result actorNameResult = tx.run("MATCH (n:actor {id: $x}) RETURN n.Name as name", parameters("x", actorId));
    			List<Record> actorNameRecords = actorNameResult.list();
    			if(actorNameRecords.size() == 0)
    				throw new NotFoundException();
    			
    			Result movieIdsResult = tx.run("MATCH (:actor {id: $x})-[:ACTED_IN]-(m:movie) RETURN collect(m.id) as movies", 
    					parameters("x", actorId));
    			List<Record> movieIdsRecords = movieIdsResult.list();
    			
    			String actorName = actorNameRecords.get(0).get("name").asString();
    			List<Object> movieIds = movieIdsRecords.get(0).get("movies").asList();
                tx.commit();
                session.close();

                JSONObject json = new JSONObject();
                json.put("actorId", actorId);
                json.put("name", actorName);
                JSONArray array = new JSONArray(movieIds);
                json.put("movies", array);
    			return json.toString();
    		}
    	} catch (Exception e){
            throw e;
        }
    }

    public void addMovie(String name, String movieId) throws Exception{
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result nameResult = tx.run("MATCH (n:movie {Name: $x}) RETURN n"
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
    
    public String getMovie(String movieId) throws BadRequestException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){
    			Result movieNameResult = tx.run("MATCH (m:movie {id: $x}) RETURN m.Name as name"
    					, parameters("x", movieId));
    			List<Record> movieNameRecords = movieNameResult.list();
    			if(movieNameRecords.size() == 0)
    				throw new NotFoundException();
    			
    			Result actorIdsResult = tx.run("MATCH (m:movie {id: $x})<-[:ACTED_IN]-(n:actor) RETURN collect(n.id) as actors", 
    					parameters("x", movieId));
    			List<Record> actorIdsRecords = actorIdsResult.list();
    			
    			String movieName = movieNameRecords.get(0).get("name").asString();
    			List<Object> actorIds = actorIdsRecords.get(0).get("actors").asList();
                tx.commit();
                session.close();

                JSONObject json = new JSONObject();
                json.put("movieId", movieId);
                json.put("name", movieName);
                JSONArray array = new JSONArray(actorIds);
                json.put("actors", array);
    			return json.toString();
    		}
    	} catch (Exception e){
            throw e;
        }
    }

    public void addRelationship(String actorId, String movieId) throws Exception{
        try (Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()) {
                Result result = tx.run("MATCH (a:actor {id: $x}),(m:movie {id: $y}) return a, m"
                        , parameters("x", actorId, "y", movieId) );
                if (result.hasNext()){
                    Result node_boolean = tx.run("RETURN EXISTS( (:actor {id: $x})"
                                    + "-[:ACTED_IN]-(:movie {id: $y}) ) as bool"
                            ,parameters("x", actorId, "y", movieId) );
                    if ((node_boolean.next().values().toArray()[0].toString() == "FALSE")) {
                        tx.run("MATCH (a:actor {id: $x}),(m:movie {id: $y})\n" +
                                        "MERGE (a)-[r:ACTED_IN]->(m)",
                                parameters("x", actorId, "y", movieId));
                        tx.commit();
                        session.close();
                    } else {
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
    
    public String hasRelationship(String actorId, String movieId) throws BadRequestException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){	
    			Result result = tx.run("MATCH (n:actor {id: $x}), (m:movie {id: $y})"
    					+ "RETURN n.id, m.id", parameters("x", actorId, "y", movieId));
    			if(!result.hasNext())
    				throw new NotFoundException();
    			
    			Result hasRelationshipResult = tx.run("RETURN EXISTS((:actor {id: $x})-[:ACTED_IN]-(:movie {id: $y})) as bool", parameters("x", actorId, "y", movieId));
    			boolean bool = hasRelationshipResult.list().get(0).get("bool").asBoolean();
                tx.commit();
                session.close();

                JSONObject json = new JSONObject();
                json.put("actorId", actorId);
                json.put("movieId", movieId);
                json.put("hasRelationship", bool);
    			return json.toString();
    		}
    	} catch (Exception e){
            throw e;
        }
    }
    
    public String computeBaconNumber(String actorId) throws BadRequestException, NotFoundException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){
                String baconId = "nm0000102";
    		    if (actorId.equals(baconId)){
                    JSONObject json = new JSONObject();
                    json.put("baconNumber", "0");
                    return json.toString();
                }

                Result actorResult = tx.run("MATCH (n:actor {id: $x}) RETURN n"
                        , parameters("x", actorId ));
    			if(!actorResult.hasNext())
    				throw new BadRequestException();

    			Result baconResult = tx.run("MATCH p=shortestPath((:actor {id: $x})-[*]-(:actor {id: $y})) RETURN length(p)/2 as baconNumber", parameters("x", actorId, "y", baconId));
    			if(!baconResult.hasNext())
    				throw new NotFoundException();
    			
    			String baconNumber = Integer.toString(baconResult.list().get(0).get("baconNumber").asInt());
                session.close();

                JSONObject json = new JSONObject();
                json.put("baconNumber", baconNumber);
    			return json.toString();
    		}
    	} catch (Exception e){
            throw e;
        }
    }
    
    public String computeBaconPath(String actorId) throws BadRequestException, NotFoundException, Exception {
    	try (Session session = driver.session()){
    		try(Transaction tx = session.beginTransaction()){	
    			Result actorResult = tx.run("MATCH (n:actor {id: $x}), (m:actor {Name: $y}) RETURN m.id as baconId", parameters("x", actorId, "y", "Kevin Bacon"));
    			
    			if(!actorResult.hasNext())
    				throw new BadRequestException();
    			
    			String baconId = actorResult.list().get(0).get("baconId").asString();
    			if(actorId.equals(baconId)) {
    				Result pathResult = tx.run("MATCH (:actor {id: $x})-[:ACTED_IN]-(m:movie) RETURN m.id as movieId", parameters("x", actorId));
    				String movieId = pathResult.list().get(0).get("movieId").asString();
    				
    				JSONObject pathJson = new JSONObject();
    				pathJson.put("actorId", actorId);
    				pathJson.put("movieId", movieId);
    				
    				JSONArray pathArray = new JSONArray(pathJson);
    				
    				JSONObject json = new JSONObject();
                    json.put("baconNumber", 0);
                    json.put("baconPath", pathArray);
        			return json.toString();
    			}
    				
    			Result baconResult = tx.run("MATCH p=shortestPath((:actor {id: $x})-[*]-(:actor {id: $y})) RETURN length(p)/2 as baconNumber, p as baconPath", parameters("x", actorId, "y", "nm0000102"));
    			if(!baconResult.hasNext())
    				throw new NotFoundException();
    			
    			List<Record> baconRecords = baconResult.list();
    			
    			int baconNumber = baconRecords.get(0).get("baconNumber").asInt();
    			Iterable<Node> pathNodes = baconRecords.get(0).get("baconPath").asPath().nodes();
    			
    			List<String> ids = new ArrayList<String>();
    			
    			for(Node node : pathNodes)
    				ids.add(node.get("id").asString());
    			
    			JSONArray pathArray = new JSONArray();
    			boolean flip = false;
    			
    			for(int i = 0; i < ids.size() - 1; i++) {
    				String actor, movie;
    				if(!flip) {
    					actor = ids.get(i);
    					movie = ids.get(i+1);
    				} else {
    					actor = ids.get(i+1);
    					movie = ids.get(i);
    				}
    				flip = !flip;
    				JSONObject pathSeg = new JSONObject();
    				pathSeg.put("actorId", actor);
    				pathSeg.put("movieId", movie);
    				pathArray.put(pathSeg);
    			}
    			
    			JSONObject json = new JSONObject();
    			json.put("baconNumber", baconNumber);
    			json.put("baconPath", pathArray);
    			
                tx.commit();
                session.close();
    			return json.toString();
    		}
    	} catch (Exception e){
            throw e;
        }
    }
    
    public void close() {
        driver.close();
    }
}

