#TweetSniffer - Twitter Streaming API with Lambda Expressions

So far, the program contains a wrapper, that connects to the Twitter Streaming API with a given list of stop words and / or users to track. For each new Tweet arriving the given function object accepts it, so you can process the Tweets as you like without worrying about the API.

## Example
```java
TweetStreamer.stream(apiKeys, stopWords, users, tweet -> {
    System.err.println(tweet.getText()); // Print the text of every new Tweet.
});
```

That's all you have to write to do whatever you like with the Streaming API.

See [here](http://twitter4j.org/javadoc/twitter4j/Status.html), what you can do with *tweet*, an object of the class *Status*.

## Variables
*apiKeys* is an array of strings with the following layout:
```
0:  Consumer Key
1:  Consumer Secret
2:  Token
3:  Secret (of the Token)
```
-----
*stopWords* is a list of strings that should be tracked. You can initialise it like this:
```java
import com.google.common.collect.Lists;
//...
List<String> stopwords = Lists.newArrayList("twitter", "api", "other", "words");
```
----
*users* is a list of double values representing the user ID of twitter users you'd like to track:
```java
import com.google.common.collect.Lists;
//...
List<Long> users = Lists.newArrayList(1234L, 566788L);
```

## Configuration file
If you would like to use the whole program, the keys for accessing the API are read from a file called *config.properties*. It must be located in the root of the project and have the following layout:
```
consumerKey=
consumerSecret=
token=
secret=
```

## Maven dependencies
Currently, this project uses the *Guava* library created by Google for general tweaks and the [twitter4j](http://twitter4j.org) libraries (*twitter4j-stream* and *twitter4j-core*)

twitter also has a good documented [JavaDoc](http://twitter4j.org/javadoc/overview-summary.html).