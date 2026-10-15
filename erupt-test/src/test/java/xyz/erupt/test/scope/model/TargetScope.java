package xyz.erupt.test.scope.model;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.model.edit.RefTargetModel;

import java.util.List;

@Getter
@Setter
// the options are rows: a model field referring to them binds by id
@Erupt(name = "Target", primaryKeyCol = "target")
public class TargetScope {

    @EruptField(edit = @Edit(title = "Target", type = EditType.CHOICE,
            choiceType = @ChoiceType(fetchHandler = TargetScope.Options.class)))
    private Long target;

    @Component
    public static class Options implements ChoiceFetchHandler<Object> {

        @Resource
        private EruptDao eruptDao;

        @Override
        public List<VLModel> fetch(String[] params) {
            return eruptDao.lambdaQuery(RefTargetModel.class).list().stream()
                    .map(it -> new VLModel(it.getId().toString(), it.getName())).toList();
        }
    }

}
