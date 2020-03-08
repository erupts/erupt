package xyz.erupt.example.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.core.model.BaseTree;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-03-07
 */
@Erupt(name = "demo", tree = @Tree(id = "id", label = "name", pid = "parent.id",
        linkTable = @Link(eruptClass = News.class, joinColumn = "title")))
@Table(name = "TEST_TREE")
@Entity
public class TreeTest extends BaseTree<TreeTest> {

}
