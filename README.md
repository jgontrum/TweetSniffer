#Tweets2SQL

Tweets2SQL is designed as an application for those who want to access the **Twitter Streaming API** in a smooth way. The Tweets will be saved in a MySQL database, so you can analyse and interpret the data easily from your own programs.

## Simple configuration

You have to provide two configuration files: One with technical information about the database and your API codes and one that defines the filter for the Streaming API. This way you can securely **share your filter without exposing any passwords** or other sensitive information.

### filter.properties
Given no arguments, the program tries to read *filter.properties*. Or use the command line option *--filter* / *-f*.
``` properties
# Multiple values must be separated by a semicolon
# Define a rectangle by the south-west coordinate first and then the north-west one.
# Example: coordinates=7.910156,51.074194,13.535156,54.576838;...
coordinates=
users=
terms=
```
--
### config.properties
Given no arguments, the program tries to read *config.properties*. Or use the command line option *--config* / *-c*.
``` properties
## Twitter API
consumerKey=
consumerSecret=
token=
secret=

## MySQL
mySQLHost=localhost:3306/tweets # define the database by '/db'
mySQLUser=
mySQLPassword=
mySQLTablePrefix=testFilter # The name of the table
```
**Note:** Future versions will let you define the layout of the table in this file. Also there will be an option to create a new table for each day.

## Roadmap
Future versions will implement the following features:
* Defining the structure of the table in the *config.properties* file.
* Import Tweets as JSON objects from file.
* Limit filtering: Stop after *n* Tweets / after *n* minutes

-----

# Implementation details


So far, the program contains a wrapper, that connects to the Twitter Streaming API with a given list of stop words and / or users to track. For each new Tweet arriving the given function object accepts it, so you can process the Tweets as you like without worrying about the API.

## Example
``` java
TweetStreamer.stream(apiKeys, stopWords, users, coordinates, tweet -> {
    System.err.println(tweet.getText()); // Print the text of every new Tweet.
});
```

That's all you have to write to do whatever you like with the Streaming API.

See [here](http://twitter4j.org/javadoc/twitter4j/Status.html), what you can do with *tweet*, an object of the class *Status*.


## Maven dependencies
Currently, this project uses the *Guava* library created by Google for general tweaks and the [twitter4j](http://twitter4j.org) libraries (*twitter4j-stream* and *twitter4j-core*).

twitter4j also has a good documented [JavaDoc](http://twitter4j.org/javadoc/overview-summary.html).