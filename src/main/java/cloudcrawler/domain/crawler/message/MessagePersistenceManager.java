package cloudcrawler.domain.crawler.message;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.trust.link.InheritedLinkTrust;
import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 14.04.13
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
public class MessagePersistenceManager {

    protected Gson gson;

    @Inject
    public MessagePersistenceManager(Gson gson) {
        this.setGson(gson);
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public String sleep(Message message) {
        return gson.toJson(message);
    }

    public Message wakeup(String json) {
        Message message = gson.fromJson(json, Message.class);

        String documentClass = Document.class.getCanonicalName();
        if(message.getAttachmentClassname().equals(documentClass)) {
            DocumentMessage documentMessage = gson.fromJson(json, DocumentMessage.class);
            return documentMessage;
        }

        String linkTrustClass = InheritedLinkTrust.class.getCanonicalName();
        if(message.getAttachmentClassname().equals(linkTrustClass)) {
            InheritLinkTrustMessage pageRankMessage = gson.fromJson(json, InheritLinkTrustMessage.class);
            return pageRankMessage;
        }

        return message;
    }
}
