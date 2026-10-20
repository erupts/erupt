package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Vis;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.controller.vis.EruptBoardController.VisBoardGroupCommand;
import xyz.erupt.core.controller.vis.EruptCalendarController.VisCalendarDateCommand;
import xyz.erupt.core.controller.vis.EruptGanttController.GanttDateCommand;
import xyz.erupt.core.controller.vis.EruptGanttController.GanttLinkCommand;
import xyz.erupt.core.controller.vis.EruptGanttController.GanttLinksCommand;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;
import xyz.erupt.linq.lambda.LambdaSee;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The writes of a node erupt's board, calendar and gantt views, done by the server for every node: each is a row
 * read through the node's get, given the dragged values and saved whole through its update ({@link NodeCellService}),
 * so a node serves no view endpoint. Gantt links are read off the dependencyField of the rows the same way: a
 * reference to one predecessor, or a list of them, each its key or an object carrying it.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Service
public class NodeVisService {

    private static final String ID = "id";

    @Resource
    private NodeCellService nodeCellService;

    /**
     * @param resolve which view write the request is
     * @param body    the command the view sends
     */
    public R<?> answer(EruptRouter.Resolve resolve, String erupt, EruptBuildModel build, byte[] body) {
        EruptModel model = EruptModel.remote(erupt, erupt.substring(0, erupt.lastIndexOf(EruptConst.DOT)), build);
        JsonObject command = JsonParser.parseString(new String(body, StandardCharsets.UTF_8)).getAsJsonObject();
        return switch (resolve) {
            case VIS_BOARD_GROUP -> boardGroup(erupt, build, model, command);
            case VIS_CALENDAR_DATE -> calendarDate(erupt, build, model, command);
            case VIS_GANTT_DATE -> ganttDate(erupt, build, model, command);
            case VIS_GANTT_LINKS -> ganttLinks(erupt, model, command);
            case VIS_GANTT_LINK -> ganttLink(erupt, build, model, command);
            default -> throw new IllegalArgumentException(resolve.name());
        };
    }

    private R<?> boardGroup(String erupt, EruptBuildModel build, EruptModel model, JsonObject command) {
        Vis vis = vis(model, text(command.get(LambdaSee.field(VisBoardGroupCommand::getVisCode))));
        // a view the model does not declare changes nothing, as on a local erupt
        if (null == vis) return R.ok();
        Map<String, JsonElement> values = new LinkedHashMap<>();
        values.put(vis.boardView().groupField(), value(command, LambdaSee.field(VisBoardGroupCommand::getGroupValue)));
        // a card dropped into another swimlane changes the second grouping as well
        JsonElement swimlaneChanged = command.get(LambdaSee.field(VisBoardGroupCommand::isSwimlaneChanged));
        if (null != swimlaneChanged && swimlaneChanged.isJsonPrimitive() && swimlaneChanged.getAsBoolean()
                && !vis.boardView().swimlaneField().isEmpty()) {
            values.put(vis.boardView().swimlaneField(), value(command, LambdaSee.field(VisBoardGroupCommand::getSwimlaneValue)));
        }
        return nodeCellService.patch(erupt, build, model, text(command.get(LambdaSee.field(VisBoardGroupCommand::getPk))), values);
    }

    private R<?> calendarDate(String erupt, EruptBuildModel build, EruptModel model, JsonObject command) {
        Vis vis = vis(model, text(command.get(LambdaSee.field(VisCalendarDateCommand::getVisCode))));
        if (null == vis) return R.ok();
        Map<String, JsonElement> values = new LinkedHashMap<>();
        values.put(vis.calendarView().dateField(), value(command, LambdaSee.field(VisCalendarDateCommand::getDate)));
        JsonElement end = value(command, LambdaSee.field(VisCalendarDateCommand::getEndDate));
        if (!vis.calendarView().endDateField().isEmpty() && !end.isJsonNull()) values.put(vis.calendarView().endDateField(), end);
        return nodeCellService.patch(erupt, build, model, text(command.get(LambdaSee.field(VisCalendarDateCommand::getPk))), values);
    }

    private R<?> ganttDate(String erupt, EruptBuildModel build, EruptModel model, JsonObject command) {
        Vis vis = ganttVis(model, text(command.get(LambdaSee.field(GanttDateCommand::getVisCode))));
        Map<String, JsonElement> values = new LinkedHashMap<>();
        values.put(vis.ganttView().startDateField(), value(command, LambdaSee.field(GanttDateCommand::getStartDate)));
        values.put(vis.ganttView().endDateField(), value(command, LambdaSee.field(GanttDateCommand::getEndDate)));
        return nodeCellService.patch(erupt, build, model, text(command.get(LambdaSee.field(GanttDateCommand::getPk))), values);
    }

