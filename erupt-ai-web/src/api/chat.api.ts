import axios from "axios";


export interface R<T> {
    data: T;
    message: string | null;
    success: boolean;
}

export interface ChatMessage {
    id: number;
    chatId: number;
    senderType: "USER" | "MODEL";
    content: string;
    createTime: string;
    tokens: number;
    loading: boolean;
}

export class ChatApi {

    static chats() {
        return axios.get("/erupt-api/ai/chat/chats?chatId=1");
    }

    static messages(chatId: number, size: number): Promise<R<ChatMessage[]>> {
        return axios.get(`/erupt-api/ai/chat/messages?chatId=${chatId}&size=${size}`)
    }
}