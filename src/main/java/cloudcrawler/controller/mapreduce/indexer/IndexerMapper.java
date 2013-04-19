package cloudcrawler.controller.mapreduce.indexer;

import cloudcrawler.controller.mapreduce.AbstractMapper;
import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.Message;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import cloudcrawler.domain.indexer.Indexer;
import cloudcrawler.domain.ioc.CloudCrawlerModule;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * The page linkTrust mapper is producing pagerank messages. It inherits his
 * own page linkTrust to linked documents by sending messages to the linked websites.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 *
 */
public class IndexerMapper extends AbstractMapper {

    protected Indexer indexer;

    protected Injector injector;

    public IndexerMapper() throws Exception{
        //since the Crawling mapper is instanciated in hadoop
        //we inject the dependecies by our own
        injector = CloudCrawlerModule.getConfiguredInjector();
        this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        this.setIndexer(injector.getInstance(Indexer.class));
        indexer.flush();
    }

    /**
     * @param indexer
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     *
     * @param key
     * @param value
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        try {
            Message message = this.messageManager.wakeup(value.toString());

            if(!message.getAttachmentClassname().equals(Document.class.getCanonicalName())) {
                System.out.println("Can not handle message with class "+message.getAttachmentClassname());
                return;
            }

            DocumentMessage currentDocumentCrawlMessage = (DocumentMessage) message;
            Document        crawled = currentDocumentCrawlMessage.getAttachment();

                //keep the message
            postMessage(key,currentDocumentCrawlMessage,context);

            if(crawled.getCrawlingState() == Document.CRAWLING_STATE_CRAWLED) {
                //and index it
                indexer.index(crawled);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
