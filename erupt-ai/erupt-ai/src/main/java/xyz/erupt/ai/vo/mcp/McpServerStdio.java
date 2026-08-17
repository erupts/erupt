package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class McpServerStdio {

    private String command = "";

    private List<String> args = new ArrayList<>();

    private Map<String, String> env = new HashMap<>();
}
