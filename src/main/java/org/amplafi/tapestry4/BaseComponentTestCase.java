package org.amplafi.tapestry4;

import org.apache.hivemind.*;
import org.apache.hivemind.impl.DefaultClassResolver;
import org.apache.hivemind.impl.RegistryBuilder;
import org.apache.hivemind.impl.XmlModuleDescriptorProvider;
import org.apache.hivemind.util.URLResource;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IEngine;
import org.apache.tapestry.IForm;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRender;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.NestedMarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.TapestryUtils;
import org.apache.tapestry.TestBase;
import org.apache.tapestry.components.ILinkComponent;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.engine.NullWriter;
import org.apache.tapestry.event.BrowserEvent;
import org.apache.tapestry.json.IJSONWriter;
import org.apache.tapestry.markup.AsciiMarkupFilter;
import org.apache.tapestry.markup.JSONWriterImpl;
import org.apache.tapestry.markup.MarkupWriterImpl;
import org.apache.tapestry.services.ResponseBuilder;
import org.apache.tapestry.services.impl.DefaultResponseBuilder;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.test.Creator;
import org.apache.tapestry.web.WebRequest;

import static org.easymock.EasyMock.*;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Base class for testing components, or testing classes that operate on components. Simplifies
 * creating much of the infrastructure around the components.
 *
 */
public class BaseComponentTestCase extends TestBase
{
    private Creator _creator;

    public BaseComponentTestCase() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

    protected Creator getCreator()
    {
        if (_creator == null) {
            _creator = new Creator();
        }

        return _creator;
    }

    protected ClassResolver getClassResolver()
    {
        return new DefaultClassResolver();
    }

    protected CharArrayWriter _charArrayWriter;

    protected IMarkupWriter newBufferWriter()
    {
        _charArrayWriter = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(_charArrayWriter);

        return new MarkupWriterImpl("text/html", pw, new AsciiMarkupFilter());
    }

    protected IJSONWriter newBufferJSONWriter()
    {
        _charArrayWriter = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(_charArrayWriter);

        return new JSONWriterImpl(pw);
    }

    protected void assertBuffer(String expected)
    {
        String actual = _charArrayWriter.toString();

        assertEquals(actual, expected);

        _charArrayWriter.reset();
    }

    protected void assertExceptionSubstring(Throwable t, String msg)
    {
        assertTrue(t.getMessage().contains(msg));
    }

    protected IRequestCycle newCycle()
    {
        return newMock(IRequestCycle.class);
    }

    protected IRequestCycle newCycle(IMarkupWriter writer)
    {
        IRequestCycle cycle = newMock(IRequestCycle.class);

        trainResponseBuilder(cycle, writer);

        return cycle;
    }

    protected IRequestCycle newCycle(boolean rewinding)
    {
        return newCycle(rewinding, null);
    }

    protected IRequestCycle newCycle(boolean rewinding, boolean trainWriter)
    {
        IRequestCycle cycle = newRequestCycle();

        trainIsRewinding(cycle, rewinding);

        if (trainWriter) {
            trainResponseBuilder(cycle, null);
        }

        return cycle;
    }

    protected IRequestCycle newCycle(boolean rewinding, IMarkupWriter writer)
    {
        IRequestCycle cycle = newRequestCycle();
        checkOrder(cycle, false);

        trainIsRewinding(cycle, rewinding);

        if (writer != null) {
            trainResponseBuilder(cycle, writer);
        }

        return cycle;
    }

    protected void trainResponseBuilder(IRequestCycle cycle, IMarkupWriter writer)
    {
        ResponseBuilder builder =
            new DefaultResponseBuilder(writer == null ? NullWriter.getSharedInstance() : writer);

        expect(cycle.getResponseBuilder()).andReturn(builder);
    }

    protected void trainIsRewinding(IRequestCycle cycle, boolean rewinding)
    {
        expect(cycle.isRewinding()).andReturn(rewinding);
    }

    protected IRequestCycle newCycleGetPage(String pageName, IPage page)
    {
        IRequestCycle cycle = newRequestCycle();

        expect(cycle.getPage(pageName)).andReturn(page);

        return cycle;
    }

    protected IRequestCycle newCycleGetUniqueId(String id, String uniqueId)
    {
        IRequestCycle cycle = newRequestCycle();

        expect(cycle.getUniqueId(id)).andReturn(uniqueId);
        return cycle;
    }

