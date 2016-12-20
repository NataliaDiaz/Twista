package daw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.abdera.i18n.templates.Template;
import org.linkedopenservices.json2rdf.JSON2RDF;
import org.ontoware.rdf2go.RDF2Go;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class Lift {
	String basetw = "http://localhost:8080/twittersentiment-0.0.1-SNAPSHOT/rs/twista/";
	String basese = "http://localhost:8080/twittersentiment-0.0.1-SNAPSHOT/rs/emotions#";
	Model store = ModelFactory.createDefaultModel();

	public Set<String> getSentimentAtDateByKeyword(String keyword){

		Set<String> res = new HashSet<String>();
		Query getInfo= QueryFactory.create("" +
				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
				"PREFIX twista: <"+basetw+"> " +
				"PREFIX jsr: <http://www.linkedopenservices.org/ns/temp-json#> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX sent: <"+basese+"> " +
				"SELECT ?date ?sent " +
				"WHERE{ " +
				"	?x a sioc:Post ;" +
				"	twista:foundbykeyword \""+keyword+"\" ; " +
				"	dcterms:created ?date . " +
				"	?st sent:foundIn ?x ;" +
				"	a ?sent. } ");
		QueryExecution qexec = QueryExecutionFactory.create(getInfo, store) ;
		ResultSet resSet = qexec.execSelect();

		Map<String, Map<String, Integer>> h = new HashMap<String, Map<String, Integer>>();
		while(resSet.hasNext()){
			QuerySolution sol = resSet.next();
			RDFNode date = sol.get("?date");
			RDFNode sent = sol.get("?sent");

			//System.out.println(sent.toString());
			Map<String, Integer> prevValue = h.get(date.toString());
			prevValue = prevValue == null ? new HashMap<String, Integer>() : prevValue;
			Integer prevIntValue = prevValue.get(sent.toString());
			prevValue.put(sent.toString(), prevIntValue == null ? 1 : ++prevIntValue);
			h.put(date.toString(), prevValue);
		}
		Iterator<String> itr =  h.keySet().iterator();

		while(itr.hasNext()){

			String key = itr.next();

			String line = key+",";
			Map<String, Integer> inner = h.get(key);
			int pos = inner.get(basese+"PositiveEmotion") == null ? 0 : inner.get(basese+"PositiveEmotion");
			int neg = inner.get(basese+"NegativeEmotion") == null ? 0 : inner.get(basese+"NegativeEmotion");
			int neu = inner.get(basese+"NeutralEmotion") == null ? 0 : inner.get(basese+"NeutralEmotion");
			line += pos+","+neg+","+neu;
			res.add(line);
		}

		return res;
	}

	public Set<String> getPicturesByKeyword(String keyword){
		Set<String> res = new HashSet<String>();


//		Query getInfo= QueryFactory.create("" +
//				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
//				"PREFIX twista: <"+basetw+"> " +
//				"PREFIX jsr: <http://www.linkedopenservices.org/ns/temp-json#> " +
//				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
//				"PREFIX sent: <"+basese+"> " +
//				"SELECT ?topic " +
//				"WHERE{ " +
//				"	?x a sioc:Post ;" +
//				"	twista:foundbykeyword \""+keyword+"\" ; " +
//				"	sioc:topic ?q ; " +
//				"	?q rdfs:label ?topic . }");
//		QueryExecution qexec = QueryExecutionFactory.create(getInfo, store) ;
//		ResultSet resSet = qexec.execSelect();
//
//		Map<String, Integer> h = new HashMap<String, Integer>();
//		while(resSet.hasNext()){
//			QuerySolution sol = resSet.next();
//			RDFNode topic = sol.get("?topic");
//
//			Integer prevValue = h.get(topic.toString());
//			prevValue = prevValue == null ? 0 : prevValue;
//			h.put(topic.toString(), ++prevValue);
//		}
//
//		Map<String, Integer> map = MapUtils.sortMapByValue(h);
//		int i = 0;
//		for(Entry<String, Integer> ent : map.entrySet()){
//			i+=1;
//			String top = ent.getKey();

			String query = "" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
			"SELECT ?x " +
			"WHERE { " +
			"	?y rdfs:label \""+keyword+"\"@en . " +
			"	?y foaf:depiction ?x ." +
			"} ";

			Client client = Client.create();

			MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			formData.add("query", query);
			WebResource webResource = client.resource("http://dbpedia.org/sparql");
			ClientResponse response = webResource.post(ClientResponse.class, formData);
			String resp = response.getEntity(String.class);
			if(resp.contains("<td>")){
				String picf = resp.split("<td>")[1].split("</td>")[0];
				res.add(picf);
			}


//			if(i==10){
//				continue;
//			}
//		}


		return res;
	}



	public void fire (String json){
		try {
			RDF2Go.register( new org.ontoware.rdf2go.impl.jena26.ModelFactoryImpl() );
			Model test = ModelFactory.createDefaultModel();
			JSON2RDF myTransformer = new JSON2RDF(false);
			Map<String, Template> map = new HashMap<String, Template>();
			org.ontoware.rdf2go.model.Model temp = myTransformer.getJSON2RDF(json, map);

			Model model = (Model) temp.getUnderlyingModelImplementation();
//                        OutputStream out1 = new ByteArrayOutputStream();
//			model.write(out1, "N3");
//                        System.out.println(out1.toString());
			Query getInfo= QueryFactory.create("" +
					"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
					"PREFIX twista: <"+basetw+"> " +
					"PREFIX jsr: <http://www.linkedopenservices.org/ns/temp-json#> " +
					"PREFIX dcterms: <http://purl.org/dc/terms/> " +
					"CONSTRUCT {?p a sioc:Post ;" +
					"			sioc:creator ?creator ; " +
					"			sioc:content ?content ;" +
					"			dcterms:created ?date ;" +
					"			twista:foundbykeyword ?key . " +
					"			?creator twista:num_followers ?follownumber ; " +
					"   		twista:num_followedby ?followedBy . } " +
					"WHERE{ " +
					"	?x jsr:content ?content ;" +
					"	jsr:keyword ?key ; " +
					"	jsr:date ?date ; " +
					"	jsr:followers ?follownumber ; " +
					"	jsr:friends ?followedBy ; " +
					"	jsr:id ?id ; " +
					"	jsr:user ?user ." +
					"   BIND(IRI(CONCAT(\"http://twitter.com/#!/\", ?user)) AS ?creator)" +
					"   BIND(IRI(CONCAT(\"http://twitter.com/#!/\", ?user, \"/status/\", ?id)) AS ?p) } ");
			QueryExecution qexec = QueryExecutionFactory.create(getInfo, model) ;
			Model lifted = qexec.execConstruct();

			//add input data to store
			store.add(lifted);

			Client client = Client.create();
			OutputStream out = new ByteArrayOutputStream();
			lifted.write(out, null);

			//System.out.println(out.toString());

			MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			formData.add("input", out.toString());
			WebResource webResource = client.resource("http://localhost:8080/twittersentiment-0.0.1-SNAPSHOT/rs/twista/getSentiment");
			ClientResponse response = webResource.post(ClientResponse.class, formData);

			//add outputdata to store
			Model analyseData = ModelFactory.createDefaultModel();
			String str = response.getEntity(String.class) ;
			System.out.println(str);
			analyseData.read(new StringReader(str) , null , null);
			store.add(analyseData);
			System.out.println(out.toString());

//			Query nfo= QueryFactory.create("" +
//					"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
//					"PREFIX twista: <"+basetw+"> " +
//					"PREFIX jsr: <http://www.linkedopenservices.org/ns/temp-json#> " +
//					"PREFIX dcterms: <http://purl.org/dc/terms/> " +
//					"PREFIX sent: <"+basese+"> " +
//					"SELECT ?f ?k ?j " +
//					"WHERE{ " +
//					"	?x a sioc:Post . " +
//					"	?x sioc:content ?f . " +
//					"	?x sioc:creator ?p . " +
//					"	?p twista:num_followers ?k . " +
//					"	?p twista:num_followedby ?j . " +
//					"	} ");
//			QueryExecution qexe = QueryExecutionFactory.create(nfo, store) ;
//			ResultSet resSet = qexe.execSelect();
//
//			while(resSet.hasNext()){
//				QuerySolution sol = resSet.next();
//				RDFNode date = sol.get("?f");
//				RDFNode d = sol.get("?k");
//				RDFNode da = sol.get("?j");
//				System.out.println(date.toString()+" "+d.toString()+" "+da.toString());
//			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main (String arg[]){
//		Lift li = new Lift();
//		li.fire("");
//		for(String s : li.getSentimentAtDateByKeyword("Obama")){
//			System.out.println(s);
//		}
	}
}
