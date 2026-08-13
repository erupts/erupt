package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * XML codec built on the JDK DOM parser (no extra dependency). A list is a root
 * element wrapping repeated {@code <item>} rows; a single record is a root element
 * whose children are the fields — the {@link EruptFile#single()} flag decides which
 * on read, since the two shapes are otherwise indistinguishable. Field elements
 * carry text; nested elements become nested objects. The parser is hardened against
 * XXE (DTDs and external entities are rejected). Models should be flat.
 *
 * @author YuePeng
 */
public class XmlCodec implements FileCodec {

    private static final String ROOT = "items";

    private static final String ROW = "item";

    @Override
    public FileType type() {
        return FileType.XML;
    }

    @Override
    public boolean accept(String path) {
        return "xml".equals(FileCodec.extension(path));
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return eruptFile.single();
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        List<JsonObject> list = new ArrayList<>();
        Element root = this.parse(content).getDocumentElement();
        if (null == root) return list;
        if (eruptFile.single()) {
            list.add(this.toJson(root));
        } else {
            for (Element row : this.elementChildren(root)) list.add(this.toJson(row));
        }
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        List<String> columns = this.columns(eruptModel);
        if (eruptFile.single()) {
            this.writeRow(sb, ROW, columns, records.isEmpty() ? new JsonObject() : records.get(0), 0);
        } else {
            sb.append('<').append(ROOT).append(">\n");
            for (JsonObject record : records) this.writeRow(sb, ROW, columns, record, 1);
            sb.append("</").append(ROOT).append(">\n");
        }
        return sb.toString();
    }

    private List<String> columns(EruptModel eruptModel) {
        List<String> columns = new ArrayList<>();
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        if (!eruptModel.getEruptFieldMap().containsKey(primaryKey)) columns.add(primaryKey);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) columns.add(fieldModel.getFieldName());
        return columns;
    }

    private void writeRow(StringBuilder sb, String tag, List<String> columns, JsonObject record, int depth) {
        String indent = "  ".repeat(depth);
        sb.append(indent).append('<').append(tag).append(">\n");
        for (String column : columns) {
            JsonElement value = record.get(column);
            if (null == value || value.isJsonNull()) continue;
            sb.append(indent).append("  <").append(column).append('>')
                    .append(this.escape(FileCodec.asText(value)))
                    .append("</").append(column).append(">\n");
        }
        sb.append(indent).append("</").append(tag).append(">\n");
    }

    private JsonObject toJson(Element element) {
        JsonObject json = new JsonObject();
        for (Element child : this.elementChildren(element)) {
            List<Element> grandChildren = this.elementChildren(child);
            if (grandChildren.isEmpty()) {
                json.addProperty(child.getTagName(), child.getTextContent());
            } else {
                json.add(child.getTagName(), this.toJson(child));
            }
        }
        return json;
    }

    private List<Element> elementChildren(Element element) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) elements.add((Element) node);
        }
        return elements;
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private Document parse(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // reject DTDs / external entities to prevent XXE from user-editable files
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.parse_error") + " → " + e.getMessage());
        }
    }

}
