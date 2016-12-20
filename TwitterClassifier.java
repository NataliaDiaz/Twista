package hu.u_szeged.twitter;

import hu.u_szeged.Twit;
import hu.u_szeged.datamining.DataHandler;
import hu.u_szeged.datamining.DataMiningException;
import hu.u_szeged.datamining.Model;
import hu.u_szeged.datamining.mallet.MalletClassificationResult;
import hu.u_szeged.datamining.mallet.MalletClassifier;
import hu.u_szeged.datamining.mallet.MalletDataHandler;
import hu.u_szeged.utils.NLPUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.types.Alphabet;

import com.ibatis.common.resources.Resources;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.NormalizerAnnotator;
import edu.stanford.nlp.pipeline.NormalizerAnnotator.NormalizerAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class TwitterClassifier {

  /** Some external knowledge gathered from SWN */
  private static Map<CoreLabel, List<double[]>> swn;

  public TwitterClassifier() {
    featurePatterns = new Pattern[7];
    featurePatterns[0] = positiveSmileyPattern;
    featurePatterns[1] = plusNumberPattern;
    featurePatterns[2] = negativeSmileyPattern;
    featurePatterns[3] = minusNumberPattern;
    featurePatterns[4] = emphasizingPattern;
    featurePatterns[5] = reTwitPattern;
    featurePatterns[6] = charRunPattern;
    if (swn == null) {
      readInSWN();
    }
  }

  @SuppressWarnings("unchecked")
  private void readInSWN() {
    try {
      InputStream is = Resources.getResourceAsStream("SentiWordNet.ser");
      swn = (Map<CoreLabel, List<double[]>>) new ObjectInputStream(new BufferedInputStream(is)).readObject();
      is.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Map<String, int[]> getUserInfo() {
    Map<String, int[]> userInfo = new HashMap<String, int[]>();
    List<String> lines = new ArrayList<String>();
    NLPUtils.readDocToCollection("trainingUsers.txt", lines);
    for (String line : lines) {
      if (line.charAt(0) == '#')
        continue;
      String[] parts = line.split(";");
      userInfo.put(parts[0], new int[] { Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) });
    }
    return userInfo;
  }

  private List<Twit> getReliableTwits(boolean forSubjectivity) {
    List<Twit> reliableTwits = new LinkedList<Twit>();
    List<String> lines = new ArrayList<String>();
    Map<String, int[]> userInfo = getUserInfo();
    NLPUtils.readDocToCollection("debate08_sentiment_tweets.tsv", lines);
    for (int l = 0; l < lines.size(); ++l) {
      String line = lines.get(l);
      if (line.startsWith("#") || line.startsWith("tweet.id"))
        continue;
      String[] split = line.split("\t");
      Map<Integer, Integer> annotationStat = new HashMap<Integer, Integer>();
      for (int a = 5; a < split.length; ++a) {
        int classId = Integer.parseInt(split[a]);
        Integer prevVal = annotationStat.get(classId);
        annotationStat.put(classId, prevVal == null ? 1 : ++prevVal);
      }
      int maxNum = 0;
      int argMax = -1;
      int totalAnnotations = 0;
      for (Entry<Integer, Integer> vals : annotationStat.entrySet()) {
        totalAnnotations += vals.getValue();
        if (vals.getValue() > maxNum) {
          argMax = vals.getKey();
          maxNum = vals.getValue();
        }
      }
      int[] userVals = userInfo.get(split[3].replace("\"", ""));
      boolean isReallyNeededInTheCurrentSetting = forSubjectivity ? argMax != 3 : argMax < 3;
      // the annotation of that twit just does not seems like reliable
      if (!isReallyNeededInTheCurrentSetting || maxNum < 2 || totalAnnotations - maxNum > annotationStat.size() - 1
          || userVals == null) {
        continue;
      }
      String content = split[2];
      boolean classLabel = argMax == 1 || (argMax == 2 && forSubjectivity);
      Twit t = new Twit(content, reliableTwits.size() + 1, classLabel, userVals);
      reliableTwits.add(t);
    }
    return reliableTwits;
  }

  private static final NormalizerAnnotator norm = new NormalizerAnnotator();
  private static final Pattern negativeSmileyPattern = Pattern.compile(":'?-?[\\[cC(/\\\\]");
  private static final Pattern positiveSmileyPattern = Pattern.compile(":-?[)D\\]]");
  private static final Pattern minusNumberPattern = Pattern.compile("-\\d+");
  private static final Pattern plusNumberPattern = Pattern.compile("\\+\\d+");
  private static final Pattern emphasizingPattern = Pattern.compile("(^|\\s)(_[^\\s]+_|[^a-z\\s\\p{Punct}]{3,})(\\s|$)");
  private static final Pattern reTwitPattern = Pattern.compile("(?i)(^|\\s)rt(\\s|$)");
  private static final Pattern charRunPattern = Pattern.compile("(?i)([a-z])\\1{3,}");
  private Pattern[] featurePatterns;

  private LinkedList<String> updateDataHandler(Twit t, DataHandler dh, String instanceId, boolean interestedInTopics) {
    LinkedList<String> topics = new LinkedList<String>();
    int totalTokensOfTwit = 0, upperCaseTokens = 0;
    Annotation annotation = t.getAnnotation();
    norm.annotate(annotation);
    String originalText = annotation.get(TextAnnotation.class);
    for (int p = 0; p < featurePatterns.length; ++p) {
      Pattern pattern = featurePatterns[p];
      Matcher m = pattern.matcher(originalText);
      boolean patternFound = m.find();
      dh.setBinaryValue(instanceId, "pattern_" + p, patternFound);
    }
    Map<String, Integer> wordsUsages = new HashMap<String, Integer>();
    Set<String> verbTypes = new HashSet<String>();
    double[] total = new double[2];
    double maxPos = 0.0d, maxNeg = 0.0d;

    for (CoreMap annotatedSentence : annotation.get(SentencesAnnotation.class)) {
      String previousTopicType = "O";
      StringBuffer topic = new StringBuffer();
      for (CoreLabel token : annotatedSentence.get(TokensAnnotation.class)) {
        if (interestedInTopics) {
          String neTag = token.getString(NamedEntityTagAnnotation.class);
          neTag = neTag.equals("PERSON") ? neTag : "O";
          if (!previousTopicType.equals(neTag)) {
            if (topic.toString().length() > 0 && !previousTopicType.equals("O")) {
              topics.add(topic.toString());
              topics.add(previousTopicType);
            }
            topic = new StringBuffer(neTag.equals("O") ? "" : token.word());
          } else if (!neTag.equals("O") && previousTopicType.equals(neTag)) {
            topic.append(" " + token.word());
          }
          previousTopicType = neTag;
        }
        totalTokensOfTwit++;
        upperCaseTokens += Character.isUpperCase(token.word().charAt(0)) ? 1 : 0;
        String tag = token.tag();
        String normalized = token.getString(NormalizerAnnotation.class);
        Matcher m = charRunPattern.matcher(normalized);
        if (m.find()) {
          normalized = m.replaceAll("$1");
        }
        List<double[]> clSentimentScores = swn.get(token);
        double[] posNeg = new double[2];
        if (clSentimentScores != null) {
          for (double[] scores : clSentimentScores) {
            posNeg[0] += (double) scores[0] / clSentimentScores.size();
            posNeg[1] += (double) scores[1] / clSentimentScores.size();
          }
        }
        if (posNeg[0] > maxPos) {
          maxPos = posNeg[0];
        }
        if (posNeg[1] > maxNeg) {
          maxNeg = posNeg[1];
        }
        total[0] += posNeg[0];
        total[1] += posNeg[1];
        if (tag.startsWith("V")) {
          verbTypes.add(tag);
        }
        Integer prevVal = wordsUsages.get(normalized);
        wordsUsages.put(normalized, prevVal == null ? 1 : ++prevVal);
      }
    }
    dh.setNumericValue(instanceId, "totalPos", (double) total[0] / totalTokensOfTwit);
    dh.setNumericValue(instanceId, "totalNeg", (double) total[1] / totalTokensOfTwit);
    dh.setNumericValue(instanceId, "maxPos", maxPos);
    dh.setNumericValue(instanceId, "maxNeg", maxNeg);
    for (String verbType : verbTypes) {
      dh.setBinaryValue(instanceId, "verbType" + verbType, true);
    }
    for (Entry<String, Integer> verbUsage : wordsUsages.entrySet()) {
      dh.setNumericValue(instanceId, "verbUsage" + verbUsage, verbUsage.getValue());
    }
    dh.setNumericValue(instanceId, "tokens", totalTokensOfTwit);
    dh.setNumericValue(instanceId, "uppercaseRatio", (double) upperCaseTokens / totalTokensOfTwit);
    // int[] f = t.followersAndFriends();
    // dh.setNumericValue(instanceId, "followers", f[0]);
    // dh.setNumericValue(instanceId, "friends", f[1]);
    // dh.setNumericValue(instanceId, "followerRatio", (double) f[0] / f[1]);
    boolean label = t.getClassLabel();
    dh.setLabel(instanceId, label);
    return topics;
  }

  public void learnModel() {
    if (new File("true_sentimentModel.ser").exists())
      return;
    for (boolean forSubjectivity : new boolean[] { true, false }) {
      List<Twit> twitsToLearnFrom = getReliableTwits(forSubjectivity);
      Map<String, Object> initClassifier = new HashMap<String, Object>();
      initClassifier.put("classifier", "MaxEntL1");
      MalletDataHandler dataHandler = new MalletDataHandler();
      try {
        dataHandler.initClassifier(initClassifier);
      } catch (DataMiningException dme) {
        dme.printStackTrace();
      }
      dataHandler.createNewDataset(null);

      for (Twit t : twitsToLearnFrom) {
        updateDataHandler(t, dataHandler, "twit" + t.getTwitId(), false);
      }

      List<Alphabet> alphabets = new LinkedList<Alphabet>();
      alphabets.add(((MalletDataHandler) dataHandler).getAlphabet("feature"));
      alphabets.add(((MalletDataHandler) dataHandler).getAlphabet("label"));
      try {
        System.err.println(dataHandler.getInstanceCount() + "\t" + dataHandler.getFeatureCount());
        Model learnedModel = dataHandler.trainClassifier();
        NLPUtils.serialize(learnedModel, forSubjectivity + "_sentimentModel.ser");
        NLPUtils.serialize(alphabets, forSubjectivity + "_sentimentAlphabets.ser");
        System.err.println("Model learned successfully...");
      } catch (DataMiningException e) {
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> analyseSentiment(String content, String followerNum, String friendsNum) {
    String classLabel = null;
    LinkedList<String> toReturn = new LinkedList<String>();
    for (boolean forSubjectivity : new boolean[] { true, false }) {
      Model model = null;
      List<Alphabet> alphabets = null;
      try {
        InputStream is = Resources.getResourceAsStream(forSubjectivity + "_sentimentAlphabets.ser");
        alphabets = (List<Alphabet>) new ObjectInputStream(new BufferedInputStream(is)).readObject();
        is.close();
        is = Resources.getResourceAsStream(forSubjectivity + "_sentimentModel.ser");
        model = (MalletClassifier) new ObjectInputStream(new BufferedInputStream(is)).readObject();
        is.close();
      } catch (Exception e) {
        try {
          ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(forSubjectivity
              + "_sentimentAlphabets.ser")));
          alphabets = (List<Alphabet>) in.readObject();
          in.close();
          in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(forSubjectivity + "_sentimentModel.ser")));
          model = (MalletClassifier) in.readObject();
          in.close();
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
          e1.printStackTrace();
        }
      }

      int[] followers = { Integer.parseInt(followerNum), Integer.parseInt(friendsNum) };

      Twit t = new Twit(content, -1, followers);

      MalletDataHandler mdh = new MalletDataHandler();
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("useFeatureSet", alphabets);
      mdh.createNewDataset(parameters);
      toReturn = updateDataHandler(t, mdh, "dummiestInstanceNameEver", !forSubjectivity);

      try {
        MalletClassificationResult res = (MalletClassificationResult) mdh.classifyDataset(model);
        boolean classOfInstance = res.getPredictedLabel("dummiestInstanceNameEver");
        if (forSubjectivity && !classOfInstance) {
          classLabel = "NeutralEmotion";
          break;
        } else if (!forSubjectivity) {
          classLabel = classOfInstance ? "NegativeEmotion" : "PositiveEmotion";
        }
      } catch (DataMiningException e) {
        e.printStackTrace();
      }
    }
    toReturn.push(classLabel);
    return toReturn;
  }

  public static void main(String[] args) {
    TwitterClassifier tc = new TwitterClassifier();
    tc.learnModel();
    List<String> l = tc.analyseSentiment("-1 bad sorrow sad sorry :( :(", "20", "3");
    System.out.println(l);
    l = tc.analyseSentiment("+5 happy news :)", "20", "3");
    System.out.println(l);
    l = tc.analyseSentiment("Obama sucks :C", "20", "3");
    System.out.println(l);
    l = tc.analyseSentiment("McCain is great", "20", "3");
    System.out.println(l);
    l = tc.analyseSentiment("RT @APBBlue: So far, Obama is winning. #GOPdebate #p2", "0", "0");
    System.out.println(l);
    l = tc.analyseSentiment(
        "Word. RTì@Karoli: A-fricking-men RT @theonlyadult: It's PRESIDENT Obama, you stupid racists. #GOPdebateî 479 1034", "0", "0");
    System.out.println(l);
    l = tc.analyseSentiment("RT @nationallampoon: POLL: Smarter bet to win the GOP nomination - Cain or Obama? #GOPDebate", "207",
        "333");
    System.out.println(l);
  }
}