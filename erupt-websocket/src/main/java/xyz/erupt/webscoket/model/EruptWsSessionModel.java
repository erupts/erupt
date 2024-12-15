package xyz.erupt.webscoket.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.module.MetaUserinfo;

import javax.websocket.Session;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/12/10 22:51
 */
@Getter
@Setter
public class EruptWsSessionModel {

    private MetaUserinfo metaUserinfo;

    private List<Session> sessions;

    public EruptWsSessionModel(MetaUserinfo metaUserinfo, List<Session> sessions) {
        this.metaUserinfo = metaUserinfo;
        this.sessions = sessions;
    }
}
