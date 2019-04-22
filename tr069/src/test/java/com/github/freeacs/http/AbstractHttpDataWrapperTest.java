package com.github.freeacs.http;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractHttpDataWrapperTest {

    private static final String FAKE_SESSION_ID = "fakeSessionId";

    @BeforeClass
    public static void setup() {
        SessionData mockSessionData = mock(SessionData.class);
        BaseCache.putSessionData(FAKE_SESSION_ID, mockSessionData);
    }

    @AfterClass
    public static void teardown() {
        BaseCache.removeSessionData(FAKE_SESSION_ID);
    }

    @Test
    public void getHttpRequestResponseDate() throws SQLException {
        // Given:
        final DBAccess mockDBAccess = mock(DBAccess.class);
        final DBI mockDbi = mock(DBI.class);
        final ACS mockAcs = mock(ACS.class);
        when(mockDbi.getAcs()).thenReturn(mockAcs);
        when(mockDBAccess.getDBI()).thenReturn(mockDbi);
        final HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
        final HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getId()).thenReturn(FAKE_SESSION_ID);
        when(mockServletRequest.getSession()).thenReturn(mockSession);
        final HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
        final Properties properties = new Properties(ConfigFactory.load());

        // When:
        final AbstractHttpDataWrapper wrapper = new AbstractHttpDataWrapper(mockDBAccess, properties) {};
        final HTTPRequestResponseData httpRequestResponseData =
            wrapper.getHttpRequestResponseData(mockServletRequest, mockServletResponse);

        // Then:
        assertNotNull(httpRequestResponseData);
        assertEquals(properties.getContextPath(), httpRequestResponseData.getRequestData().getContextPath());
    }
}
