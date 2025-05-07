import axios, {type R} from "./axios.config.ts";


export interface Chat {
    id: number;
    title: string;
}

export interface ChatMessage {
    id: number;
    senderType: "USER" | "MODEL";
    content: string;
    createTime: string;
    loading: boolean;
    chatId?: number;
}

export interface UserInfo {
    nickname: string;
}

export interface Agent {
    id: number;
    name: string;
    desc: string;
}

export class ChatApi {

    static userInfo(): Promise<UserInfo> {
        return axios.get("erupt-api/userinfo");
    }

    static chats(): Promise<R<Chat[]>> {
        return axios.get("erupt-api/ai/chat/chats");
    }

    static agents(): Promise<R<Agent[]>> {
        return axios.get("erupt-api/ai/agent/list");
    }

    static createChat(title: string): Promise<R<number>> {
        const formData = new URLSearchParams();
        formData.append('title', title);
        return axios.post("erupt-api/ai/chat/create_chat", formData,{
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    }

    static deleteChat(chatId: number): Promise<R<void>> {
        return axios.get("erupt-api/ai/chat/delete_chat", {
            params: {
                chatId
            }
        })
    }

    static messages(chatId: number, size: number, index: number): Promise<R<ChatMessage[]>> {
        return axios.get("erupt-api/ai/chat/messages", {
            params: {
                chatId,
                size,
                index
            }
        })
    }
}