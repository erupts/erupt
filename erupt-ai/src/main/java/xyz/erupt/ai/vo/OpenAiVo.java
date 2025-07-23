package xyz.erupt.ai.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OpenAiVo {

    private String openapi = "3.0.3";

    private Info info;

    private Server[] servers;

    private Map<String, Path> paths = new HashMap<>();

    @Getter
    @Setter
    public static class Info {

        private String title;

        private String version;

        private String description;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Server {

        private String url;

        public Server(String url) {
            this.url = url;
        }

    }

    @Getter
    @Setter
    public static class Path {

        private PathPost post;

    }

    @Getter
    @Setter
    public static class PathPost {

        private String summary;

        private String operationId;

        private RequestBody requestBody;

        //<status code,>
        private Map<Integer, Response> responses = new HashMap<>();

    }


    @Getter
    @Setter
    public static class RequestBody {

        private boolean required;

        private Map<String, ApplicationType> content = new HashMap<>();

    }

    @Getter
    @Setter
    public static class Response {

        private String description;

        private Map<String, ApplicationType> content = new HashMap<>();

    }


    @Getter
    @Setter
    public static class ApplicationType {

        private Schema schema;

    }

    @Getter
    @Setter
    public static class Schema {

        private String type;

        private List<String> required;

        private Map<String, SchemaProperties> properties = new HashMap<>();

    }

    @Getter
    @Setter
    public static class SchemaProperties {

        private String type;

        private String description;

    }

}
