package fi.exercise.camel.processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchieveDirectoryProcessor implements Processor {

    private static Logger logger = LoggerFactory.getLogger(ArchieveDirectoryProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        String dirPath = Path.of("").toAbsolutePath().toString();

        // Get archieve property from camel context
        Optional<String> prop = exchange.getContext().getPropertiesComponent().resolveProperty("archieve");
        if (prop.isPresent()) {
            dirPath += prop.get();
        }
       
        // Add to list all the file names located at archieve directory
        List<String> listOfFiles = Files.list(Path.of(dirPath))
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(Collectors.toList());

        logger.info("Found " + listOfFiles.size() + " time entry files from archieve at " + dirPath + " :: " + listOfFiles);
        
        exchange.getIn().setBody(listOfFiles);        
    }
    
}
