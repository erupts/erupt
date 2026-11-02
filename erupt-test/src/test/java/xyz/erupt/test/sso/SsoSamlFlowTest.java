package xyz.erupt.test.sso;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.sso.model.data_proxy.EruptSsoDataProxy;
import xyz.erupt.sso.service.EruptSsoBindService;
import xyz.erupt.sso.service.SsoSamlService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SAML 2.0 end to end against a stand-in IdP: the AuthnRequest erupt sends, the responses
 * it accepts, and, mostly, the ones it must refuse. The IdP is a key pair generated here and
 * a self-signed certificate built by hand, so nothing is stored with the sources.
 */
public class SsoSamlFlowTest extends EruptApplicationTests {

    private static final String CODE = "ut-saml";

    private static final String SP_ENTITY_ID = "urn:erupt:sp";

    private static final String IDP_ENTITY_ID = "urn:test:idp";

    private static final String EMAIL_URN = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

    private static KeyPair idpKeys;

    private static String idpCertificatePem;

    @LocalServerPort
    private int port;

    private final RestTemplate noRedirect = new RestTemplate(new SimpleClientHttpRequestFactory() {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            connection.setInstanceFollowRedirects(false);
        }
    });

    @Resource
    private EruptDao dao;

    @Resource
    private EruptSsoDataProxy ssoDataProxy;

    @Resource
    private EruptSsoBindService bindService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    @BeforeAll
    static void idp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        idpKeys = generator.generateKeyPair();
        X509Certificate certificate = selfSigned(idpKeys);
        idpCertificatePem = "-----BEGIN CERTIFICATE-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(certificate.getEncoded())
                + "\n-----END CERTIFICATE-----\n";
    }

    @BeforeEach
    void provider() {
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = new EruptSso();
            sso.setType(SsoProviderType.SAML);
            sso.setCode(CODE);
            sso.setName("SAML");
            sso.setStatus(true);
            sso.setSort(0);
            sso.setIssuer(IDP_ENTITY_ID);
            sso.setAuthorizeUrl("https://idp.test/sso");
            sso.setClientId(SP_ENTITY_ID);
            sso.setIdpCertificate(idpCertificatePem);
            // matched on a test-only attribute so no real profile field is touched
            sso.setAccountClaim("account");
            sso.setNameClaim("displayName");
            sso.setEmailClaim("email");
            sso.setOpenIdClaim("objectId");
            sso.setAutoCreate(false);
            sso.setSyncProfile(false);
            dao.persist(sso);
        });
    }

    @AfterEach
    void cleanup() {
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one();
            if (null == sso) return;
            dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).list()
                    .forEach(it -> dao.delete(dao.find(EruptSsoBind.class, it.getId())));
            dao.delete(dao.find(EruptSso.class, sso.getId()));
        });
    }

    // ------------------------------------------------------------- request

    @Test
    void authorizeSendsADeflatedAuthnRequestWithTheStateAsRelayState() throws Exception {
        String location = this.authorize();
        assertTrue(location.startsWith("https://idp.test/sso?"), location);
        String state = param(location, "RelayState");
        assertNotNull(state);
        String xml = inflate(Base64.getDecoder().decode(URLDecoder.decode(param(location, "SAMLRequest"), StandardCharsets.UTF_8)));
        assertTrue(xml.contains("ID=\"" + SsoSamlService.requestId(state) + "\""), xml);
        assertTrue(xml.contains("AssertionConsumerServiceURL=\"" + this.acs() + "\""), xml);
        assertTrue(xml.contains("Destination=\"https://idp.test/sso\""), xml);
        assertTrue(xml.contains("<saml:Issuer>" + SP_ENTITY_ID + "</saml:Issuer>"), xml);
        assertTrue(xml.contains("ProtocolBinding=\"" + SsoSamlService.BINDING_POST + "\""), xml);
    }

    @Test
    void metadataNamesTheEntityAndTheConsumer() {
        ResponseEntity<String> resp = noRedirect.getForEntity(this.url("/erupt-api/sso/saml/" + CODE + "/metadata"), String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getHeaders().getContentType().toString().startsWith("application/samlmetadata+xml"), String.valueOf(resp.getHeaders().getContentType()));
        String xml = resp.getBody();
        assertTrue(xml.contains("entityID=\"" + SP_ENTITY_ID + "\""), xml);
        assertTrue(xml.contains("Location=\"" + this.acs() + "\""), xml);
        assertTrue(xml.contains("WantAssertionsSigned=\"true\""), xml);
    }

    // ------------------------------------------------------------ accepted

    @Test
    void aSignedAssertionSignsTheUserInAndTheBindingCarriesTheNameId() throws Exception {
        String state = this.state();
        String back = this.post(this.response(state).signAssertion().build(), state);
        assertNotNull(param(back, "ssoTicket"), back);

        EruptSsoBind bind = this.bind();
        assertEquals("bob@idp", bind.getSubject(), "the NameID is the subject");
        assertEquals("obj-1", bind.getOpenId(), "an attribute mapped as open id");
        assertEquals(this.defaultUser().getId(), bind.getEruptUser().getId());
        Long userId = this.defaultUser().getId();
        assertEquals("bob@x.io", bindService.claim(userId, CODE, EMAIL_URN).orElse(null), "an attribute by its full name");
        assertEquals("bob@x.io", bindService.claim(userId, CODE, "email").orElse(null), "and by its friendly name");
        assertEquals("bob@idp", bindService.claim(userId, CODE, SsoSamlService.NAME_ID).orElse(null));
        assertTrue(bind.getClaims().contains("\"groups\":[\"dev\",\"ops\"]"), "a multi-valued attribute keeps every value: " + bind.getClaims());
    }

    @Test
    void aResponseSignedAsAWholeIsAccepted() throws Exception {
        String state = this.state();
        assertNotNull(param(this.post(this.response(state).signResponse().build(), state), "ssoTicket"));
    }

    @Test
    void theStateIsSpentOnFirstUse() throws Exception {
        String state = this.state();
        String response = this.response(state).signAssertion().build();
        assertNotNull(param(this.post(response, state), "ssoTicket"));
        assertNotNull(param(this.post(response, state), "ssoError"), "a replayed response finds no pending request");
    }

    // ------------------------------------------------------------- refused

    @Test
    void anUnsignedResponseIsRefused() throws Exception {
        this.refused(this.response(this.state()).build());
    }

    @Test
    void aTamperedAssertionIsRefused() throws Exception {
        String state = this.state();
        String signed = this.response(state).signAssertion().build();
        this.refused(signed.replace("bob@x.io", "eve@x.io"), state);
    }

    @Test
    void aResponseSignedByAnotherKeyIsRefused() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.refused(this.response(this.state()).signAssertion().key(generator.generateKeyPair()).build());
    }

    @Test
    void anAssertionForAnotherRequestIsRefused() throws Exception {
        this.refused(this.response(this.state()).inResponseTo("_someone-elses-request").signAssertion().build());
    }

    @Test
    void anExpiredAssertionIsRefused() throws Exception {
        this.refused(this.response(this.state()).notOnOrAfter(Instant.now().minus(10, ChronoUnit.MINUTES)).signAssertion().build());
    }

    @Test
    void anAssertionForAnotherAudienceIsRefused() throws Exception {
        this.refused(this.response(this.state()).audience("urn:someone:else").signAssertion().build());
    }

    @Test
    void anAssertionFromAnotherIssuerIsRefused() throws Exception {
        this.refused(this.response(this.state()).issuer("urn:evil:idp").signAssertion().build());
    }

    @Test
    void anEncryptedAssertionIsRefused() throws Exception {
        this.refused(this.response(this.state()).encrypted().signResponse().build());
    }

    @Test
    void theIdpRefusingIsReported() throws Exception {
        String back = this.post(this.response(this.state()).failed().signResponse().build(), this.lastState);
        assertNull(param(back, "ssoTicket"));
        assertTrue(URLDecoder.decode(param(back, "ssoError"), StandardCharsets.UTF_8).contains("AuthnFailed"), back);
    }

    /**
     * The classic forgery: the signed assertion is kept, intact, somewhere the SP will not
     * look, and a copy with another subject and the same ID is put where the SP does look.
     * The copy carries the signature too; it just was not computed over the copy.
     */
    @Test
    void aWrappedAssertionIsRefused() throws Exception {
        String state = this.state();
        Document document = this.response(state).signAssertion().document();
        Element response = document.getDocumentElement();
        Element legit = first(response, "Assertion");
        Element forged = (Element) legit.cloneNode(true);
        first(first(forged, "Subject"), "NameID").setTextContent("eve@idp");
        Element extensions = document.createElementNS(SsoSamlService.NS_PROTOCOL, "samlp:Extensions");
        response.insertBefore(forged, legit);
        extensions.appendChild(legit);
        response.appendChild(extensions);
        this.refused(serialize(document), state);
        assertNull(this.bind());
    }

    // ---------------------------------------------------------------- form

    @Test
    void aSamlRowNeedsNoSecretButACertificateThatParses() {
        EruptSso draft = new EruptSso();
        draft.setType(SsoProviderType.SAML);
        draft.setAuthorizeUrl("https://idp.test/sso");
        draft.setClientId(SP_ENTITY_ID);
        draft.setIdpCertificate(idpCertificatePem);
        ssoDataProxy.beforeAdd(draft);

        draft.setIdpCertificate("not a certificate");
        assertThrows(EruptWebApiRuntimeException.class, () -> ssoDataProxy.beforeAdd(draft));

        draft.setIdpCertificate(null);
        assertThrows(EruptWebApiRuntimeException.class, () -> ssoDataProxy.beforeAdd(draft));

        EruptSso oauth = new EruptSso();
        oauth.setType(SsoProviderType.GITHUB);
        oauth.setAuthorizeUrl("https://github.com/login/oauth/authorize");
        oauth.setTokenUrl("https://github.com/login/oauth/access_token");
        oauth.setUserInfoUrl("https://api.github.com/user");
        oauth.setClientId("id");
        oauth.setScopes("read:user");
        assertThrows(EruptWebApiRuntimeException.class, () -> ssoDataProxy.beforeAdd(oauth), "an OAuth2 row still needs its secret");
    }

    @Test
    void pickingASamlPresetClearsTheOauthFields() {
        EruptSso draft = new EruptSso();
        draft.setType(SsoProviderType.MICROSOFT_ENTRA_SAML);
        Map<String, Object> form = ssoDataProxy.populateForm(draft, new String[0]);
        assertEquals("https://login.microsoftonline.com/<tenant-id>/saml2", form.get("authorizeUrl"));
        assertTrue(form.containsKey("tokenUrl") && null == form.get("tokenUrl"));
        assertTrue(form.containsKey("scopes") && null == form.get("scopes"));
        assertEquals(SsoSamlService.NAME_ID, form.get("accountClaim"));
        assertEquals("edit.desc=\"Identifier (Entity ID)\"", ssoDataProxy.buildEditExpr(draft, new String[0]).get("clientId"));
    }

    // --------------------------------------------------------------- steps

    private String lastState;

    private String state() {
        lastState = param(this.authorize(), "RelayState");
        return lastState;
    }

    private String authorize() {
        ResponseEntity<Void> resp = noRedirect.getForEntity(this.url("/erupt-api/sso/authorize/" + CODE), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private String post(String samlResponse, String relayState) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("SAMLResponse", Base64.getEncoder().encodeToString(samlResponse.getBytes(StandardCharsets.UTF_8)));
        if (null != relayState) form.add("RelayState", relayState);
        ResponseEntity<Void> resp = noRedirect.postForEntity(this.url("/erupt-api/sso/callback/" + CODE), form, Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private void refused(String samlResponse) {
        this.refused(samlResponse, lastState);
    }

    private void refused(String samlResponse, String relayState) {
        String back = this.post(samlResponse, relayState);
        assertNull(param(back, "ssoTicket"), back);
        assertNotNull(param(back, "ssoError"), back);
    }

    private String acs() {
        return this.url("/erupt-api/sso/callback/" + CODE);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private EruptSsoBind bind() {
        return transactionTemplate.execute(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one();
            return dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).one();
        });
    }

    private EruptUser defaultUser() {
        return dao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
    }

    // ------------------------------------------------------------ stand-in IdP

    private Response response(String state) {
        return new Response(SsoSamlService.requestId(state), this.acs(), eruptUpmsProp.getDefaultAccount());
    }

    /**
     * A Response as an IdP would issue it, valid unless told otherwise, signed where asked.
     */
    private static class Response {

        private final String requestId;

        private final String acs;

        private final String account;

        private String issuer = IDP_ENTITY_ID;

        private String inResponseTo;

        private String audience = SP_ENTITY_ID;

        private Instant notOnOrAfter = Instant.now().plus(5, ChronoUnit.MINUTES);

        private boolean signAssertion;

        private boolean signResponse;

        private boolean encrypted;

        private boolean failed;

        private KeyPair key = idpKeys;

        Response(String requestId, String acs, String account) {
            this.requestId = requestId;
            this.inResponseTo = requestId;
            this.acs = acs;
            this.account = account;
        }

        Response signAssertion() {
            this.signAssertion = true;
            return this;
        }

        Response signResponse() {
            this.signResponse = true;
            return this;
        }

        Response key(KeyPair key) {
            this.key = key;
            return this;
        }

        Response issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        Response inResponseTo(String inResponseTo) {
            this.inResponseTo = inResponseTo;
            return this;
        }

        Response audience(String audience) {
            this.audience = audience;
            return this;
        }

        Response notOnOrAfter(Instant notOnOrAfter) {
            this.notOnOrAfter = notOnOrAfter;
            return this;
        }

        Response encrypted() {
            this.encrypted = true;
            return this;
        }

        Response failed() {
            this.failed = true;
            return this;
        }

        String build() throws Exception {
            return serialize(this.document());
        }

        Document document() throws Exception {
            String now = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
            String status = failed
                    ? "<samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Responder\"><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:AuthnFailed\"/></samlp:StatusCode><samlp:StatusMessage>Bad password</samlp:StatusMessage>"
                    : "<samlp:StatusCode Value=\"" + SsoSamlService.STATUS_SUCCESS + "\"/>";
            String assertion = encrypted
                    ? "<saml:EncryptedAssertion><xenc:EncryptedData xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\"/></saml:EncryptedAssertion>"
                    : "<saml:Assertion ID=\"_assertion1\" Version=\"2.0\" IssueInstant=\"" + now + "\">"
                    + "<saml:Issuer>" + issuer + "</saml:Issuer>"
                    + "<saml:Subject><saml:NameID Format=\"" + SsoSamlService.NAME_ID_UNSPECIFIED + "\">bob@idp</saml:NameID>"
                    + "<saml:SubjectConfirmation Method=\"" + SsoSamlService.CONFIRMATION_BEARER + "\">"
                    + "<saml:SubjectConfirmationData NotOnOrAfter=\"" + notOnOrAfter + "\" Recipient=\"" + acs + "\" InResponseTo=\"" + inResponseTo + "\"/>"
                    + "</saml:SubjectConfirmation></saml:Subject>"
                    + "<saml:Conditions NotBefore=\"" + Instant.now().minus(1, ChronoUnit.MINUTES) + "\" NotOnOrAfter=\"" + notOnOrAfter + "\">"
                    + "<saml:AudienceRestriction><saml:Audience>" + audience + "</saml:Audience></saml:AudienceRestriction></saml:Conditions>"
                    + "<saml:AuthnStatement AuthnInstant=\"" + now + "\"><saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement>"
                    + "<saml:AttributeStatement>"
                    + "<saml:Attribute Name=\"" + EMAIL_URN + "\" FriendlyName=\"email\"><saml:AttributeValue>bob@x.io</saml:AttributeValue></saml:Attribute>"
                    + "<saml:Attribute Name=\"account\"><saml:AttributeValue>" + account + "</saml:AttributeValue></saml:Attribute>"
                    + "<saml:Attribute Name=\"displayName\"><saml:AttributeValue>Bob</saml:AttributeValue></saml:Attribute>"
                    + "<saml:Attribute Name=\"objectId\"><saml:AttributeValue>obj-1</saml:AttributeValue></saml:Attribute>"
                    + "<saml:Attribute Name=\"groups\"><saml:AttributeValue>dev</saml:AttributeValue><saml:AttributeValue>ops</saml:AttributeValue></saml:Attribute>"
                    + "</saml:AttributeStatement>"
                    + "</saml:Assertion>";
            String xml = "<samlp:Response xmlns:samlp=\"" + SsoSamlService.NS_PROTOCOL + "\" xmlns:saml=\"" + SsoSamlService.NS_ASSERTION + "\""
                    + " ID=\"_response1\" Version=\"2.0\" IssueInstant=\"" + now + "\" Destination=\"" + acs + "\" InResponseTo=\"" + inResponseTo + "\">"
                    + "<saml:Issuer>" + issuer + "</saml:Issuer>"
                    + "<samlp:Status>" + status + "</samlp:Status>"
                    + assertion
                    + "</samlp:Response>";
            Document document = parse(xml);
            Element response = document.getDocumentElement();
            if (signAssertion) {
                Element element = first(response, "Assertion");
                sign(element, first(element, "Subject"));
            }
            if (signResponse) sign(response, first(response, "Status"));
            return document;
        }

        /**
         * An enveloped signature over {@code element}, placed before {@code before} as the
         * schema wants it (right after the Issuer).
         */
        private void sign(Element element, Node before) throws Exception {
            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
            Reference reference = factory.newReference("#" + element.getAttribute("ID"), factory.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                            factory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null)), null, null);
            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null), List.of(reference));
            DOMSignContext context = new DOMSignContext(key.getPrivate(), element, before);
            context.setIdAttributeNS(element, null, "ID");
            factory.newXMLSignature(signedInfo, null).sign(context);
        }

    }

    // ---------------------------------------------------------------- utils

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String serialize(Document document) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private static Element first(Element parent, String localName) {
        NodeList list = parent.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getParentNode() == parent) return (Element) list.item(i);
        }
        throw new IllegalStateException("no " + localName + " under " + parent.getLocalName());
    }

    private static String inflate(byte[] deflated) throws Exception {
        Inflater inflater = new Inflater(true);
        inflater.setInput(deflated);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int n = inflater.inflate(buffer);
            if (n == 0 && inflater.needsInput()) break;
            out.write(buffer, 0, n);
        }
        inflater.end();
        return out.toString(StandardCharsets.UTF_8);
    }

    private static String param(String url, String name) {
        int hash = url.indexOf('#');
        String bare = hash < 0 ? url : url.substring(0, hash);
        int mark = bare.indexOf('?');
        String query = mark < 0 ? URI.create(url).getFragment() : bare.substring(mark + 1);
        if (null == query) return null;
        // the login page is a hash route, so its query may sit inside the fragment
        int inner = query.indexOf('?');
        if (mark < 0 && inner >= 0) query = query.substring(inner + 1);
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) return pair.substring(eq + 1);
        }
        return null;
    }

    // ------------------------------------------------- self-signed certificate

    /**
     * A self-signed X.509 v3 certificate for the key pair, DER built by hand: the JDK has no
     * public API to issue one, and a certificate file next to the sources would be a secret
     * nobody wants to maintain.
     */
    private static X509Certificate selfSigned(KeyPair keys) throws Exception {
        byte[] sha256WithRsa = der(0x30, cat(oid("1.2.840.113549.1.1.11"), der(0x05, new byte[0])));
        byte[] name = der(0x30, der(0x31, der(0x30, cat(oid("2.5.4.3"), der(0x0c, "idp".getBytes(StandardCharsets.UTF_8))))));
        byte[] tbs = der(0x30, cat(
                der(0xa0, der(0x02, new byte[]{2})),                                   // version v3
                der(0x02, new byte[]{1}),                                               // serial
                sha256WithRsa, name,
                der(0x30, cat(utcTime("200101000000Z"), utcTime("491231235959Z"))),    // validity
                name,
                keys.getPublic().getEncoded()));                                        // SubjectPublicKeyInfo
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keys.getPrivate());
        signature.update(tbs);
        byte[] certificate = der(0x30, cat(tbs, sha256WithRsa, der(0x03, cat(new byte[]{0}, signature.sign()))));
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificate));
    }

    private static byte[] der(int tag, byte[] body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(tag);
        if (body.length < 128) {
            out.write(body.length);
        } else {
            byte[] length = BigInteger.valueOf(body.length).toByteArray();
            if (length[0] == 0) length = Arrays.copyOfRange(length, 1, length.length);
            out.write(0x80 | length.length);
            out.write(length, 0, length.length);
        }
        out.write(body, 0, body.length);
        return out.toByteArray();
    }

    private static byte[] cat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) out.write(part, 0, part.length);
        return out.toByteArray();
    }

    private static byte[] oid(String dotted) {
        String[] arcs = dotted.split("\\.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(Integer.parseInt(arcs[0]) * 40 + Integer.parseInt(arcs[1]));
        for (int i = 2; i < arcs.length; i++) {
            long value = Long.parseLong(arcs[i]);
            byte[] base128 = new byte[10];
            int n = 0;
            do {
                base128[n++] = (byte) (value & 0x7f);
                value >>= 7;
            } while (value > 0);
            for (int j = n - 1; j >= 0; j--) out.write(base128[j] | (j > 0 ? 0x80 : 0));
        }
        return der(0x06, out.toByteArray());
    }

    private static byte[] utcTime(String value) {
        return der(0x17, value.getBytes(StandardCharsets.US_ASCII));
    }

}
