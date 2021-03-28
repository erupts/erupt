package xyz.erupt.magicapi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.upms.model.base.HyperModel;

import javax.persistence.MappedSuperclass;

/**
 * @author YuePeng
 * date 2021/1/18 17:09
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperFlowProxy.class)
public class HyperFlow extends HyperModel {

    private String status;

}