    protected IRequestCycle newCycleGetParameter(String name, String value)
    {
        IRequestCycle cycle = newRequestCycle();

        expect(cycle.getParameter(name)).andReturn(value);
        return cycle;
    }

    protected IMarkupWriter newWriter()
    {
        return newMock(IMarkupWriter.class);
    }

    protected IBinding newBinding(Object value)
    {
        IBinding binding = newMock(IBinding.class);
        checkOrder(binding, false);

        expect(binding.getObject()).andReturn(value);
        return binding;
    }

    protected IBinding newBinding(Location location)
    {
        IBinding binding = newBinding();
        checkOrder(binding, false);

        trainGetLocation(binding, location);

        return binding;
    }

    protected IComponent newComponent(String extendedId, Location location)
    {
        IComponent component = newMock(IComponent.class);
        checkOrder(component, false);

        expect(component.getExtendedId()).andReturn(extendedId);
        expect(component.getLocation()).andReturn(location);
        return component;
    }

    protected IComponentSpecification newSpec(String parameterName, IParameterSpecification pspec)
    {
        IComponentSpecification spec = newMock(IComponentSpecification.class);

        expect(spec.getParameter(parameterName)).andReturn(pspec);
        return spec;
    }

    protected IRender newRender()
    {
        return newMock(IRender.class);
    }

    protected IPage newPage()
    {
        return newMock(IPage.class);
    }

    protected IPage newPage(String name)
    {
        return newPage(name, 1);
    }

    protected IPage newPage(String name, int count)
    {
        IPage page = newMock(IPage.class);
        checkOrder(page, false);

        expect(page.getPageName()).andReturn(name).times(count);

        return page;
    }

    protected IForm newForm()
    {
        return newMock(IForm.class);
    }

    protected IRender newBody()
    {
        return new IRender()
        {
            @SuppressWarnings("unused")
            public void render(IMarkupWriter writer, IRequestCycle cycle)
            {
                writer.print("BODY");
            }
        };
    }

    protected PageRenderSupport newPageRenderSupport()
    {
        return newMock(PageRenderSupport.class);
    }

    protected void trainGetSupport(IRequestCycle cycle, PageRenderSupport support)
    {
        trainGetAttribute(cycle, TapestryUtils.PAGE_RENDER_SUPPORT_ATTRIBUTE, support);
    }

    protected void trainGetAttribute(IRequestCycle cycle, String attributeName, Object attribute)
    {
        expect(cycle.getAttribute(attributeName)).andReturn(attribute);
    }

    protected void trainGetUniqueId(IRequestCycle cycle, String id, String uniqueId)
    {
        expect(cycle.getUniqueId(id)).andReturn(uniqueId);
    }

    protected void trainGetIdPath(IComponent component, String idPath)
    {
        expect(component.getIdPath()).andReturn(idPath);
    }

    protected void trainGetParameter(IRequestCycle cycle, String name, String value)
    {
        expect(cycle.getParameter(name)).andReturn(value);
    }

    protected void trainGetPageName(IPage page, String pageName)
    {
        expect(page.getPageName()).andReturn(pageName);
    }
    @SuppressWarnings("unused")
    protected void trainBuildURL(IAsset asset, IRequestCycle cycle, String URL)
    {
        expect(asset.buildURL()).andReturn(URL);
    }

    protected IAsset newAsset()
    {
        return newMock(IAsset.class);
    }
    @SuppressWarnings("unused")
    protected IEngine newEngine(ClassResolver resolver)
    {
        return newMock(IEngine.class);
    }

    protected void trainGetEngine(IPage page, IEngine engine)
    {
        expect(page.getEngine()).andReturn(engine);
    }

    protected IComponent newComponent()
    {
        return newMock(IComponent.class);
    }

    protected void trainGetPage(IComponent component, IPage page)
    {
        expect(component.getPage()).andReturn(page);
    }

    protected void trainGetExtendedId(IComponent component, String extendedId)
    {
        expect(component.getExtendedId()).andReturn(extendedId);
    }

    protected void trainGetLocation(Locatable locatable, Location location)
    {
        expect(locatable.getLocation()).andReturn(location);
    }

    protected IBinding newBinding()
    {
        return newMock(IBinding.class);
    }

