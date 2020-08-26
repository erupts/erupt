package xyz.demo.erupt.example.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.mongodb.service.EruptMongodbImpl;

import javax.persistence.Id;

/**
 * @author liyuepeng
 * @date 2020-08-16
 */
@EruptDataProcessor(EruptMongodbImpl.class)
@Document(collection = "test")
@Erupt(
        name = "mongoDB数据实现",
        orderBy = "sort"
)
public class MongoDBModel {

    @Id
    @EruptField
    private ObjectId id;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "顺序"),
            edit = @Edit(title = "顺序", notNull = true)
    )
    private Integer sort;
}
