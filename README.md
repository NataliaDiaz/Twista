# Twista
An Evolution Timeline Sentiment Analysis in Politics Microblogging via Twitter microblogging and Linked Open Services for Sentiment Analysis. 
 -Summer school on Semantic Computing, UC Berkeley Hackathon winner project 2011-

Prezi presentation: https://prezi.com/tqvx8wa7xhq9/twista/



Dependency: 
<li>Timeline Gadget used: http://www.simile-widgets.org/timeplot/docs/
<li>Stanford NLP

Tasks:
<li>Generation of RDF by employing JSON2RDF (generic lifting).
<li>Semantic Lifting happens via SPARQL CONSTRUCT Service Call 
<li>SPARQL Endpoint Query Query for additional data in DBpedia (Extract all necessary information from triple store (SPARQL))
<li>Builds JSP and data files for libraries to visualize results RDF Input RDF Output Sentiment Ontology models the six basic human emotions [1]

Data: Annotated Tweets related to the U.S. electional debate from 2008 via Mechanical Turk: 4 classes, 3,238 multiple annotated Tweets

Feature set:
<li>SentiWordNet based features
<li>Token based features (tokens occurring, total #token)
<li>Linguistic features (POS-codes)
<li>Surface based features (e.g. 'uppercasedness') Thank you! giving pluses/minuses, smileys, character-runs, retwitting, forms of emphasising maximum/average positivity/negativity scores per Tweet


Mechanisms to secure quality (annotations done too fast or predictibly sloppily were discarded)
Our preliminary investigation ended up in further filtering Training data Subjectivity detection Neutrality detection Polarity detection Positive Tweet Negative Tweet 2445 instances
<li>4292 features 1938 instances
<li>3722 features Machine Learning Framework MaxEnt classifier



Team members:
<li>Gábor Berend (berendg@inf.u-szeged.hu)
<li>Natalia Díaz (ndiaz@decsai.ugr.es)
<li>Steffen Stadtmüller (Steffen.Stadtmueller@kit.edu)
<li>Carmen Vaca (vacaruiz@elet.polimi.it) API Extract Tweets + Info that is tagged with the retrieved Keyword.


[1] Parrott, W. (2001), Emotions in Social Psychology, Psychology Press, Philadelphia links to the human emotions ontology later on possible Sentiment Analysis Service Response 
[2] N. Diakopoulos, D. Shamma: Characterizing: Debate Performance via Aggregated Twitter Sentiment, CHI 2010 

