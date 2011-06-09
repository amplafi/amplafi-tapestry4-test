/*
 * Created on Jun 23, 2007
 * Copyright 2006-2008 by Amplafi
 */
package org.amplafi.tapestry4;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.easymock.classextension.EasyMock.*;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.services.ApplicationGlobals;
import org.apache.tapestry.services.CookieSource;
import org.apache.tapestry.services.RequestGlobals;
import org.apache.tapestry.services.ResponseBuilder;
import org.apache.tapestry.services.impl.RequestGlobalsImpl;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.web.WebActivator;
import org.apache.tapestry.web.WebContext;
import org.apache.tapestry.web.WebRequest;
import org.apache.tapestry.web.WebResponse;
import org.easymock.internal.ClassExtensionHelper;
import org.easymock.internal.MocksControl;
import org.easymock.internal.RuntimeExceptionWrapper;

/**
 * This class assumes that {@link RequestGlobals} is in a threaded
 * pool (6/2007:this is current default).
 *
 * So if creating mocks it finds that there already is a mock,
 * it may just reset it.
 * @author Patrick Moore
 */
public class ProgrammableTapestryGlobals {
    private RequestGlobals requestGlobals;
    private CookieSource cookieSource;
    private ApplicationGlobals applicationGlobals;
    public ProgrammableTapestryGlobals() {

    }

    public void initializeService() {
        requestGlobals = new RequestGlobalsImpl();
        programApplicationGlobals();
    }

    /**
     * Assign nice mocks to all Tapestry Request objects.
     */
    public void initializeWithNiceMocks() {
        store(createNiceMock(IRequestCycle.class));
        store(createNiceMock(ResponseBuilder.class));
        store(createNiceMock(HttpServletRequest.class), createNiceMock(HttpServletResponse.class));
        store(createNiceMock(WebRequest.class), createNiceMock(WebResponse.class));
        replayMocks();
    }
    /**
     * Assign mocks to all Tapestry Request objects.
     */
    public void initializeWithMocks() {
        store(createMock(IRequestCycle.class));
        store(createMock(ResponseBuilder.class));
        store(createMock(HttpServletRequest.class), createMock(HttpServletResponse.class));
        store(createMock(WebRequest.class), createMock(WebResponse.class));
    }

    public void replayMocks() {
        for(Iterator<Object> mock= mocksIterator(); mock.hasNext(); ) {
            MocksControl control = ClassExtensionHelper.getControl(mock.next());
            control.replay();
        }
    }
    public void resetMocks() {
        for(Iterator<Object> mock= mocksIterator(); mock.hasNext(); ) {
            MocksControl control = ClassExtensionHelper.getControl(mock.next());
            control.reset();
        }
    }
    public void verifyMocks() {
        for(Iterator<Object> mock= mocksIterator(); mock.hasNext(); ) {
            MocksControl control = ClassExtensionHelper.getControl(mock.next());
            control.verify();
        }
    }

    public WebRequest getWebRequest()
    {
        return requestGlobals.getWebRequest();
    }

    public WebResponse getWebResponse()
    {
        return requestGlobals.getWebResponse();
    }

    public HttpServletRequest getRequest()
    {
        return requestGlobals.getRequest();
    }

    public HttpServletResponse getResponse()
    {
        return requestGlobals.getResponse();
    }

    public void store(WebRequest request, WebResponse response)
    {
        requestGlobals.store(request, response);
    }

    public void store(HttpServletRequest request, HttpServletResponse response)
    {
        requestGlobals.store(request, response);
    }
    public IRequestCycle getRequestCycle()
    {
        return requestGlobals.getRequestCycle();
    }

    public void store(IRequestCycle cycle)
    {
        requestGlobals.store(cycle);
    }

    public ResponseBuilder getResponseBuilder()
    {
        return requestGlobals.getResponseBuilder();
    }

    public void store(ResponseBuilder builder)
    {
        requestGlobals.store(builder);
    }

    public List<Object> requestObjects() {
        return Arrays.asList(new Object[] {
                getRequest(),
                getResponse(),
                getWebRequest(),
                getWebResponse(),
                getRequestCycle()
        });
    }

    @SuppressWarnings("unchecked")
    public Iterator<Object> mocksIterator() {
        return new FilterIterator(requestObjects().iterator(),
                new Predicate() {
                    public boolean evaluate(Object object) {
                        return isMockObject(object);
                    }
                });
    }
    private boolean isMockObject(Object object) {
        if ( object == null ) {
            return false;
        }
        try {
            MocksControl control = ClassExtensionHelper.getControl(object);
            return control != null;
        } catch (RuntimeExceptionWrapper e) {
            // was not a mock... ignore
            return false;
        }
    }

    public RequestGlobals getRequestGlobals() {
        return requestGlobals;
    }
    public CookieSource getCookieSource() {
        return cookieSource;
    }
    /**
    *
    */
   public void programApplicationGlobals() {
       if ( getApplicationGlobals() != null ) {
           WebActivator activator = createNiceMock(WebActivator.class);
           expect(activator.getActivatorName()).andReturn("under-test").anyTimes();
           WebContext webContext = createNiceMock(WebContext.class);
           ServletContext servletContext = createNiceMock(ServletContext.class);
           IApplicationSpecification applicationSpecification = createNiceMock(IApplicationSpecification.class);
           replay(activator, applicationSpecification, webContext, servletContext);
           getApplicationGlobals().storeActivator(activator);
           getApplicationGlobals().storeWebContext(webContext);
           getApplicationGlobals().storeSpecification(applicationSpecification);
           getApplicationGlobals().storeServletContext(servletContext);
       }
   }
    public void setApplicationGlobals(ApplicationGlobals applicationGlobals) {
        this.applicationGlobals = applicationGlobals;
    }

    public ApplicationGlobals getApplicationGlobals() {
        return applicationGlobals;
    }

    /**
     *
     */
    public void storeNiceMocks() {
        store(createNiceMock(IRequestCycle.class));
        store(createNiceMock(ResponseBuilder.class));
        store(createNiceMock(HttpServletRequest.class), createNiceMock(HttpServletResponse.class));
    }

}
