package tools;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;



import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
/**
 *      Artifact that implements the auction.
 */
public class RedditMgmt extends Artifact {

    private Display display;
/**
 *      Information related to the user.
*/
    private static final String USER_AGENT = "agent1";
    private static final String CLIENT_ID = "Pla4gjOWJLc1uNjKfhU5PQ";
    private static final String CLIENT_SECRET = "Cgr7V5goiMWMqrOdmCemjWi4tIyj7Q";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static final String ACCESS_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String POSTS_URL = "https://www.reddit.com/r/subreddit/new";
    private static final String USER_AGENT_PARAM = "User-Agent";
    private static final String AUTHORIZATION_PARAM = "Authorization";
    private static final String BEARER_PARAM = "Bearer ";

    void init(String name) {
        // creates an observable property called numMsg
        this.defineObsProperty("numMsg",0);
        display = new Display(name);
        display.setVisible(true);
    }


    private static String getPostsJson(String subreddit, String accessToken) throws Exception {
        String urlString = "https://www.reddit.com/r/" +subreddit+ "/hot.json";
        URL url = new URL(urlString);
        // Open an HTTP connection to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        // Read the response from the connection
        Scanner scanner = new Scanner(conn.getInputStream());
        StringBuilder responseBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            responseBuilder.append(scanner.nextLine());
        }
        scanner.close();

        // Return the JSON response as a string
        return responseBuilder.toString();
    }

    @OPERATION static String submitPost(String subreddit, String title, String content, String url,String accessToken) throws Exception {
        System.out.println("[RedditMgmt] ------ submitting ------");
        String urlParameters =
                "&sr=" + URLEncoder.encode(subreddit, "UTF-8") +
                        "&title=" + URLEncoder.encode(title, "UTF-8") +
                        "&text=" + URLEncoder.encode(content, "UTF-8") +
                        "&url=" + URLEncoder.encode(url, "UTF-8");

        byte[] postData = urlParameters.getBytes();
        URL urlObj = new URL("https://oauth.reddit.com/api/submit");
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setDoOutput(true);
        connection.getOutputStream().write(postData);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        reader.close();
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //JsonElement je = JsonParser.parseString(response.toString());
        //String prettyJsonString = gson.toJson(je);

        return response.toString();


    }



    // implements an operation available to the agents


    @OPERATION void printMsg(String msg){
        String agentName = this.getOpUserName();
        ObsProperty prop = this.getObsProperty("numMsg");
        prop.updateValue(prop.intValue()+1);
        display.addText("Message at "+System.currentTimeMillis()+" from "+agentName+": "+msg);
        display.updateNumMsgField(prop.intValue());
    }


    static class Display extends JFrame {

        private JTextArea text;
        private JLabel numMsg;
        private static int n = 0;

        public Display(String name) {
            setTitle(".:: "+name+" console ::.");

            JPanel panel = new JPanel();
            setContentPane(panel);
            panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

            numMsg = new JLabel("0");
            text = new JTextArea(15,40);

            panel.add(text);
            panel.add(Box.createVerticalStrut(5));
            panel.add(numMsg);
            pack();
            setLocation(n*40, n*80);
            setVisible(true);

            n++;
        }

        public void addText(final String s){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    text.append(s+"\n");
                }
            });
        }

        public void updateNumMsgField(final int value){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    numMsg.setText(""+value);
                }
            });
        }
    }
    public static boolean topic_related(String subreddit, String topic) {
        // Define a map of related topics for each subreddit
        Map<String, List<String>> related_topics = new HashMap<>();
        related_topics.put("science", Arrays.asList("biology", "chemistry", "physics"));
        related_topics.put("technology", Arrays.asList("computers", "software", "gadgets"));
        related_topics.put("sports", Arrays.asList("basketball", "football", "soccer"));
        related_topics.put("funny", Arrays.asList("fun","laugh"));
        // Add more related topics for other subreddits

        // Check if the topic is in the list of related topics for the subreddit
        if (related_topics.containsKey(subreddit) && related_topics.get(subreddit).contains(topic)) {
            return true;
        } else {
            return false;
        }
    }
    @OPERATION void submit(String subreddit, String title, String content, String url, String topic) {
        try {
            String accessToken = "2471115951123-hsbe4rIiBYgqN0Cvii07uKnoJme5ng"; // replace with your access token
            content += "\n\nTopic: " + topic;
            if (topic_related(subreddit, topic)) {
                String postId = submitPost(subreddit, title, content, url,accessToken);
                printMsg("\nPostID: " + postId);
                System.out.println("Post submitted successfully");
            } else {
                String errorMsg = "Error: Topic " + topic + " is not related to the subreddit " + subreddit;
                failed(errorMsg);
            }
        } catch (Exception e) {
            System.err.println("Error: Topic " + topic + " is not related to the subreddit " + subreddit); // print the error message to the console
            failed(e.getMessage()); //
        }

    }

    @OPERATION void retrieve(String subreddit){
        String accessToken = "2471115951123-RtfZnG189WmZTzs9w5N5Sjy6gBVR1w"; // replace with your access token
        System.out.println("[RedditMgmt] ------ retrieve ------");
        try {
            String postsJson = getPostsJson(subreddit, accessToken);
            printMsg("\nPosts: " + postsJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}

