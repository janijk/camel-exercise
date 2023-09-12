package fi.exercise.camel.aggregator;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewEntryAggregator implements AggregationStrategy{

    private static Logger logger = LoggerFactory.getLogger(NewEntryAggregator.class);

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        String totalXml = newExchange != null ? newExchange.getIn().getBody(String.class) : "";
        String entryXml = oldExchange.getIn().getBody(String.class);
        String aggregatedXml = combineXml(totalXml, entryXml);
        
        oldExchange.getIn().setBody(aggregatedXml);

        return oldExchange;
    }

    private String combineXml(String totalXml, String entryXml) {
        int indxOfEndTag = totalXml.indexOf("</TimeEntries>");
        StringBuilder aggregatedXml = new StringBuilder();

        // Does TimeEntries element exists in the file
        if (indxOfEndTag != -1) {

            // Insert content of entryXml as last element within totalXml's <TimeEntries> tag
            aggregatedXml.append(totalXml).insert(indxOfEndTag, entryXml);

        } else{ 

            logger.warn("Missing file :: initializing total.xml");

            aggregatedXml.append("<TimeEntries>\n").append(entryXml).append("</TimeEntries>");
        }

        return aggregatedXml.toString();
    }
    
}
