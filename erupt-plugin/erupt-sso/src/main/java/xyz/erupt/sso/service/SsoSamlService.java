package xyz.erupt.sso.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.sso.model.EruptSso;

import javax.xml.XMLConstants;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

/**
 * SAML 2.0 web browser SSO, SP side, on nothing but the JDK: the AuthnRequest goes out over
 * the HTTP-Redirect binding, the Response comes back over HTTP-POST and is checked with
 * {@code javax.xml.crypto.dsig} against the one certificate the row trusts.
 *
 * <p>What makes a response acceptable, in order: it parses without a DTD, its status is
 * success, it answers the request erupt issued, it was sent to this consumer, the first
 * assertion (or the whole response) carries an enveloped signature that references the
 * element it sits in and verifies with the IdP certificate, the assertion is inside its
 * validity window and names this SP as its audience. Only then is the subject read.
 *
 * <p>The signature is resolved by ID through the validate context alone, so a second
 * element carrying the same ID elsewhere in the document (signature wrapping) cannot be
 * what the digest was computed over. Encrypted assertions are refused rather than
 * decrypted: that needs an SP key pair and the IdPs that matter can send them in clear.
 *
 * @author YuePeng
 * date 2026-10-08
 */
@Service
public class SsoSamlService {

    public static final String NS_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";

    public static final String NS_ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";

    public static final String NS_METADATA = "urn:oasis:names:tc:SAML:2.0:metadata";

    public static final String BINDING_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";

    public static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

    public static final String CONFIRMATION_BEARER = "urn:oasis:names:tc:SAML:2.0:cm:bearer";

    public static final String NAME_ID_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";

    // the claim names the subject is published under, next to the attributes
    public static final String NAME_ID = "nameId";

    public static final String NAME_ID_FORMAT = "nameIdFormat";

    // IdP and SP clocks are rarely in step; the usual allowance
    private static final Duration CLOCK_SKEW = Duration.ofMinutes(3);

    // a response is a few KB; anything bigger is not a login
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

    // the only transforms a SAML signature legitimately uses: everything else can change what was signed
    private static final Set<String> TRANSFORMS = Set.of(Transform.ENVELOPED,
            CanonicalizationMethod.EXCLUSIVE, CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
            CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS);

    private final XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");

    /**
     * The request id a state travels under: an xs:ID may not start with a digit, so the
     * random state gets a leading underscore.
     */
    public static String requestId(String state) {
        return "_" + state;
    }

    // --------------------------------------------------------- authn request

    /**
     * Where to send the browser: the IdP's single sign-on URL with a deflated, base64
     * AuthnRequest and the state as RelayState. The request is not signed; the SP metadata
     * says so, and the IdPs that insist on signed requests can be told not to.
     */
    public String authnRequestUrl(EruptSso sso, String state, String acsUrl) {
        String xml = "<samlp:AuthnRequest xmlns:samlp=\"" + NS_PROTOCOL + "\" xmlns:saml=\"" + NS_ASSERTION + "\""
                + " ID=\"" + requestId(state) + "\" Version=\"2.0\""
                + " IssueInstant=\"" + Instant.now().truncatedTo(ChronoUnit.SECONDS) + "\""
                + " Destination=\"" + escape(sso.getAuthorizeUrl()) + "\""
                + " ProtocolBinding=\"" + BINDING_POST + "\""
                + " AssertionConsumerServiceURL=\"" + escape(acsUrl) + "\">"
                + "<saml:Issuer>" + escape(sso.getClientId()) + "</saml:Issuer>"
                + "<samlp:NameIDPolicy AllowCreate=\"true\"/>"
                + "</samlp:AuthnRequest>";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("SAMLRequest", Base64.getEncoder().encodeToString(deflate(xml)));
        params.put("RelayState", state);
        return SsoProviderApi.appendQuery(sso.getAuthorizeUrl(), params);
    }

