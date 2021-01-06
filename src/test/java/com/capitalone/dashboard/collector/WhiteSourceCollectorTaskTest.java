package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.client.RestClient;
import com.capitalone.dashboard.client.RestOperationsSupplier;
import com.capitalone.dashboard.config.CollectorTestConfig;
import com.capitalone.dashboard.config.TestMongoServerConfig;
import com.capitalone.dashboard.config.TestRestConfig;
import com.capitalone.dashboard.config.TestConstants;
import com.capitalone.dashboard.model.LibraryPolicyResult;
import com.capitalone.dashboard.model.WhiteSourceCollector;
import com.capitalone.dashboard.repository.LibraryPolicyResultsRepository;
import com.capitalone.dashboard.repository.LibraryReferenceRepository;
import com.capitalone.dashboard.repository.WhiteSourceCollectorRepository;
import com.capitalone.dashboard.repository.WhiteSourceComponentRepository;
import com.capitalone.dashboard.settings.WhiteSourceSettings;
import com.capitalone.dashboard.utils.TestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestRestConfig.class, TestMongoServerConfig.class, CollectorTestConfig.class})
@ComponentScan("com.capitalone.dashboard.client")
@PrepareForTest(fullyQualifiedNames = "com.capitalone.dashboard.*")
public class WhiteSourceCollectorTaskTest {

    @Autowired
    private WhiteSourceCollectorRepository whiteSourceCollectorRepository;

    @Autowired
    private WhiteSourceComponentRepository whiteSourceComponentRepository;

    @Autowired
    private DefaultWhiteSourceClient whiteSourceClient;

    @Autowired
    private RestClient restClient;

    @Autowired
    private WhiteSourceSettings whiteSourceSettings;

    @Autowired
    private LibraryReferenceRepository libraryReferenceRepository;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private LibraryPolicyResultsRepository libraryPolicyResultsRepository;

    @Autowired
    RestOperationsSupplier restOperationsSupplier;

    @Autowired
    private WhiteSourceCollectorTask whiteSourceCollectorTask;


    @Before
    public void populateRequestResponse() {
        PowerMockito.spy(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(TestConstants.FIX_TIME_MILLIS);
        populateOrgDetailsResponse();
        populateOrgChangeReportResponse();
        populateAllProductsResponse();
        populateAllProjectsResponse();
        populateProjectVitalsResponse();

        long t = System.currentTimeMillis();
    }

    private void populateProjectVitalsResponse() {
        String response = getJsonResponse("project-vitals.json");
        TestUtils.addResponse(restOperationsSupplier, TestConstants.PROJECT_VITALS_FOR_ORG_REQUEST, response,HttpStatus.ACCEPTED);
    }

    private void populateAllProjectsResponse() {
        String response = getJsonResponse("all-projects-" + TestConstants.PRODUCT_TOKEN_Test1Product + ".json");
        TestUtils.addResponse(restOperationsSupplier, String.format(TestConstants.ALL_PROJECTS_REQUEST, TestConstants.PRODUCT_TOKEN_Test1Product), response,HttpStatus.ACCEPTED);

        response = getJsonResponse("all-projects-" + TestConstants.PRODUCT_TOKEN_Test2Product + ".json");
        TestUtils.addResponse(restOperationsSupplier, String.format(TestConstants.ALL_PROJECTS_REQUEST, TestConstants.PRODUCT_TOKEN_Test2Product), response,HttpStatus.ACCEPTED);

        response = getJsonResponse("all-projects-" + TestConstants.PRODUCT_TOKEN_Test3Product + ".json");
        TestUtils.addResponse(restOperationsSupplier, String.format(TestConstants.ALL_PROJECTS_REQUEST, TestConstants.PRODUCT_TOKEN_Test3Product), response,HttpStatus.ACCEPTED);

        response = getJsonResponse("all-projects-" + TestConstants.PRODUCT_TOKEN_Test5Product + ".json");
        TestUtils.addResponse(restOperationsSupplier, String.format(TestConstants.ALL_PROJECTS_REQUEST, TestConstants.PRODUCT_TOKEN_Test5Product), response,HttpStatus.ACCEPTED);
    }

    private void populateAllProductsResponse() {
        String response = getJsonResponse("all-products.json");
        TestUtils.addResponse(restOperationsSupplier, TestConstants.ALL_PRODUCTS_REQUEST, response,HttpStatus.ACCEPTED);
    }

    private void populateOrgChangeReportResponse() {
        String response = getJsonResponse("changelog.json");
        TestUtils.addResponse(restOperationsSupplier, TestConstants.CHANGE_REPORT_REQUEST, response,HttpStatus.ACCEPTED);
    }

    private void populateOrgDetailsResponse() {
        String response = getJsonResponse("org-details.json");
        TestUtils.addResponse(restOperationsSupplier, TestConstants.ORG_DETAILS_REQUEST, response,HttpStatus.ACCEPTED);
    }

    @Test
    public void getCollector() {
        WhiteSourceCollector collector = whiteSourceCollectorTask.getCollector();
        Iterable<LibraryPolicyResult> all = libraryPolicyResultsRepository.findAll();
        whiteSourceCollectorTask.run();
//        whiteSourceCollectorTask.collect(collector);
    }


    private String getJsonResponse(String fileName) {
        String json = null;
        try {
            json = IOUtils.toString(WhiteSourceCollectorTaskTest.class.getResourceAsStream(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}