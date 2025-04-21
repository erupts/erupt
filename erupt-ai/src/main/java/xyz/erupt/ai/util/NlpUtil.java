package xyz.erupt.ai.util;

/**
 * @author YuePeng
 * date 2025/3/3 20:16
 */
public class NlpUtil {

//    public static String geneTitle(String message) {
//        try {
//            // 输入句子
//            String sentence = "Apple is looking at buying U.K. startup for $1 billion.";
//
//            // 分词
//            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
//            String[] tokens = tokenizer.tokenize(sentence);
//
//            // 加载命名实体识别模型
//            InputStream modelInputStream = new FileInputStream("en-ner-organization.bin");
//            TokenNameFinderModel model = new TokenNameFinderModel(modelInputStream);
//            NameFinderME nameFinder = new NameFinderME(model);
//
//            // 识别命名实体
//            Span[] spans = nameFinder.find(tokens);
//
//            // 提取关键信息并生成标题
//            StringBuilder title = new StringBuilder();
//            for (Span span : spans) {
//                title.append(tokens[span.getStart()]);
//            }
//
//            // 如果没有识别到实体，直接提取句子中的关键词
//            if (title.toString().isEmpty()) {
//                for (String token : tokens) {
//                    if (token.length() > 3) { // 简单过滤短词
//                        title.append(token).append(" ");
//                    }
//                }
//            }
//
//            // 输出生成的标题
//            System.out.println("Generated Title: " + title.toString().trim());
//
//            // 关闭模型输入流
//            modelInputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