    protected void trainGetComponent(IComponent container, String componentId, IComponent containee)
    {
        expect(container.getComponent(componentId)).andReturn(containee);
    }

    protected IEngineService newEngineService()
    {
        return newMock(IEngineService.class);
    }
    @SuppressWarnings("unused")
    protected void trainGetLink(IEngineService service, IRequestCycle cycle, boolean post,
            Object parameter, ILink link)
    {
        expect(service.getLink(post, parameter)).andReturn(link);
    }
    @SuppressWarnings("unused")
    protected void trainGetLinkCheckIgnoreParameter(IEngineService service, IRequestCycle cycle,
            boolean post, Object parameter, ILink link)
    {
        expect(service.getLink(eq(post), anyObject())).andReturn(link);
    }

    protected void trainGetURL(ILink link, String URL)
    {
        expect(link.getURL()).andReturn(URL);
    }

    protected void trainGetPageRenderSupport(IRequestCycle cycle, PageRenderSupport support)
    {
        trainGetAttribute(cycle, TapestryUtils.PAGE_RENDER_SUPPORT_ATTRIBUTE, support);
    }

    protected IComponentSpecification newSpec()
    {
        return newMock(IComponentSpecification.class);
    }

    protected Resource newResource()
    {
        return newMock(Resource.class);
    }

    protected WebRequest newRequest()
    {
        return newMock(WebRequest.class);
    }

    protected Location newLocation()
    {
        return newMock(Location.class);
    }

    protected Location fabricateLocation(int line)
    {
        Location location = newLocation();
        checkOrder(location, false);

        expect(location.getLineNumber()).andReturn(line).anyTimes();

        return location;
    }

    protected void trainEncodeURL(IRequestCycle rc, String URL, String encodedURL)
    {
        expect(rc.encodeURL(URL)).andReturn(encodedURL);
    }

    protected void trainGetServerPort(WebRequest request, int port)
    {
        expect(request.getServerPort()).andReturn(port);
    }

    protected void trainGetServerName(WebRequest request, String serverName)
    {
        expect(request.getServerName()).andReturn(serverName);
    }

    protected void trainGetScheme(WebRequest request, String scheme)
    {
        expect(request.getScheme()).andReturn(scheme);
    }

    protected NestedMarkupWriter newNestedWriter()
    {
        return newMock(NestedMarkupWriter.class);
    }

    protected void trainGetNestedWriter(IMarkupWriter writer, NestedMarkupWriter nested)
    {
        expect(writer.getNestedWriter()).andReturn(nested);
    }

    protected void trainGetURL(ILink link, String scheme, String anchor, String URL)
    {
        trainGetURL(link, scheme, anchor, URL, 0);
    }

    protected void trainGetURL(ILink link, String scheme, String anchor, String URL, int port)
    {
        expect(link.getURL(scheme, null, port, anchor, true)).andReturn(URL);
    }

    protected ILink newLink()
    {
        return newMock(ILink.class);
    }

    protected void trainGetLink(ILinkComponent component, IRequestCycle cycle, ILink link)
    {
        expect(component.getLink(cycle)).andReturn(link);
    }

    protected void trainGetEngine(IRequestCycle cycle, IEngine engine)
    {
        expect(cycle.getEngine()).andReturn(engine);
    }

    protected void trainGetParameterValues(ILink link, String parameterName, String[] values)
    {
        expect(link.getParameterValues(parameterName)).andReturn(values);
    }

    protected void trainGetParameterNames(ILink link, String[] names)
    {
        expect(link.getParameterNames()).andReturn(names);
    }

    protected void trainGetSpecification(IComponent component, IComponentSpecification spec)
    {
        expect(component.getSpecification()).andReturn(spec);
    }

    protected void trainGetBinding(IComponent component, String name, IBinding binding)
    {
        expect(component.getBinding(name)).andReturn(binding);
    }

    protected void trainGetId(IComponent component, String id)
    {
        expect(component.getId()).andReturn(id);
    }

