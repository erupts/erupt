package xyz.erupt.ai_staff.channel.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_staff.channel.ChannelMessage;
import xyz.erupt.ai_staff.channel.ChannelRequest;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WeCom (Enterprise WeChat): push via group robot webhook, inbound via a
 * self-built app's message callback (AES-encrypted XML) — replies via the
 * app message-send API.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Component
public class WeComChannel extends StaffChannel {

    private static final String TOKEN_API = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    private static final String SEND_API = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s";

    @Override
    public String code() {
        return "WeCom";
    }

    @Override
    public Object configTemplate() {
        return new Config();
    }

    @Override
    public void push(JsonObject config, String content) {
        String url = str(config, "webhook");
        if (StringUtils.isBlank(url)) throw new EruptWebApiRuntimeException("WeCom webhook not configured");
        JsonObject markdown = new JsonObject();
        markdown.addProperty("content", content);
        JsonObject body = new JsonObject();
        body.addProperty("msgtype", "markdown");
        body.add("markdown", markdown);
        this.post(url, body.toString());
    }

    @Override
    public String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener) {
        String token = str(config, "token");
        String aesKey = str(config, "encodingAesKey");
        String timestamp = request.param("timestamp");
        String nonce = request.param("nonce");
        // URL verification handshake: echo the decrypted echostr in plain text
        if ("GET".equals(request.getMethod())) {
            String echostr = request.param("echostr");
            this.verifySignature(token, timestamp, nonce, echostr, request.param("msg_signature"));
            return this.decrypt(aesKey, echostr);
        }
        String encrypt = xmlTag(request.getBody(), "Encrypt");
        this.verifySignature(token, timestamp, nonce, encrypt, request.param("msg_signature"));
        String plain = this.decrypt(aesKey, encrypt);
        if ("text".equals(xmlTag(plain, "MsgType"))) {
            listener.accept(ChannelMessage.builder()
                    .content(xmlTag(plain, "Content").trim())
                    .sender(xmlTag(plain, "FromUserName"))
                    .replyTo(xmlTag(plain, "FromUserName"))
                    .build());
        }
        // Empty body = no passive reply; the answer is sent asynchronously via the app API
        return "";
    }

    @Override
    public void reply(JsonObject config, ChannelMessage message, String content) {
        String tokenResult = this.httpGet(String.format(TOKEN_API, str(config, "corpId"), str(config, "corpSecret")));
        String accessToken = str(GsonFactory.getGson().fromJson(tokenResult, JsonObject.class), "access_token");
        if (null == accessToken) throw new EruptWebApiRuntimeException("WeCom get token failed: " + tokenResult);
        JsonObject markdown = new JsonObject();
        markdown.addProperty("content", content);
        JsonObject body = new JsonObject();
        body.addProperty("touser", message.getReplyTo());
        body.addProperty("msgtype", "markdown");
        body.addProperty("agentid", str(config, "agentId"));
        body.add("markdown", markdown);
        this.post(String.format(SEND_API, accessToken), body.toString());
    }

    @SneakyThrows
    private void verifySignature(String token, String timestamp, String nonce, String encrypt, String signature) {
        String[] arr = {token, timestamp, nonce, encrypt};
        Arrays.sort(arr);
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(String.join("", arr).getBytes(StandardCharsets.UTF_8));
        if (!hex(digest).equals(signature)) {
            throw new EruptWebApiRuntimeException("WeCom signature mismatch");
        }
    }

    // WeCom envelope: AES-256-CBC, plaintext = random(16) + msgLen(4, BE) + msg + receiveId
    @SneakyThrows
    private String decrypt(String encodingAesKey, String cipherText) {
        byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesKey, 0, 16));
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        int pad = original[original.length - 1];
        if (pad < 1 || pad > 32) pad = 0;
        byte[] content = Arrays.copyOfRange(original, 0, original.length - pad);
        int msgLen = ByteBuffer.wrap(content, 16, 4).getInt();
        return new String(content, 20, msgLen, StandardCharsets.UTF_8);
    }

    private String xmlTag(String xml, String tag) {
        Matcher matcher = Pattern.compile("<" + tag + ">(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</" + tag + ">", Pattern.DOTALL).matcher(xml);
        if (!matcher.find()) throw new EruptWebApiRuntimeException("WeCom callback missing tag: " + tag);
        return matcher.group(1);
    }

    @Getter
    @Setter
    public static class Config {
        // Group robot webhook for outbound push
        private String webhook = "";
        // Self-built app callback credentials
        private String token = "";
        private String encodingAesKey = "";
        // Self-built app credentials, used to reply to messages
        private String corpId = "";
        private String corpSecret = "";
        private String agentId = "";
    }

}
