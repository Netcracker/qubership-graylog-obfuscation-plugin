package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;
import com.netcracker.graylog2.plugin.utils.ResourceLoader;
import org.bson.BsonDocument;
import org.bson.Document;
import org.graylog2.database.MongoConnection;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class MongoDatabaseConfigurationProvider implements ConfigurationProvider {

    private static final String JSON_SCHEMA = "configuration/mongodb-configuration-schema.json";

    private static final String CONFIGURATION_COLLECTION = "configuration";

    private static final long COLLECTION_SIZE = 1024 * 1024 * 5;

    private final MongoConnection mongoConnection;

    private final ConfigurationSerializer configurationSerializer;

    private final ResourceLoader resourceLoader;

    private final MongoCollection<Document> configurationCollection;

    @Inject
    public MongoDatabaseConfigurationProvider(MongoConnection mongoConnection,
                                              ConfigurationSerializer configurationSerializer,
                                              ResourceLoader resourceLoader) {
        this.configurationSerializer = configurationSerializer;
        this.mongoConnection = mongoConnection;
        this.configurationCollection = mongoConnection.getMongoDatabase().getCollection(CONFIGURATION_COLLECTION);
        this.resourceLoader = resourceLoader;
    }

    @Inject
    //@PostConstruct
    public void initializeCollections() {
        if (!isCollectionExists(CONFIGURATION_COLLECTION)) {
            ValidationOptions validationOptions = new ValidationOptions()
                    .validationAction(ValidationAction.ERROR)
                    .validationLevel(ValidationLevel.STRICT)
                    .validator(BsonDocument.parse(resourceLoader.getResourceAsString(JSON_SCHEMA)));

            CreateCollectionOptions collectionOptions = new CreateCollectionOptions()
                    .maxDocuments(1)
                    .sizeInBytes(COLLECTION_SIZE)
                    .capped(true)
                    .validationOptions(validationOptions);

            MongoDatabase mongoDatabase = mongoConnection.getMongoDatabase();
            mongoDatabase.createCollection(CONFIGURATION_COLLECTION, collectionOptions);
        }
    }

    @Override
    public synchronized void uploadConfiguration(Configuration configuration) {
        FindIterable<Document> documents = configurationCollection.find();
        Document serializedConfiguration = documents.first();
        if (serializedConfiguration != null) {
            configurationSerializer.deserialize(configuration, serializedConfiguration);
        }
    }

    @Override
    public synchronized void storeConfiguration(Configuration configuration) {
        Map<String, Object> serializedConfiguration = configurationSerializer.serialize(configuration);
        Document document = new Document(serializedConfiguration);
        configurationCollection.insertOne(document);
    }

    @Override
    public synchronized void restoreConfiguration(Configuration configuration) {
        configurationCollection.drop();
        initializeCollections();
        storeConfiguration(configuration);
    }

    private boolean isCollectionExists(String collectionName) {
        MongoDatabase mongoDatabase = mongoConnection.getMongoDatabase();

        for (String listCollectionName : mongoDatabase.listCollectionNames()) {
            if (listCollectionName.equals(collectionName)) {
                return true;
            }
        }

        return false;
    }
}

