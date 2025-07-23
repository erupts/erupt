package xyz.erupt.ai.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McpVo {

    private String homepage_url = "https://www.erupt.xyz";

    private String schema_version = "v1";

    private String name_for_human = "Erupt AI MCP";

    private String name_for_model = "erupt_ai";

    private String description_for_human;

    private String description_for_model;

    private Auth auth = new Auth();

    private Api api = new Api();

    private String contact_email;

    private String legal_info_url;

    @Getter
    @Setter
    public static class Auth {
        private String type = "none";
    }

    @Getter
    @Setter
    public static class Api {

        private String type = "openapi";

        private String url;

    }

}
