package io.openliberty.guides.inventory;

import io.openliberty.guides.models.SystemLoad;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

import java.util.logging.Logger;

@MessageDriven(mappedName="jms/InventoryQueue")
public class InventoryQueueListener implements MessageListener {

    private static Logger logger = Logger.getLogger(InventoryQueueListener.class.getName());

    @Inject
    private InventoryManager manager;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String json = textMessage.getText();
                SystemLoad systemLoad = SystemLoad.fromJson(json);

                String hostname = systemLoad.hostname;
                Double loadAverage = systemLoad.loadAverage;

                if (manager.getSystem(hostname).isPresent()) {
                    manager.updateCpuStatus(hostname, loadAverage);
                    logger.info("Host " + hostname + " was updated: " + loadAverage);
                } else {
                    manager.addSystem(hostname, loadAverage);
                    logger.info("Host " + hostname + " was added: " + loadAverage);
                }
            } else {
                logger.warning("Unsupported Message Type: " + message.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
