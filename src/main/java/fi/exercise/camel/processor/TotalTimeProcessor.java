package fi.exercise.camel.processor;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class TotalTimeProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        // total.xml as string
        StringBuilder ogBody = new StringBuilder(
            exchange.getProperty("originalBody", String.class)
        );
        
        // total.xml as hashmap
        HashMap<?,?> totalXmlAsHashMap = exchange.getIn().getBody(HashMap.class);

        // entries as list from hashmap
        ArrayList<?> listOfEntries = (ArrayList<?>) totalXmlAsHashMap.get("TimeEntry");
                
        // Calculate total hours from entries        
        int aggregatedHours = 0;

        for(Object entry: listOfEntries){
            if(entry instanceof HashMap){
                Object hours = ((HashMap<?,?>) entry).get("hours");
                if(hours instanceof String) aggregatedHours += Integer.parseInt((String) hours);
            }
        }

        String timeEntriesElement = "<TimeEntries>";

        StringBuilder totalHoursElement = new StringBuilder("<TotalHours>").append(aggregatedHours).append("</TotalHours>");

        // Calculate position where to insert TotalTime element
        int indxOfInsert = ogBody.indexOf(timeEntriesElement) + timeEntriesElement.length();

        ogBody.insert(indxOfInsert, totalHoursElement);

        exchange.getIn().setBody(ogBody);       
    }    
}
