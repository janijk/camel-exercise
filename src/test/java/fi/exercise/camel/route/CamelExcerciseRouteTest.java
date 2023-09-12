package fi.exercise.camel.route;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;


import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest/* (classes = Application.class) */
@CamelSpringBootTest
@MockEndpoints
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
// @UseAdviceWith
public class CamelExcerciseRouteTest /* extends CamelTestSupport */{

    @Autowired
    private ProducerTemplate template;

    @EndpointInject("file:{{entries}}")
    private MockEndpoint mock;

    @Test
    @DirtiesContext
    void jsonToXmlEndpointTest_JSONCorrectlyConvertedToXml() throws Exception {

        /* AdviceWith.adviceWith(context, "timed-transfer-of-entries", a ->
            a.mockEndpointsAndSkip("*")
        );

        context.start(); */

        String jsonFilePath = "src/test/resources/entry.json";
        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

        String xmlFilePath = "src/test/resources/entry.xml";
        String xmlContent = new String(Files.readAllBytes(Paths.get(xmlFilePath)));

        mock.expectedBodiesReceived(xmlContent);

        template.sendBody("direct:jsonToXml", jsonContent);

        mock.assertIsSatisfied();
    }

}
