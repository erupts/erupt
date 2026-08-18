package xyz.erupt.ai_rag.core;

import lombok.Builder;
import lombok.Getter;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Getter
@Builder
public class RetrievedChunk {

    private Long chunkId;

    private String document;

    private Integer seq;

    private String text;

    private Double score;

}
