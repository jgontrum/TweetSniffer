/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;
import com.beust.jcommander.Parameter;

/**
 * Define the arguments for the command line.
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class CommandLineArguments {
    @Parameter(names = { "--config", "-c" }, description = "Configuration file for MySQL and the Twitter API.")
    public String configFile = "config.properties";
    
    @Parameter(names = {"--filter", "-f"}, description = "Define a filter for the Twitter stream.")
    public String filterFile = "filter.properties";
    
    @Parameter(names = {"--testdb", "--test", "-t"}, description = "Only try to connect to the database server and NOT to Twitter. Usefull if you have to troubleshoot your MySQL settings.")
    public boolean testDB = false;    
    
    @Parameter(names = {"help", "--help", "-help", "--usage", "--info"}, description="Displays this information.", help = true)
    public boolean help;
}