    /**
     * What the IdP has to know about this SP, for import: the entity id, and that assertions
     * are expected signed and posted to the consumer URL.
     */
    public String metadata(EruptSso sso, String acsUrl) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<md:EntityDescriptor xmlns:md=\"" + NS_METADATA + "\" entityID=\"" + escape(sso.getClientId()) + "\">"
                + "<md:SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"true\" protocolSupportEnumeration=\"" + NS_PROTOCOL + "\">"
                + "<md:NameIDFormat>" + NAME_ID_UNSPECIFIED + "</md:NameIDFormat>"
                + "<md:AssertionConsumerService Binding=\"" + BINDING_POST + "\" Location=\"" + escape(acsUrl) + "\" index=\"0\" isDefault=\"true\"/>"
                + "</md:SPSSODescriptor>"
                + "</md:EntityDescriptor>";
    }

    // --------------------------------------------------------------- response

    /**
     * The claims a posted response vouches for, once every check above has passed: the
     * NameID under {@link #NAME_ID}, each attribute under its Name and, when it has one, its
     * FriendlyName, so a row can map {@code email} as well as the full URN.
     */
    public JsonObject identity(EruptSso sso, String samlResponse, String requestId, String acsUrl) {
        X509Certificate certificate = this.parseCertificate(sso.getIdpCertificate());
        Element response = this.parse(decode(samlResponse)).getDocumentElement();
        Erupts.requireTrue(is(response, NS_PROTOCOL, "Response"), invalid("not a SAML response"));
        requireStatus(response);
        requireMatch("InResponseTo", response.getAttribute("InResponseTo"), requestId);
        requireMatch("Destination", response.getAttribute("Destination"), acsUrl);
        requireIssuer(sso, child(response, NS_ASSERTION, "Issuer"));

        Element assertion = child(response, NS_ASSERTION, "Assertion");
        if (null == assertion) {
            Erupts.requireTrue(null == child(response, NS_ASSERTION, "EncryptedAssertion"), I18nTranslate.$translate("sso.saml_encrypted"));
            throw new EruptWebApiRuntimeException(invalid("no assertion"));
        }
        // the assertion's own signature, or the response's enveloping it; a present signature that fails is fatal
        Erupts.requireTrue(this.verifySignature(assertion, certificate) || this.verifySignature(response, certificate),
                I18nTranslate.$translate("sso.saml_unsigned"));
        requireIssuer(sso, child(assertion, NS_ASSERTION, "Issuer"));
        Instant now = Instant.now();
        requireConditions(sso, child(assertion, NS_ASSERTION, "Conditions"), now);
        Element subject = child(assertion, NS_ASSERTION, "Subject");
        Erupts.requireTrue(null != subject, invalid("no subject"));
        requireBearerConfirmation(subject, requestId, acsUrl, now);
        return claims(assertion, subject);
    }

    /**
     * The row's certificate as pasted: PEM armour, line breaks and whitespace are all
     * tolerated, what is left has to be a DER encoded X.509 certificate.
     */
    public X509Certificate parseCertificate(String pem) {
        try {
            String body = StringUtils.defaultString(pem)
                    .replaceAll("-----(BEGIN|END)[^-]*-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(body);
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(der));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.certificate_invalid"), e);
        }
    }

    // -------------------------------------------------------------- signature

    /**
     * Verify the enveloped signature that is a direct child of {@code signed}, if there is
     * one. The reference has to be the element's own ID and nothing else, with no transform
     * beyond enveloped-signature and canonicalisation, and the ID is only resolvable on this
     * element: a lookalike elsewhere in the document is unreachable. False when unsigned.
     */
    private boolean verifySignature(Element signed, X509Certificate certificate) {
        Element signature = child(signed, XMLSignature.XMLNS, "Signature");
        if (null == signature) return false;
        String id = signed.getAttribute("ID");
        Erupts.requireTrue(StringUtils.isNotBlank(id), invalid("signed element has no ID"));
        DOMValidateContext context = new DOMValidateContext(KeySelector.singletonKeySelector(certificate.getPublicKey()), signature);
        context.setIdAttributeNS(signed, null, "ID");
        context.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
        try {
            XMLSignature xmlSignature = signatureFactory.unmarshalXMLSignature(context);
            List<Reference> references = xmlSignature.getSignedInfo().getReferences();
            Erupts.requireTrue(references.size() == 1 && ("#" + id).equals(references.get(0).getURI()),
                    invalid("signature does not cover the element it sits in"));
            for (Transform transform : references.get(0).getTransforms()) {
                Erupts.requireTrue(TRANSFORMS.contains(transform.getAlgorithm()), invalid("transform " + transform.getAlgorithm()));
            }
            Erupts.requireTrue(xmlSignature.validate(context), invalid("signature verification failed"));
            return true;
        } catch (EruptWebApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            // the signature itself is malformed, or its reference does not resolve: say which
            throw new EruptWebApiRuntimeException(invalid(e.getMessage()), e);
        }
    }

    // ----------------------------------------------------------------- checks

    private static void requireStatus(Element response) {
        Element status = child(response, NS_PROTOCOL, "Status");
        Element code = null == status ? null : child(status, NS_PROTOCOL, "StatusCode");
        if (null != code && STATUS_SUCCESS.equals(code.getAttribute("Value"))) return;
        StringBuilder detail = new StringBuilder();
        // the top level code only says which side failed; the nested one and the message say why
        for (Element it = code; null != it; it = child(it, NS_PROTOCOL, "StatusCode")) {
            if (detail.length() > 0) detail.append(' ');
            detail.append(StringUtils.substringAfterLast(it.getAttribute("Value"), ":"));
        }
        Element message = null == status ? null : child(status, NS_PROTOCOL, "StatusMessage");
        if (null != message && StringUtils.isNotBlank(message.getTextContent())) detail.append(": ").append(message.getTextContent().trim());
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.saml_rejected") + " (" + detail + ")");
    }

    private static void requireIssuer(EruptSso sso, Element issuer) {
        if (StringUtils.isBlank(sso.getIssuer()) || null == issuer) return;
        requireMatch("Issuer", issuer.getTextContent().trim(), sso.getIssuer());
    }

    private static void requireConditions(EruptSso sso, Element conditions, Instant now) {
        if (null == conditions) return;
        requireNotBefore(conditions.getAttribute("NotBefore"), now);
        requireNotOnOrAfter(conditions.getAttribute("NotOnOrAfter"), now);
        Element restriction = child(conditions, NS_ASSERTION, "AudienceRestriction");
        if (null == restriction) return;
        for (Element audience : children(restriction, NS_ASSERTION, "Audience")) {
            if (sso.getClientId().equals(audience.getTextContent().trim())) return;
        }
        throw new EruptWebApiRuntimeException(invalid("audience is not " + sso.getClientId()));
    }

    /**
     * Web SSO is the bearer profile: the assertion has to carry a bearer confirmation and
     * whatever that confirmation pins down (recipient, request, time) has to be us, now.
     */
    private static void requireBearerConfirmation(Element subject, String requestId, String acsUrl, Instant now) {
        boolean bearer = false;
        for (Element confirmation : children(subject, NS_ASSERTION, "SubjectConfirmation")) {
            if (!CONFIRMATION_BEARER.equals(confirmation.getAttribute("Method"))) continue;
            bearer = true;
            Element data = child(confirmation, NS_ASSERTION, "SubjectConfirmationData");
            if (null == data) continue;
            requireMatch("Recipient", data.getAttribute("Recipient"), acsUrl);
            requireMatch("InResponseTo", data.getAttribute("InResponseTo"), requestId);
            requireNotBefore(data.getAttribute("NotBefore"), now);
            requireNotOnOrAfter(data.getAttribute("NotOnOrAfter"), now);
        }
        Erupts.requireTrue(bearer, invalid("no bearer subject confirmation"));
    }

    private static void requireNotBefore(String value, Instant now) {
        if (StringUtils.isBlank(value)) return;
        Erupts.requireTrue(!now.plus(CLOCK_SKEW).isBefore(parseInstant(value)), invalid("not yet valid, NotBefore " + value));
    }

    private static void requireNotOnOrAfter(String value, Instant now) {
        if (StringUtils.isBlank(value)) return;
        Erupts.requireTrue(now.minus(CLOCK_SKEW).isBefore(parseInstant(value)), invalid("expired, NotOnOrAfter " + value));
    }

    /**
     * An attribute the IdP chose to send has to agree with us; one it left out is not held
     * against it, the signature and the state are what bind the response to the request.
     */
    private static void requireMatch(String name, String actual, String expected) {
        if (StringUtils.isBlank(actual)) return;
        Erupts.requireTrue(actual.equals(expected), invalid(name + " is " + actual));
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(invalid("bad timestamp " + value));
        }
    }

    // ----------------------------------------------------------------- claims

    private static JsonObject claims(Element assertion, Element subject) {
        JsonObject claims = new JsonObject();
        for (Element statement : children(assertion, NS_ASSERTION, "AttributeStatement")) {
            for (Element attribute : children(statement, NS_ASSERTION, "Attribute")) {
                List<String> values = new ArrayList<>();
                for (Element value : children(attribute, NS_ASSERTION, "AttributeValue")) values.add(value.getTextContent().trim());
                JsonElement value = values.size() == 1 ? new JsonPrimitive(values.get(0)) : array(values);
                claims.add(attribute.getAttribute("Name"), value);
                String friendly = attribute.getAttribute("FriendlyName");
                if (StringUtils.isNotBlank(friendly) && !claims.has(friendly)) claims.add(friendly, value);
            }
        }
        // last, so an attribute that happens to share the name cannot shadow the subject
        Element nameId = child(subject, NS_ASSERTION, "NameID");
        if (null != nameId && StringUtils.isNotBlank(nameId.getTextContent())) {
            claims.addProperty(NAME_ID, nameId.getTextContent().trim());
            if (StringUtils.isNotBlank(nameId.getAttribute("Format"))) claims.addProperty(NAME_ID_FORMAT, nameId.getAttribute("Format"));
        }
        return claims;
    }

    private static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    // -------------------------------------------------------------------- xml

    private Document parse(byte[] bytes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // no DTD, no entity, no include: the document is data from a stranger
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(invalid("malformed XML"), e);
        }
    }

    private static byte[] decode(String samlResponse) {
        Erupts.requireTrue(StringUtils.isNotBlank(samlResponse) && samlResponse.length() <= MAX_RESPONSE_BYTES * 4 / 3, invalid("empty or oversized"));
        try {
            return Base64.getMimeDecoder().decode(samlResponse);
        } catch (IllegalArgumentException e) {
            throw new EruptWebApiRuntimeException(invalid("not base64"));
        }
    }

    private static byte[] deflate(String xml) {
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        deflater.setInput(xml.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer));
        deflater.end();
        return out.toByteArray();
    }

    private static boolean is(Node node, String namespace, String localName) {
        return node instanceof Element && namespace.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName());
    }

    /**
     * The first direct child of that name. Direct on purpose: an element nested deeper is
     * not where the schema puts it, and a descendant search is how a wrapped forgery gets read.
     */
    private static Element child(Element parent, String namespace, String localName) {
        if (null == parent) return null;
        for (Node node = parent.getFirstChild(); null != node; node = node.getNextSibling()) {
            if (is(node, namespace, localName)) return (Element) node;
        }
        return null;
    }

    private static List<Element> children(Element parent, String namespace, String localName) {
        List<Element> list = new ArrayList<>();
        if (null == parent) return list;
        for (Node node = parent.getFirstChild(); null != node; node = node.getNextSibling()) {
            if (is(node, namespace, localName)) list.add((Element) node);
        }
        return list;
    }

    private static String escape(String value) {
        return StringUtils.defaultString(value)
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String invalid(String detail) {
        return I18nTranslate.$translate("sso.saml_invalid") + " (" + detail + ")";
    }

}
