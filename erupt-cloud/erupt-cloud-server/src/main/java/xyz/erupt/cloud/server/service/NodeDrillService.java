package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.linq.lambda.LambdaSee;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

/**
 * A drill (PROTOCOL.md §12.4) turned into a plain condition for a v2 node, as {@code EruptService.drillProcess} does
 * for a local erupt: the drill checked against the source erupt's build, the source row read, and the rows of the
 * table kept whose {@code link.joinColumn} holds the source row's {@code link.column}. The drill itself travels in the
 * query as {@code {"erupt": source, "code": code}}, for the {@code linkCondition} the node applies in its own query
 * language.
 *
 * @author YuePeng
 * date 2026/11/05
 */
@Service
public class NodeDrillService {

    // the member of a v2 table query naming the drill, and its members
    public static final String DRILL = "drill";

    public static final String DRILL_ERUPT = "erupt";

    private static final String DRILL_CODE = LambdaSee.method(Drill::code);

    private static final String LINK_ERUPT = LambdaSee.method(Link::linkErupt);

    private static final String COLUMN = LambdaSee.method(Link::column);

    private static final String JOIN_COLUMN = LambdaSee.method(Link::joinColumn);

    private static final String DEFAULT_COLUMN = "id";

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    @Resource
    private CloudNodeRouter cloudNodeRouter;

    /**
     * @param erupt the table read, "nodeName.Erupt"
     * @return the query with the drill's condition, or as it was without drill headers
     */
    public JsonObject apply(String nodeName, String erupt, HttpServletRequest request, JsonObject query) {
        // the drill member is the server's alone: one the browser sends would pick a linkCondition without its condition
        if (query.has(DRILL)) {
            query = query.deepCopy();
            query.remove(DRILL);
        }
        String code = request.getHeader(EruptReqHeader.DRILL);
        if (null == code) return query;
        String value = request.getHeader(EruptReqHeader.DRILL_VALUE);
        String source = request.getHeader(EruptReqHeader.DRILL_SOURCE_ERUPT);
        if (null == value || null == source) {
            throw new EruptWebApiRuntimeException("Drill Header Illegal, Lack: " + EruptReqHeader.DRILL_VALUE + "," + EruptReqHeader.DRILL_SOURCE_ERUPT);
        }
        // a drill links erupts of one node
        String prefix = nodeName + EruptConst.DOT;
        if (!source.startsWith(prefix)) throw new EruptNoLegalPowerException();
        JsonObject link = link(cloudNodeRouter.resolveBuild(source).getEruptModel(), code);
        JsonElement linkErupt = link.get(LINK_ERUPT);
        if (null == linkErupt || !erupt.equals(prefix + linkErupt.getAsString())) {
            throw new EruptWebApiRuntimeException("Illegal erupt from " + code);
        }
        JsonElement joinColumn = link.get(JOIN_COLUMN);
        if (null == joinColumn || joinColumn.getAsString().isBlank()) {
            throw new EruptWebApiRuntimeException("Invalid build of " + source + ": drill " + code + " has no link.joinColumn");
        }
        JsonElement column = link.get(COLUMN);
        JsonElement row = JsonParser.parseString(cloudNodeRouter.proxy(source, HttpMethod.GET, EruptRestPath.ERUPT_DATA
                + EruptRestPath.ERUPT_NAME_HOLDER + "/" + UriUtils.encodePathSegment(value, StandardCharsets.UTF_8), null, VOUCHED));
        // no such row (a node answers null or an empty object): a drill from nowhere lists nothing, it does not fall to NULL
        if (!row.isJsonObject() || row.getAsJsonObject().isEmpty()) throw new EruptNoLegalPowerException();
        JsonElement held = row.getAsJsonObject().get(null == column ? DEFAULT_COLUMN : column.getAsString());
        JsonObject condition = new JsonObject();
        // "author.id" names the reference field and its target's key; a node stores a reference as that key
        condition.addProperty(LambdaSee.field(Condition::getKey), joinColumn.getAsString().split("\\.")[0]);
        // a source row without the value links to the rows without one, never to every row
        if (null == held || held.isJsonNull()) {
            condition.addProperty(LambdaSee.field(Condition::getExpression), QueryExpression.NULL.name());
        } else {
            condition.addProperty(LambdaSee.field(Condition::getExpression), QueryExpression.EQ.name());
            condition.add(LambdaSee.field(Condition::getValue), held);
        }
        JsonObject drilled = query.deepCopy();
        String conditions = LambdaSee.field(TableQuery::getCondition);
        if (!drilled.has(conditions) || !drilled.get(conditions).isJsonArray()) drilled.add(conditions, new JsonArray());
        drilled.getAsJsonArray(conditions).add(condition);
        JsonObject drill = new JsonObject();
        drill.addProperty(DRILL_ERUPT, source.substring(prefix.length()));
        drill.addProperty(DRILL_CODE, code);
        drilled.add(DRILL, drill);
        return drilled;
    }

    // The link of the drill the source erupt declares under the code; a drill it does not declare is refused
    private static JsonObject link(EruptModel source, String code) {
        JsonElement drills = source.getEruptJson().get(LambdaSee.method(Erupt::drills));
        if (null != drills && drills.isJsonArray()) {
            for (JsonElement drill : drills.getAsJsonArray()) {
                if (!drill.isJsonObject()) continue;
                JsonElement own = drill.getAsJsonObject().get(DRILL_CODE);
                JsonElement link = drill.getAsJsonObject().get(LambdaSee.method(Drill::link));
                if (null != own && code.equals(own.getAsString()) && null != link && link.isJsonObject()) return link.getAsJsonObject();
            }
        }
        throw new EruptNoLegalPowerException();
    }

}
