import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimerTask;

public class TwittsPublisher extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(TwittsPublisher.class);

    private static String PROJECT_ID;
    private static String PUBSUB_TOPIC;
    private static boolean isPublisherMode = false;
    private static int batchSize = 10;
    private static Publisher publisher = null;
    private static int counter = 0;
    private final TwitterStream twitterStream;
    private String[] keywords;
    private String[] languages;


    public TwittsPublisher(String[] keywords) throws Exception{

        if (System.getenv("TWBATCHSIZE") != null) {
            String batchSizeStr = System.getenv("TWBATCHSIZE");
            try {
                batchSize = Integer.parseInt(batchSizeStr);
            } catch (Exception e) {
                throw new Exception("Batch size is not number!");
            }
        } else {
            throw new Exception("Batch size is not defined!");
        }

        // over write filtering key words
        if (System.getenv("TWKEYWORDS") != null) {
            this.keywords = System.getenv("TWKEYWORDS").split(",");
        } else if (keywords.length > 0) {
            this.keywords = keywords;
        } else {
            throw new Exception("Key words not found!");
        }

        if (System.getenv("TWLANGUAGE") != null) {
            this.languages = System.getenv("TWLANGUAGE").split(",");
        } else {
            throw new Exception("Language preference not found!");
        }

        if (System.getenv("TWPROJECTID") != null) {
            this.PROJECT_ID = System.getenv("TWPROJECTID");
        } else {
            throw new Exception("GCP project not found!");
        }

        if (System.getenv("PUBSUB_TOPIC") != null) {
            this.PUBSUB_TOPIC = System.getenv("PUBSUB_TOPIC");
        } else {
            throw new Exception("Pub/Sub topic not found!");
        }

        if (System.getenv("TWSTREAMMODE") != null) {
            this.isPublisherMode = System.getenv("TWSTREAMMODE").equals("publisher");
        }

        // create configuration Builder
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(System.getenv("CONSUMERKEY"))
                .setOAuthConsumerSecret(System.getenv("CONSUMERSECRET"))
                .setOAuthAccessToken(System.getenv("ACCESSTOKEN"))
                .setOAuthAccessTokenSecret(System.getenv("ACCESSTOKENSEC"));


        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, PUBSUB_TOPIC);

        try {
            publisher = Publisher.newBuilder(topicName).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();

        twitterStream.addListener(new StatusListener() {
            @Override
            public void onException(Exception e) {

            }

            public void onStatus(Status status) {

                if (counter < batchSize) {
                    JSONObject json = toJson(status);
                    if (null != json.getString("text") && !json.getString("text").startsWith("RT")) {
                        String jsonInString = toJson(status).toString();
                        System.out.println(jsonInString);
                        LOGGER.debug(jsonInString);
                        if (isPublisherMode) {
                            publish(jsonInString);
                        }
                        counter++;
                    }
                } else {
                    twitterStream.shutdown();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }
        });
    }

    public static void main(String[] args) {
        LOGGER.info("Program start!");
        try {
            TwittsPublisher myPublisher = new TwittsPublisher(args);
            myPublisher.run();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        
        LOGGER.info("Program complete!");
        
    }

    public void publish(String message) {

        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(message))
                .build();

        //schedule a message to be published, messages are automatically batched
        ApiFuture<String> future = publisher.publish(pubsubMessage);

        // add an asynchronous callback to handle success / failure
        ApiFutures.addCallback(future, new ApiFutureCallback<String>() {

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable instanceof ApiException) {
                    ApiException apiException = ((ApiException) throwable);
                    // details on the API exception
                    LOGGER.error(apiException.getStatusCode().getCode().toString());
                    LOGGER.error(apiException.isRetryable() + "");
                }
                LOGGER.error("Error publishing message : " + throwable.getMessage());
            }

            @Override
            public void onSuccess(String messageId) {
                // Once published, returns server-assigned message ids (unique within the topic)
                LOGGER.info("Success: " + messageId);
            }
        });
    }

    public JSONObject toJson(Status status) {
        JSONObject newObj = new JSONObject();
        JSONObject newUser = new JSONObject();
        //EEE MMM d HH:mm:ss Z yyyy
        newObj.put("created_at", new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").format(status.getCreatedAt()));
        newObj.put("id_str", status.getId() + "");
        newObj.put("text", status.getText());

        newUser.put("id_str", status.getUser().getId() + "");
        newUser.put("name", status.getUser().getName());
        newUser.put("followers_count", status.getUser().getFollowersCount());
        newUser.put("friends_count", status.getUser().getFriendsCount());
        newUser.put("listed_count", status.getUser().getListedCount());
        newUser.put("favourites_count", status.getUser().getFavouritesCount());
        newUser.put("statuses_count", status.getUser().getStatusesCount());
        newUser.put("created_at", new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
                .format(status.getUser().getCreatedAt()));
        newUser.put("lang", status.getUser().getLang());
        newObj.put("user", newUser);
        newObj.put("lang", status.getLang());

        return newObj;
    }

    @Override
    public void run() {
        FilterQuery tweetFilterQuery = new FilterQuery();
        counter = 0;
        tweetFilterQuery.track(keywords);
        tweetFilterQuery.language(languages);
        twitterStream.filter(tweetFilterQuery);
    }

}
