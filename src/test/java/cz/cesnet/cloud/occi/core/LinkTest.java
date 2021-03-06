package cz.cesnet.cloud.occi.core;

import com.sun.net.httpserver.Headers;
import cz.cesnet.cloud.occi.TestHelper;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
import cz.cesnet.cloud.occi.exception.RenderingException;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class LinkTest {

    private static final String RESOURCE_PATH = "src/test/resources/rendering/text/";

    @Test
    public void testToText() throws Exception {
        String expected = TestHelper.readFile(RESOURCE_PATH + "link_plain.txt");
        Link link = prepareLink();

        assertEquals(expected, link.toText());
    }

    @Test
    public void testToHeaders() throws Exception {
        Headers headers = new Headers();
        Link link = prepareLink();

        try (BufferedReader br = new BufferedReader(new FileReader(RESOURCE_PATH + "link_headers_categories.txt"))) {
            String line = br.readLine();
            while (line != null) {
                headers.add("Category", line);
                line = br.readLine();
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(RESOURCE_PATH + "link_headers_attributes.txt"))) {
            String line = br.readLine();
            while (line != null) {
                headers.add("X-OCCI-Attribute", line);
                line = br.readLine();
            }
        }

        assertEquals(headers, link.toHeaders());
    }

    private Link prepareLink() throws Exception {
        Kind rel = new Kind(Category.SCHEME_CORE_DEFAULT, Link.TERM_DEFAULT);
        Kind kind = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure/compute#"), "console", "Link to the VM's console", new URI("/console/"), null);
        kind.addRelation(rel);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("occi.network.address"));
        attributes.add(new Attribute("occi.network.gateway"));
        attributes.add(new Attribute("occi.network.allocation"));
        attributes.add(new Attribute("occi.network.state"));
        Mixin m1 = new Mixin(new URI("http://schemas.ogf.org/occi/infrastructure/network#"), "ipnetwork", "IP Network Mixin", new URI("/mixins/ipnetwork/"), attributes);
        Mixin m2 = new Mixin(Category.SCHEME_INFRASTRUCTURE_DEFAULT, "os_tpl", "Operating System Template", new URI("/mixins/os_tpl/"), null);
        Mixin m3 = new Mixin(Category.SCHEME_INFRASTRUCTURE_DEFAULT, "resource_tpl", "Resource Template", new URI("/mixins/resource_tpl/"), null);

        Link link = new Link("87f3bfc3-42d4-4474-b45c-757e55e093e9", kind, "compute1", null);
        link.addMixin(m1);
        link.addMixin(m2);
        link.addMixin(m3);
        link.addAttribute(Compute.ARCHITECTURE_ATTRIBUTE_NAME, "x86");
        link.addAttribute(Compute.HOSTNAME_ATTRIBUTE_NAME, "compute1.example.org");
        link.addAttribute(Compute.MEMORY_ATTRIBUTE_NAME, "1.7");
        link.addAttribute(Compute.SPEED_ATTRIBUTE_NAME, "1.0");
        link.addAttribute(Compute.STATE_ATTRIBUTE_NAME, "active");

        return link;
    }

    @Test
    public void testToInlineText() throws Exception {
        String[] lines = TestHelper.readFile(RESOURCE_PATH + "inline_link_plain.txt").split("\n");

        Kind kind = new Kind(NetworkInterface.SCHEME_DEFAULT, NetworkInterface.TERM_DEFAULT);
        Link link = new Link("456", kind);
        link.setRelation("http://schemas.ogf.org/occi/infrastructure#network");
        link.setTarget("/network/123");
        assertEquals(lines[0], link.toInlineText());

        link.getKind().setLocation(new URI("/link/networkinterface/"));
        assertEquals(lines[1], link.toInlineText());

        link.getKind().setLocation(null);
        link.addAttribute("occi.networkinterface.interface", "eth0");
        link.addAttribute("occi.networkinterface.mac", "00:11:22:33:44:55");
        link.addAttribute("occi.networkinterface.state", "active");
        assertEquals(lines[2], link.toInlineText());

        link.getKind().setLocation(new URI("/link/networkinterface/"));
        assertEquals(lines[3], link.toInlineText());
    }

    @Test
    public void testToInlineHeaders() throws Exception {
        String[] lines = TestHelper.readFile(RESOURCE_PATH + "inline_link_headers.txt").split("\n");
        Headers headers = new Headers();

        Kind kind = new Kind(NetworkInterface.SCHEME_DEFAULT, NetworkInterface.TERM_DEFAULT);
        Link link = new Link("456", kind);
        link.setRelation("http://schemas.ogf.org/occi/infrastructure#network");
        link.setTarget("/network/123");
        headers.add("Link", lines[0]);
        assertEquals(headers, link.toInlineHeaders());

        link.getKind().setLocation(new URI("/link/networkinterface/"));
        headers.clear();
        headers.add("Link", lines[1]);
        assertEquals(headers, link.toInlineHeaders());

        link.getKind().setLocation(null);
        link.addAttribute("occi.networkinterface.interface", "eth0");
        link.addAttribute("occi.networkinterface.mac", "00:11:22:33:44:55");
        link.addAttribute("occi.networkinterface.state", "active");
        headers.clear();
        headers.add("Link", lines[2]);
        assertEquals(headers, link.toInlineHeaders());

        link.getKind().setLocation(new URI("/link/networkinterface/"));
        headers.clear();
        headers.add("Link", lines[3]);
        assertEquals(headers, link.toInlineHeaders());
    }

    @Test
    public void testInvalidToInlineText() throws InvalidAttributeValueException {
        try {
            Kind kind = new Kind(NetworkInterface.SCHEME_DEFAULT, NetworkInterface.TERM_DEFAULT);
            Link link = new Link("456", kind);
            link.setRelation("http://schemas.ogf.org/occi/infrastructure#network");
            link.toInlineText();
            fail();
        } catch (RenderingException ex) {
            //cool
        }

        try {
            Kind kind = new Kind(NetworkInterface.SCHEME_DEFAULT, NetworkInterface.TERM_DEFAULT);
            Link link = new Link("456", kind);
            link.setTarget("/network/123");
            link.toInlineText();
            fail();
        } catch (RenderingException ex) {
            //cool
        }
    }
}