    protected void trainExtractBrowserEvent(IRequestCycle cycle)
    {
        expect(cycle.getParameter(BrowserEvent.NAME)).andReturn("onClick").anyTimes();

        expect(cycle.getParameter(BrowserEvent.TYPE)).andReturn("click");
        expect(cycle.getParameters(BrowserEvent.KEYS)).andReturn(null);
        expect(cycle.getParameter(BrowserEvent.CHAR_CODE)).andReturn(null);
        expect(cycle.getParameter(BrowserEvent.PAGE_X)).andReturn("123");
        expect(cycle.getParameter(BrowserEvent.PAGE_Y)).andReturn("1243");
        expect(cycle.getParameter(BrowserEvent.LAYER_X)).andReturn(null);
        expect(cycle.getParameter(BrowserEvent.LAYER_Y)).andReturn(null);

        expect(cycle.getParameter(BrowserEvent.TARGET + "." + BrowserEvent.TARGET_ATTR_ID))
        .andReturn("element1");
    }

    /**
     * Convienience method for invoking {@link #buildFrameworkRegistry(String[])} with only a single
     * file.
     *
     * @param file
     *          The path to the hivemind xml configuration file.
     * @return The constructed registry.
     *
     * @throws Exception When file can't be found or parsed.
     */
    protected Registry buildFrameworkRegistry(String file)
            throws Exception
    {
        return buildFrameworkRegistry(new String[] { file });
    }

    /**
     * Builds a minimal registry, containing only the specified files, plus the master module
     * descriptor (i.e., those visible on the classpath).
     *
     * @param files
     *          The path to the hivemind xml configuration files to parse.
     * @return The constructed registry.
     *
     * @throws Exception When file can't be found or parsed.
     */
    protected Registry buildFrameworkRegistry(String[] files)
            throws Exception
    {
        ClassResolver resolver = getClassResolver();

        List<Resource> descriptorResources = new ArrayList<Resource>();
        for (String file : files)
        {
            Resource resource = getResource(file);

            descriptorResources.add(resource);
        }

        ModuleDescriptorProvider provider = new XmlModuleDescriptorProvider(resolver, descriptorResources);

        return buildFrameworkRegistry(provider);
    }

    /**
     * Builds a registry, containing only the modules delivered by the specified
     * {@link org.apache.hivemind.ModuleDescriptorProvider}, plus the master module descriptor
     * (i.e., those visible on the classpath).
     *
     * @param customProvider
     *          The custom module provider that should be added to the configuration.
     *
     * @return A constructed {@link Registry}.
     */
    protected Registry buildFrameworkRegistry(ModuleDescriptorProvider customProvider)
    {
        ClassResolver resolver = getClassResolver();

        RegistryBuilder builder = new RegistryBuilder();

        builder.addModuleDescriptorProvider(new XmlModuleDescriptorProvider(resolver));
        builder.addModuleDescriptorProvider(customProvider);

        return builder.constructRegistry(Locale.getDefault());
    }

    /**
     * Builds a registry from exactly the provided resource; this registry will not include the
     * <code>hivemind</code> module.
     *
     * @param l
     *         The resource to build the registry from.
     * @return A constructed {@link Registry} instance.
     *
     * @throws Exception If error building registry.
     */
    protected Registry buildMinimalRegistry(Resource l)
            throws Exception
    {
        RegistryBuilder builder = new RegistryBuilder();

        return builder.constructRegistry(Locale.getDefault());
    }

    /**
     * Returns the given file as a {@link Resource} from the classpath. Typically, this is to find
     * files in the same folder as the invoking class.
     *
     * @param file
     *          Gets a resource object for the file representing the path specified.
     * @return A {@link Resource} object.
     */
    protected Resource getResource(String file)
    {
        URL url = getClass().getResource(file);

        if (url == null) {
            throw new NullPointerException("No resource named '" + file + "'.");
        }

        return new URLResource(url);
    }

    public static boolean assertListEquals(Object[] expected, Object[] actual)
    {
        if (expected == null || actual == null) {
            notEquals(expected, actual);
        }

        if (!Arrays.equals(expected, actual)) {
            notEquals(expected, actual);
        }

        return true;
    }

    public static boolean assertListEquals(Object[] expected, List<Object> actual)
    {
        if (expected == null || actual == null) {
            notEquals(expected, actual);
        }

        Object[] acarr = actual.toArray(new Object[actual.size()]);
        return assertListEquals(expected, acarr);
    }

    public static void notEquals(Object expected, Object actual)
    {
        throw new AssertionError("Parameters don't match, expected: <"
                + expected + "> actual: <" + actual + ">");
    }
}
