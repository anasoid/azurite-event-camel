package org.anasoid.azurite.event.routes;

public final class Config {

    private final static String CONF_AZURITE_URL = "AZURITE_URL";
    private final static String CONF_AZURITE_LOG_FOLDER = "AZURITE_LOG_FOLDER";
    private final static String CONF_CAMEL_SEEK_FOLDER = "CAMEL_SEEK_FOLDER";
    public final static String FILE_PATH = System.getenv().getOrDefault(CONF_AZURITE_LOG_FOLDER, "./compose/.logs");
    public final static String FILE_PATH_SEEK = System.getenv().getOrDefault(CONF_CAMEL_SEEK_FOLDER, FILE_PATH);
    public final static String AZURITE_URL = System.getenv().getOrDefault(CONF_AZURITE_URL, "http://localhost:100000");

    //AZURE
    private final static String CONF_AZURE_EVENT_FORMAT = "AZURE_EVENT_FORMAT";// grid or cloud
    public final static String AZURE_EVENT_FORMAT = System.getenv().getOrDefault(CONF_AZURE_EVENT_FORMAT, "grid");

    //TARGET
    private final static String CONF_TARGET_NAME = "TARGET_NAME";
    public final static String TARGET_NAME = System.getenv().getOrDefault(CONF_TARGET_NAME, "AMQP");
    public final static Boolean IS_AMQP_TARGET = TARGET_NAME.equals("AMQP");
    public final static Boolean IS_KAFKA_TARGET = TARGET_NAME.equals("KAFKA");

    private final static String CONF_DEFAULT_TOPIC = "DEFAULT_TOPIC";
    public final static String DEFAULT_TOPIC = System.getenv().getOrDefault(CONF_DEFAULT_TOPIC, "azurite");

    //AMQP
    private final static String CONF_AMQP_URI = "AMQP_URI";
    public static final String AMQP_URI = System.getenv().getOrDefault(CONF_AMQP_URI, "amqp://localhost");
    //KAFKA
    private final static String CONF_KAFKA_BROKER = "KAFKA_BROKER";
    public final static String BROKER = System.getenv().getOrDefault(CONF_KAFKA_BROKER, "localhost:9092");

    private final static String CONF_KAFKA_ADDITIONAL_CONFIG = "KAFKA_ADDITIONAL_CONFIG";
    public final static String KAFKA_ADDITIONAL_CONFIG = System.getenv().getOrDefault(CONF_KAFKA_ADDITIONAL_CONFIG, "");

}
