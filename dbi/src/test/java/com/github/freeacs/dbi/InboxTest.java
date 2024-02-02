package com.github.freeacs.dbi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InboxTest {

    private Inbox inbox;
    private Message message;

    @BeforeEach
    void setUp() {
        inbox = new Inbox();
        message = new Message(1, "messageType", 2, "objectType");
    }

    @Test
    void testAddToInboxWithNoFilters() {
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }

    @Test
    void testAddToInboxWithMatchingSenderFilter() {
        inbox.addFilter(new Message(1, null, null, null));
        inbox.addToInbox(message);
        assertEquals(1, inbox.getAllMessages().size());
    }

    @Test
    void testAddToInboxWithNonMatchingSenderFilter() {
        inbox.addFilter(new Message(999, null, null, null));
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }

    @Test
    void testAddToInboxWithMatchingMessageTypeFilter() {
        inbox.addFilter(new Message(null, "messageType", null, null));
        inbox.addToInbox(message);
        assertEquals(1, inbox.getAllMessages().size());
    }

    @Test
    void testAddToInboxWithNonMatchingMessageTypeFilter() {
        inbox.addFilter(new Message(null, "otherMessageType", null, null));
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }

    @Test
    void testAddToInboxWithMatchingReceiverFilter() {
        inbox.addFilter(new Message(null, null, 2, null));
        inbox.addToInbox(message);
        assertEquals(1, inbox.getAllMessages().size());
    }

    @Test
    void testAddToInboxWithNonMatchingReceiverFilter() {
        inbox.addFilter(new Message(null, null, 999, null));
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }

    @Test
    void testAddToInboxWithMatchingObjectTypeFilter() {
        inbox.addFilter(new Message(null, null, null, "objectType"));
        inbox.addToInbox(message);
        assertEquals(1, inbox.getAllMessages().size());
    }

    @Test
    void testAddToInboxWithNonMatchingObjectTypeFilter() {
        inbox.addFilter(new Message(null, null, null, "otherObjectType"));
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }

    @Test
    void testAddToInboxWithMultipleFilters() {
        inbox.addFilter(new Message(1, "messageType", null, null));
        inbox.addFilter(new Message(null, null, 2, "objectType"));
        inbox.addToInbox(message);
        assertEquals(1, inbox.getAllMessages().size());
    }

    @Test
    void testAddToInboxWithMultipleNonMatchingFilters() {
        inbox.addFilter(new Message(999, "messageType", null, null));
        inbox.addFilter(new Message(null, null, 999, "objectType"));
        inbox.addToInbox(message);
        assertTrue(inbox.getAllMessages().isEmpty());
    }
}
