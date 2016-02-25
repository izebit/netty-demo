package ru.izebit;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.izebit.configuration.BeanConfiguration;
import ru.izebit.services.HttpServer;

public class Application {
    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.error("неправильно указаны параметры");
            System.out.println("задайте параметры в следующем формате: java -jar application.jar address port");
            System.exit(-1);
        }
        String address = args[0];
        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            LOGGER.error("неправильный задан порт", ex);
            System.exit(-1);
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BeanConfiguration.class);
        HttpServer server = context.getBean(HttpServer.class);

        try {
            server.start(address, port);
        } catch (Exception ex) {
            LOGGER.error("ошибка во время запуска сервера", ex);
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
            }
        });
    }
}
