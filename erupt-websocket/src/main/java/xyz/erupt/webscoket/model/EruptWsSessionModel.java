package xyz.erupt.webscoket.model;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.module.MetaUserinfo;

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
