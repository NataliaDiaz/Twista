/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package daw;

import java.text.SimpleDateFormat;
import java.util.List;
import org.json.simple.JSONObject;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author User
 */
public class TwitterSearch {
  public static String tweetsJson(String keyword){
        Twitter twitter = new TwitterFactory().getInstance();
        StringBuffer answer = new StringBuffer("[");
        twitter4j.User tuser;
        try {
            Query query = new Query(keyword);
            query.setRpp(100);
            query.setLang("en");
            //query.setSince("2011-07-01");
            //QueryResult result = twitter.search(query);
            QueryResult result = twitter.search(query);
            answer = new StringBuffer("[");
            List<Tweet> tweets = result.getTweets();
            int followers = 0;
            int friends   = 0;
            int count =0;
            String content;
            String username;
            for (Tweet tweet : tweets) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String date1 = formatter.format(tweet.getCreatedAt());
                formatter = new SimpleDateFormat("HH:mm:ss");
                String time = formatter.format(tweet.getCreatedAt());

                if(count>0)
                    answer.append(",");
                username = tweet.getFromUser();
                answer.append("{\"user\": \"" + username +"\",");
                answer.append("\"id\": \"" + tweet.getId()+"\",");
                content = tweet.getText();
                //content.replaceAll("(?:[^a-z0-9 ]|(?<=['\"@])s)", "");
                content = JSONObject.escape(content);

                answer.append("\"content\":\"" + content+"\",\"date\": \""+date1+"\",");

                try {
//                    tuser = twitter.showUser(username);
//                    followers = tuser.getFollowersCount();
//                    friends   = tuser.getFriendsCount();
                     friends = 0;
                    followers = 0;
                } catch (Exception ex) {
                    friends = -1;
                    followers = -1;
                }
                answer.append("\"keyword\": \"" + keyword +"\",");
                answer.append("\"time\":\"" + time+"\",\"followers\": \""+followers+"\",");
                answer.append("\"friends\": \""+friends+"\"}");
                count++;
            }
            answer.append("]");
            return answer.toString();



        } catch (TwitterException te) {
            te.printStackTrace();
            String error = te.getMessage();
            System.out.println("Failed to search tweets: " + error);
            answer.append("]");
            return answer.toString();
        }

    }

}
