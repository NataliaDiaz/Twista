package org.linkedservices.twittersentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.i18n.templates.Template;
import org.apache.commons.httpclient.HttpException;
import org.linkedopenservices.json2rdf.JSON2RDF;
import org.linkedopenservices.json2rdf.template.TemplateUtil;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;



@Path("/twista" )
public class Twista {
//	TwitterClassifier classi = new TwitterClassifier();
	
//	@Path( "/findPictures" ) @POST @Produces( "text/html" )
//   public String movieService( @Context UriInfo uriInfo ){}
	
	@Path("/getSentiment") @GET @Produces( "text/html" )
	public String test(@Context UriInfo uriInfo ){
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/twista/";
		return null;
	}
	
	@Path( "/getSentiment" ) @POST @Produces( "application/rdf+xml" )
	public String findPicturesByPOST(@FormParam("input") String input, @Context UriInfo uriInfo ) throws IOException, HttpException{
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/twista/";
		return input;
	}
	
	@Path( "/getSentiment" ) @POST @Produces( "text/N3" )
	public Response retrieveN3InfoByPOST(@FormParam("input") String input, @Context UriInfo uriInfo ) throws IOException, HttpException{
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/twista/";
		
		return Response.ok().build(); 
	}
	
	
	
	public Model analyse(Model model, String base) throws IOException, HttpException{
		
		Query getInfo= QueryFactory.create("" +
				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
				"PREFIX twista: <"+base+"> " +
				"SELECT ?p ?creator ?follownumber ?content " +
				"WHERE{ " +
				"	?p a sioc:Post ; " +
				"	sioc:creator ?creator ; " +
				"	sioc:content ?content . " +
				"	?creator twista:num_followers ?follownumber ; " +
				"   twista:num_followedby ?followedBy . } ");
		QueryExecution qexec = QueryExecutionFactory.create(getInfo, model) ;
		ResultSet data = qexec.execSelect();
		
		Model out = ModelFactory.createDefaultModel();
		
		while(data.hasNext()){
			QuerySolution sol = data.next();
			RDFNode p = sol.get("?p");
			RDFNode creator = sol.get("?crator");
			RDFNode followersNumber = sol.get("?follownumber");
			RDFNode followedbyNumber = sol.get("?followedBy");
			RDFNode content = sol.get("?content");
//			
//			List<String> analyResult = classi.analyseSentiment( content.toString(), followersNumber.toString(), followedbyNumber.toString() );
//			
//			Iterator<String> itr = analyResult.iterator();
//			
//			String sentiment = itr.next();
//			String sentiGraph = "" +
//					"_:emotion a "+sentiment+" ; " +
//					"sent:foundIn "+p.toString()+" . " +
//					sentiment+" rdfs:subClassOf sent:Sentiment . ";
//			int i =0;
//			while(itr.hasNext()){
//				sentiGraph += ""+
//					p.toString()+" sioc:topic _:d"+i+" . " +
//					"_:d"+i+" rdfs:label \""+itr.next()+"\" . ";
//			}
//			Model interim = ModelFactory.createDefaultModel();
//			interim.read(new StringReader(sentiGraph), null , "N3");
//			out.add(interim);
		}
		
		return out;
		
	}
	
	

	private static String getQuery(String file) {
		StringBuffer query = new StringBuffer();
		try {
			ClassLoader loader = TemplateUtil.class.getClassLoader();
			InputStream inputStream = loader.getResourceAsStream(file);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					inputStream));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					query.append(line+"\n");
				}
			} finally {
				input.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
		return query.toString();
	}
	
	@GET
	@Path( "/num_followers" ) @Produces("application/rdf+xml")
	public Response followers(@Context UriInfo uriInfo){
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/twista/";
		String smallAddition = "" +
				"@prefix sioc: <http://rdfs.org/sioc/ns#> . " +
				"@prefix twista:<"+base+"> . " +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . " +
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema/> . " +
				"twista:num_followers a rdfs:Property . " +
				"twista:num_followers rdfs:domain sioc:UserAccount . " +
				"twista:num_followers rdfs:range xsd:nonNegativeInteger . " +
				"twista:num_followers rdfs:comment \"Indicates the number of followers of a user.\" . "+
				"twista:num_followers rdfs:label \"NumberOfFollowers\" . ";
		Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(smallAddition), null , "N3");
		OutputStream out = new ByteArrayOutputStream();
		model.write(out, null);
		return Response.ok(out.toString()).build();
	}
	
	@GET
	@Path( "/num_followers" ) @Produces("text/N3")
	public Response followersN3(@Context UriInfo uriInfo){
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/twista/";
		String smallAddition = "" +
				"@prefix sioc: <http://rdfs.org/sioc/ns#> . " +
				"@prefix twista: <"+base+"> . " +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . " +
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema/> . " +
				"twista:num_followers a rdfs:Property . " +
				"twista:num_followers rdfs:domain sioc:UserAccount . " +
				"twista:num_followers rdfs:range xsd:nonNegativeInteger . " +
				"twista:num_followers rdfs:comment \"Indicates the number of followers of a user.\" . " +
				"twista:num_followers rdfs:label \"NumberOfFollowers\" . "+
				"twista:num_followedby a rdfs:Property . " +
				"twista:num_followedby rdfs:domain sioc:UserAccount . " +
				"twista:num_followedby rdfs:range xsd:nonNegativeInteger . " +
				"twista:num_followers rdfs:comment \"Indicates the number of users a user follows.\" . " +
				"twista:num_followers rdfs:label \"isFollowedBy\" . " +
				"twista:foundbykeyword a rdfs:Property . "+
				"twista:foundbykeyword rdfs:domain sioc:Post . "+
				"twista:foundbykeyword rdfs:range rdfs:Literal. "+
				"twista:foundbykeyword rdfs:comment \"Indicates the keyword that was used to find the post\" . "+
				"twista:foundbykeyword rdfs:label \"FoundByKeyword\" . ";
		
		Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(smallAddition), null , "N3");
		OutputStream out = new ByteArrayOutputStream();
		model.write(out, "N3");
		return Response.ok(out.toString()).build();
	}
	
	public static void main(String[] args) throws HttpException, IOException{

	}
}
