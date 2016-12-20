package org.linkedservices.twittersentiment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
@Path("/") 
public class Vocabulary {
	
	Model model; 
	String modelString;

	
	public Vocabulary(@Context UriInfo uriInfo ){
		String base = uriInfo.getAbsolutePath().toString().split("/rs/")[0]+"/rs/emotions#";
		modelString =
			"@prefix dc: <http://purl.org/dc/terms/> . " +
			"@prefix dbpo: <http://dbpedia.org/ontology/> . " +
			"@prefix foaf: <http://xmlns.com/foaf/0.1/> . " +
			"@prefix sent: <"+base+"> . " +
			"@prefix owl: <http://www.w3.org/2002/07/owl#> . " +
			"@prefix dcm: <http://purl.org/dc/dcmitype/> . " +
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema/> . " +
			"@prefix cyc: <http://sw.opencyc.org/concept/> . " +
			"@prefix sioc: <http://rdfs.org/sioc/ns#> . " +
			"sent:Sentiment a rdfs:Class . " +
			"sent:Sentiment rdfs:comment \"According to Parrot 2001 there are 6 basic hymen emotions\" . " +
			"sent:Positive a rdfs:Class . " +
			"sent:Negative a rdfs:Class . " +
			"sent:PositiveEmotion rdfs:SubClassOf sent:Sentiment . " +
			"sent:NegativeEmotion rdfs:SubClassOf sent:Sentiment . " +
			"sent:NeutralEmotion rdfs:subclassOf sent:Sentiment . " +
			"sent:Love a sent:PositiveEmotion . " +
			"sent:Joy a sent:PositiveEmotion . " +
			"sent:Surprise a sent:Sentiment . " +
			"sent:Sadness a sent:NegativeEmotion . " +
			"sent:Anger a sent:NegativeEmotion . " +
			"sent:Fear a sent:NegativeEmotion . " +
			"sent:foundIn a rdfs:Property . " +
			"sent:foundIn rdfs:range sent:Sentiment . " +
			"sent:foundIn rdfs:domain sioc:Post . " ;


		
		model = ModelFactory.createDefaultModel();
		model.read(new StringReader(modelString), null , "N3");
	}
	
	@GET @Produces("application/rdf+xml")
	@Path("/emotions") 
	public String replySchemaXML(){
		OutputStream out = new ByteArrayOutputStream();
		model.write(out, null);
		return out.toString();
	}
	@GET @Produces("text/N3")
	@Path("/emotions")
	public String replySchemaN3(){
		OutputStream out = new ByteArrayOutputStream();
		model.write(out, "N3");
		return out.toString();
	}
	
	@GET @Produces("application/rdf+xml")
	@Path("/emotions#{x}")
	public String replySchemaXMLSuppl(){
		return replySchemaXML();
	}
	
	@GET @Produces("text/N3")
	@Path("/emotions#{x}")
	public String replySchemaN3Suppl(){
		return replySchemaN3();
	}
	
	
	@GET @Produces("text/html")
	@Path("/emotions")
	public String replyHTML(){
		OutputStream out = new ByteArrayOutputStream();
		model.write(out, "N3");
		return "<html><title>Emotions Ontology</title><body>" +
  		"<h2>Emotions Ontology</h2>" +
  		"<br />" +
  		"<br />" +
  		"<br />" +
  		"<br />" +
  		"<br />" +
  		out.toString() +
  				"</body></html>";
	}
	
}
