package org.anasoid.azurite.event;

import org.apache.camel.main.Main;

/**
 * Main class that boot the Camel application
 */
public final class MyApplication {


    public static void main(String[] args) throws Exception {
        // use Camels Main class
        Main main = new Main(MyApplication.class);
        // now keep the application running until the JVM is terminated (ctrl + c or sigterm)
        main.run(args);
    }

}
