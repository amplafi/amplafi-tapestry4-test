// Copyright 2006 Howard M. Lewis Ship
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.test.Creator;
import org.easymock.IMocksControl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

import static org.easymock.EasyMock.createStrictControl;

/**
 * A base class for creating TestNG unit tests for Tapestry 4 applications. With slightly more
 * effort, this may be used as a utility class with other frameworks, such as JUnit.
 * <p>
 * A single <em>strict</em> mock control is used for <strong>all</strong> mocks, which means that
 * order of operations is checked not just for any single mock but across mocks.
 * <p>
 * Provides common mock factory and mock trainer methods.
 * <p>
 * Provides easy access to instantiated component instances.
 * <p>
 * Extends from {@link org.testng.Assert} to bring in all the public static assert methods without
 * requiring extra imports.
 * <p>
 * TestNG supports running tests in parallel, as does this class. The EasyMock control is stored in
 * a <em>thread local</em>. This is necessary as TestNG instantiates a single instance of the
 * test case class, and invokes methods on it from multiple threads.
 *
 * @author Howard M. Lewis Ship
 */
public class TestBase extends Assert
{
    private static class ControlSource extends ThreadLocal<IMocksControl>
    {
        /** Creates a strick control for <em>this</em> thread. */
        @Override
        protected IMocksControl initialValue()
        {
            return createStrictControl();
        }
    }

    private final ControlSource _source = new ControlSource();

    // Access to this is synchronized.

    private Creator _creator;

    /**
     * Creates a new instance of the provided class using the
     * {@link org.apache.tapestry.test.Creator} utility.
     *
     * @param <T>
     *            the component type
     * @param componentClass
     * @param properties
     *            alternating property names and property values to be injected into the instance
     * @return the instantiated and configured component instance
     */
    public synchronized final <T> T newInstance(Class<T> componentClass, Object... properties)
    {
        if (_creator == null) {
            _creator = new Creator();
        }

        Object instance = _creator.newInstance(componentClass, properties);

        return componentClass.cast(instance);
    }

    /**
     * Discards any mock objects created during the test. When using TestBase as a utility class,
     * not a base class, you must be careful to either invoke this method, or discard the TestBase
     * instance at the end of each test.
     */
    @AfterMethod(alwaysRun = true)
    public final void cleanupControlSource()
    {
        // TestNG reuses the same class instance across all tests within that
        // class, so if we don't
        // clear out the mocks, they will tend to accumulate. That can get
        // expensive, and can
        // cause unexpected cascade errors when an earlier test fails.

        // After each method runs, we clear this thread's mocks control.
        _source.remove();
    }

    /**
     * Creates a new mock object of the indicated type. The created object is retained for the
     * duration of the test (specifically to support {@link #replay()} and {@link #verify()}).
     *
     * @param <T>
     *            the type of the mock object
     * @param mockClass
     *            the class to mock
     * @return the mock object, ready for training
     */
    public final <T> T newMock(Class<T> mockClass)
    {
        return getMocksControl().createMock(mockClass);
    }

    /**
     * Replay's the mocks control, preparing all mocks for testing.
     */
    public final void replay()
    {
        getMocksControl().replay();
    }

    /**
     * Verifies the mocks control, ensuring that all mocks completed all trained method invocations,
     * then resets the control to allow more training of the mocks.
     */
    public final void verify()
    {
        IMocksControl control = getMocksControl();

        control.verify();
        control.reset();
    }

    /** Returns the control object used for all mocks created by this test case. */
    public final IMocksControl getMocksControl()
    {
        return _source.get();
    }

    /**
     * Invoked to indicate code should not reach a point. This is typically used after code that
     * should throw an exception.
     */
    public final void unreachable()
    {
        fail("This code should not be reachable.");
    }

    /** Mock factory method. */
    public final IRequestCycle newRequestCycle()
    {
        return newMock(IRequestCycle.class);
    }

    /** Mock factory method. */
    public final IMarkupWriter newMarkupWriter()
    {
        return newMock(IMarkupWriter.class);
    }
}
