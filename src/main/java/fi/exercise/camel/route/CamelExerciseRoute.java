package fi.exercise.camel.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import fi.exercise.camel.aggregator.NewEntryAggregator;
import fi.exercise.camel.model.TimeEntry;
import fi.exercise.camel.processor.ArchieveDirectoryProcessor;
import fi.exercise.camel.processor.TotalTimeProcessor;

@Component
public class CamelExerciseRoute extends RouteBuilder{
    
    @Override
    public void configure() throws Exception {
        // Configurations for web server
        restConfiguration()
            .component("servlet")
            .host("localhost")
            .port("{{server.port}}");

        // Publish REST POST endpoint | accepts JSON
        rest("/worktime")
            .post()
            .routeId("rest-worktime-entries")
            .consumes("application/json")   
            .to("direct:jsonToXml");

        // Publish REST GET endpoint
        rest("/archieve")
            .get()
            .routeId("rest-entries-archieve")
            .to("direct:get-archieve-list");

        // Publish REST GET endpoint
        rest("/total")
            .get()
            .routeId("total-hours")
            .to("direct:get-totals");

        
        // Fetch and return total.xml
        from("direct:get-totals")
            .routeId("get-totals")

            // Get total.xml
            .pollEnrich("file:{{total-time}}?fileName=total.xml&noop=true", 5000)

            // Store og body to property
            .setProperty("originalBody", simple("${body}"))

            // Unmarshal body to POJO
            .unmarshal().jacksonXml()

            // Calculate total hours and insert as element into response
            .process(new TotalTimeProcessor())

            .log(LoggingLevel.INFO, "Returning total.xml with added TotalHours element");


        // Fetch files from archieve | return list in XML
        from("direct:get-archieve-list")
            .routeId("get-archieve-list")

            // Get list of files located in archieve
            .process(new ArchieveDirectoryProcessor())

            // Marshal POJO -> XML
            .marshal().jacksonXml(true)

            .log(LoggingLevel.INFO, "Returning list of files in archieve");
             

        // Transform body from JSON to POJO to XML
        from("direct:jsonToXml")
            .routeId("json-to-xml")

            // Unmarshal JSON -> POJO
            .unmarshal().json(JsonLibrary.Jackson,TimeEntry.class)

            // Marshal POJO -> XML
            .marshal().jacksonXml(TimeEntry.class, true)

            // Set name for the time entry file
            .setHeader("CamelFileName").simple("${date:now:yyyy-MM-dd-HH.mm.ss}-entry.xml")

            // Save file to entries directory
            .to("file:{{entries}}")

            .log(LoggingLevel.INFO, "Saved :: ${header.CamelFileName} INTO {{entries}}")

            // Set return message
            .setBody(constant( "Time entry successful"));


        // Fetch *-entry.xml -files from {{entries}} folder
        from("file:{{entries}}?include=.*-entry.xml&delete=true&scheduler=quartz&scheduler.cron=0/25+*+*+*+*+?")
            .routeId("timed-transfer-of-entries")

            // Move original file to archieve
            .to("file:{{archieve}}")

            .log(LoggingLevel.INFO, "Archieved :: ${header.CamelFileName}")

            // Insert content to total xml
            .to("direct:insert-entry-to-total-time");
        

        // Insert new time entry into total.xml 
        from("direct:insert-entry-to-total-time")
            .routeId("insert-entry-to-total-time")

            // Store original messages filename to property
            .setProperty("EntryFileName").simple("${header.CamelFileName}")

            // Set target file name
            .setHeader("CamelFileName").constant("total.xml")    

            // Poll directory for total.xml and insert entry.xml content to it
            .pollEnrich("file:{{total-time}}?fileName=total.xml&preMove=inProcess&delete=true", 5000, new NewEntryAggregator())

            .log(LoggingLevel.INFO, "New entry :: FROM ${exchangeProperty.EntryFileName} INTO ${header.CamelFileName}")

            // Write aggregated content to total.xml
            .to("file:{{total-time}}");            
    }
    
}
