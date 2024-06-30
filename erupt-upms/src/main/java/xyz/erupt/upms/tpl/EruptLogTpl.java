package xyz.erupt.upms.tpl;

import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;

/**
 * @author YuePeng
 * date 2024/6/29 23:26
 */
@EruptTpl(engine = Tpl.Engine.Native)
public class EruptLogTpl {

    @TplAction(value = EruptConst.ERUPT_LOG, path = "/tpl/erupt-log.html")
    public void log() {
    }

}