    private R<Map<String, List<String>>> ganttLinks(String erupt, EruptModel model, JsonObject command) {
        Vis vis = ganttVis(model, text(command.get(LambdaSee.field(GanttLinksCommand::getVisCode))));
        Map<String, List<String>> links = new LinkedHashMap<>();
        JsonElement pks = command.get(LambdaSee.field(GanttLinksCommand::getPks));
        String field = vis.ganttView().dependencyField();
        if (StringUtils.isBlank(field) || null == pks || !pks.isJsonArray()) return R.ok(links);
        for (JsonElement pk : pks.getAsJsonArray()) {
            JsonObject row = nodeCellService.read(erupt, model, text(pk));
            if (null != row) links.put(text(pk), predecessors(row.get(field)));
        }
        return R.ok(links);
    }

    private R<?> ganttLink(String erupt, EruptBuildModel build, EruptModel model, JsonObject command) {
        Vis vis = ganttVis(model, text(command.get(LambdaSee.field(GanttLinkCommand::getVisCode))));
        String field = vis.ganttView().dependencyField();
        if (StringUtils.isBlank(field)) return R.error(I18nTranslate.$translate("erupt.gantt.dependency_unconfigured"));
        String pk = text(command.get(LambdaSee.field(GanttLinkCommand::getPk)));
        JsonElement predecessorPk = value(command, LambdaSee.field(GanttLinkCommand::getPredecessorPk));
        String predecessor = text(predecessorPk);
        if (Objects.equals(pk, predecessor)) return R.error(I18nTranslate.$translate("erupt.gantt.dependency_self"));
        JsonObject row = nodeCellService.read(erupt, model, pk);
        if (null == row) return R.error(I18nTranslate.$translate("erupt.gantt.record_not_found"));
        JsonElement remove = command.get(LambdaSee.field(GanttLinkCommand::isRemove));
        boolean removing = null != remove && remove.isJsonPrimitive() && remove.getAsBoolean();
        // the new predecessor must not already depend on this row, directly or through others
        if (!removing && reaches(erupt, model, field, predecessor, pk, new HashSet<>())) {
            return R.error(I18nTranslate.$translate("erupt.gantt.dependency_cycle"));
        }
        JsonElement current = row.get(field);
        JsonElement next;
        if (null != current && current.isJsonArray()) {
            JsonArray keys = new JsonArray();
            for (JsonElement it : current.getAsJsonArray()) if (!predecessor.equals(key(it))) keys.add(it);
            if (!removing) keys.add(predecessorPk);
            next = keys;
        } else {
            next = removing ? JsonNull.INSTANCE : predecessorPk;
        }
        Map<String, JsonElement> values = new LinkedHashMap<>();
        values.put(field, next);
        return nodeCellService.patch(erupt, build, model, pk, values);
    }

    // true when walking the predecessor chain from `from` meets the row `target`
    private boolean reaches(String erupt, EruptModel model, String field, String from, String target, Set<String> seen) {
        if (from.equals(target)) return true;
        if (!seen.add(from)) return false;
        JsonObject row = nodeCellService.read(erupt, model, from);
        if (null == row) return false;
        for (String predecessor : predecessors(row.get(field))) {
            if (reaches(erupt, model, field, predecessor, target, seen)) return true;
        }
        return false;
    }

    // the keys a dependency value holds: one reference or a list, each its key or an object carrying it
    private static List<String> predecessors(JsonElement value) {
        List<String> keys = new ArrayList<>();
        if (null == value || value.isJsonNull()) return keys;
        for (JsonElement it : value.isJsonArray() ? value.getAsJsonArray() : List.of(value)) {
            String key = key(it);
            if (null != key) keys.add(key);
        }
        return keys;
    }

    private static String key(JsonElement reference) {
        return text(reference.isJsonObject() ? reference.getAsJsonObject().get(ID) : reference);
    }

    private static Vis ganttVis(EruptModel model, String code) {
        return Optional.ofNullable(vis(model, code))
                .orElseThrow(() -> new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.view_not_found")));
    }

    private static Vis vis(EruptModel model, String code) {
        return Arrays.stream(model.getErupt().vis()).filter(it -> it.code().equals(code)).findFirst().orElse(null);
    }

    private static JsonElement value(JsonObject command, String key) {
        return Optional.ofNullable(command.get(key)).orElse(JsonNull.INSTANCE);
    }

    private static String text(JsonElement value) {
        return null == value || value.isJsonNull() ? null : value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }

}
