/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.mb.integration.tests.mqtt.functional;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.mb.integration.common.clients.ClientMode;
import org.wso2.mb.integration.common.clients.MQTTClientEngine;
import org.wso2.mb.integration.common.clients.MQTTConstants;
import org.wso2.mb.integration.common.clients.QualityOfService;
import org.wso2.mb.integration.common.utils.backend.MBIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;

/**
 * Test different combinations of MQTT wildcards.
 *
 * 1. Test single level wildcard in different places of the topic hierarchy
 * 2. Test multi level wildcard in different places of the topic hierarchy
 * 3. Test combination of single level wild card and multi level wild card
 */
public class WildcardTestCase extends MBIntegrationBaseTest {

    private static final String multiLevelWildCard = "#";
    private static final String singleLevelWildCard = "+";

    private static final int noOfPublisherThreads = 1;
    private static final int noOfMessagesPerPublisher = 1;

    /**
     * Initialize super class.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void prepare() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    /**
     * Test multi level wildcard {@value WildcardTestCase#multiLevelWildCard}.
     * 1. Subscribe to {@value WildcardTestCase#multiLevelWildCard}
     * 2. Subscribe to multi/level/{@value WildcardTestCase#multiLevelWildCard}
     * 3. Publish to multi/level/wild/card
     * 4. Assert received messages
     * 5. Publish to multi
     * 6. Assert received messages
     *
     * @throws MqttException
     */
    @Test(groups = {"wso2.mb", "mqtt"}, description = "Test multi level wildcard")
    public void performMultiLevelWildcardTestCase() throws MqttException, XPathExpressionException {
        int noOfTopLevelSubscribers = 1;
        int noOfMidLevelSubscribers = 1;

        MQTTClientEngine mqttClientEngine = new MQTTClientEngine();

        // Creating Subscribers

        // Receive all the messages
        mqttClientEngine.createSubscriberConnection(multiLevelWildCard, QualityOfService.LEAST_ONCE,
                noOfTopLevelSubscribers, false, ClientMode.BLOCKING, automationContext);

        // Receive messages published to 'multi/level' and all it's sub levels
        mqttClientEngine.createSubscriberConnection("multi/level/" + multiLevelWildCard, QualityOfService.LEAST_ONCE,
                noOfMidLevelSubscribers, false, ClientMode.BLOCKING, automationContext);

        // Creating Publishers

        // Publish to 'multi/level/wild/card'
        mqttClientEngine.createPublisherConnection("multi/level/wild/card", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceived();
        int receivedMessageCount = mqttClientEngine.getReceivedMessageCount();

        int expectedMessageCount = noOfPublisherThreads * noOfMessagesPerPublisher * (noOfTopLevelSubscribers +
                noOfMidLevelSubscribers);

        Assert.assertEquals(receivedMessageCount, expectedMessageCount, "Did not received expected message count " +
                "after publishing to leaf level.");

        // Publish to 'multi'
        mqttClientEngine.createPublisherConnection("multi", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceivedAndShutdownClients();
        receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        expectedMessageCount = expectedMessageCount + noOfPublisherThreads * noOfMessagesPerPublisher *
                (noOfTopLevelSubscribers);

        Assert.assertEquals(receivedMessageCount, expectedMessageCount, "Did not received expected message count " +
                "after publishing to top level.");
    }

    /**
     * Test single level wildcard {@value WildcardTestCase#singleLevelWildCard}.
     * 1. Subscribe to {@value WildcardTestCase#singleLevelWildCard}
     * 2. Subscribe to single/{@value WildcardTestCase#singleLevelWildCard}
     * 3. Subscribe to single/level/{@value WildcardTestCase#singleLevelWildCard}
     * 4. Publish to single
     * 5. Assert received messages
     * 6. Publish to single/level
     * 7. Assert received messages
     * 8. Publish to single/level/wildcard
     * 9. Assert received messages
     *
     * @throws MqttException
     */
    @Test(groups = {"wso2.mb", "mqtt"}, description = "Test single level wildcard")
    public void performSingleLevelWildcardTest() throws MqttException, XPathExpressionException {
        int noOfTopLevelOnlySubscribers = 1;
        int noOfMidLevelOnlySubscribers = 1;
        int noOfLeafLevelOnlySubscribers = 1;

        MQTTClientEngine mqttClientEngine = new MQTTClientEngine();

        // Creating Subscribers

        // Receive all the messages published to top nodes only
        mqttClientEngine.createSubscriberConnection(singleLevelWildCard, QualityOfService.LEAST_ONCE,
                noOfTopLevelOnlySubscribers, false, ClientMode.BLOCKING, automationContext);

        // Receive messages published to 'single/<any>' and all it's sub levels
        mqttClientEngine.createSubscriberConnection("single/" + singleLevelWildCard,
                QualityOfService.LEAST_ONCE,
                noOfMidLevelOnlySubscribers, false, ClientMode.BLOCKING, automationContext);

        // Receive messages published to 'single/level/<any>' and all it's sub levels
        mqttClientEngine.createSubscriberConnection("single/level/" + singleLevelWildCard, QualityOfService.LEAST_ONCE,
                noOfLeafLevelOnlySubscribers, false, ClientMode.BLOCKING, automationContext);

        // Creating Publishers

        // Publish to 'single'
        mqttClientEngine.createPublisherConnection("single", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceived();
        int receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        int expectedMessageCount = noOfPublisherThreads * noOfMessagesPerPublisher * (noOfTopLevelOnlySubscribers);

        Assert.assertEquals(receivedMessageCount, expectedMessageCount, "Did not received expected message count " +
                "after publishing to top level.");

        // Publish to 'single/level'
        mqttClientEngine.createPublisherConnection("single/level", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceived();
        receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        expectedMessageCount = expectedMessageCount + noOfPublisherThreads * noOfMessagesPerPublisher *
                (noOfMidLevelOnlySubscribers);

        Assert.assertEquals(receivedMessageCount, expectedMessageCount, "Did not received expected message count " +
                "after publishing to mid level.");

        // Publish to 'single/level/wildcard'
        mqttClientEngine.createPublisherConnection("single/level/wildcard", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceivedAndShutdownClients();
        receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        expectedMessageCount = expectedMessageCount + noOfPublisherThreads * noOfMessagesPerPublisher *
                (noOfLeafLevelOnlySubscribers);

        Assert.assertEquals(receivedMessageCount, expectedMessageCount, "Did not received expected message count " +
                "after publishing to leaf level.");
    }

    /**
     * Test single level and multi level wildcards in conjunction.
     * 1. Subscribe to {@value WildcardTestCase#singleLevelWildCard}/{@value WildcardTestCase#multiLevelWildCard}
     * 2. Subscribe to mixed/{@value WildcardTestCase#singleLevelWildCard}/wild/{@value WildcardTestCase#multiLevelWildCard}
     * 3. Subscribe to {@value WildcardTestCase#singleLevelWildCard}/level/{@value WildcardTestCase#multiLevelWildCard}
     * 4. Subscribe to mixed/level/{@value WildcardTestCase#singleLevelWildCard}/{@value WildcardTestCase#multiLevelWildCard}
     * 5. Publish to mixed and assert received messages
     * 6. Publish to mixed/level and assert received messages
     * 7. Publish to mixed/level/wild/card and assert received messages
     *
     * @throws MqttException
     */
    @Test(groups = {"wso2.mb", "mqtt"}, description = "Test single level and multi level wildcards in conjunction",
            enabled = false)
    public void performMixWildcardTestCase() throws MqttException, XPathExpressionException { // Disabled due to MQTT Client not supporting
        int noOfAllLevelSubscribers = 1;
        int noOfMidAnySubscribers = 1;
        int noOfStartWithAnySubscribers = 1;
        int noOfAdjacentWildcardSubscribers = 1;

        MQTTClientEngine mqttClientEngine = new MQTTClientEngine();

        // Creating Subscribers

        // Receive all the messages published - '+/#'
        mqttClientEngine.createSubscriberConnection(singleLevelWildCard + "/" + multiLevelWildCard,
                QualityOfService.LEAST_ONCE, noOfAllLevelSubscribers, false, ClientMode.BLOCKING,
                automationContext);

        // Receive all the messages published to 'mixed/<any>/wild' and all it's sub topics
        mqttClientEngine.createSubscriberConnection("mixed/" + singleLevelWildCard + "/wild/" + multiLevelWildCard,
                QualityOfService.LEAST_ONCE, noOfMidAnySubscribers, false, ClientMode.BLOCKING, automationContext);

        // Receive all the messages published to '<any>/level' and all it's sub topics
        mqttClientEngine.createSubscriberConnection(singleLevelWildCard + "/level/" + multiLevelWildCard,
                QualityOfService.LEAST_ONCE, noOfStartWithAnySubscribers, false, ClientMode.BLOCKING,
                automationContext);

        // Receive all the messages published to sub trees of 'mixed/level' but not 'mixed/level'
        mqttClientEngine.createSubscriberConnection("mixed/level" + singleLevelWildCard + "/" + multiLevelWildCard,
                QualityOfService.LEAST_ONCE, noOfAdjacentWildcardSubscribers, false, ClientMode.BLOCKING,
                automationContext );

        // Creating Publishers

        // Publish to 'mixed'
        mqttClientEngine.createPublisherConnection("mixed", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceived();
        int receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        int expectedCount = noOfMessagesPerPublisher * noOfPublisherThreads * (noOfAllLevelSubscribers +
                noOfMidAnySubscribers);

        Assert.assertEquals(receivedMessageCount, expectedCount, "Did not received expected message count after " +
                "publishing to top level.");

        // Publish to 'mixed/level'
        mqttClientEngine.createPublisherConnection("mixed/level", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceived();
        receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
                expectedCount = expectedCount + noOfMessagesPerPublisher * noOfPublisherThreads * (noOfAllLevelSubscribers +
                noOfMidAnySubscribers +
                noOfStartWithAnySubscribers);

        Assert.assertEquals(receivedMessageCount, expectedCount, "Did not received expected message count after " +
                "publishing to two-level topic.");

        // Publish to 'mixed/level/wild/card'
        mqttClientEngine.createPublisherConnection("mixed/level/wild/card", QualityOfService.LEAST_ONCE,
                MQTTConstants.TEMPLATE_PAYLOAD, noOfPublisherThreads,
                noOfMessagesPerPublisher, ClientMode.BLOCKING, automationContext);

        mqttClientEngine.waitUntilAllMessageReceivedAndShutdownClients();
        receivedMessageCount = mqttClientEngine.getReceivedMessageCount();
        expectedCount = expectedCount + noOfMessagesPerPublisher * noOfPublisherThreads * (noOfAllLevelSubscribers +
                noOfMidAnySubscribers +
                noOfStartWithAnySubscribers + noOfAdjacentWildcardSubscribers);

        Assert.assertEquals(receivedMessageCount, expectedCount, "Did not received expected message count after " +
                "publishing to two-level topic.");

    }


}
