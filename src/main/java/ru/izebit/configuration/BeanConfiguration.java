package ru.izebit.configuration;


import io.netty.channel.ChannelHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.izebit.handlers.FilterHandler;
import ru.izebit.handlers.ResponseHandler;
import ru.izebit.handlers.WorkerHandler;

@Configuration
@ComponentScan(value = {"ru.izebit"})
public class BeanConfiguration {

    @Bean
    @Scope("prototype")
    public FilterHandler getFilterHandler() {
        return new FilterHandler();
    }

    @Bean
    @Scope("prototype")
    public WorkerHandler getWorkHandler() {
        return new WorkerHandler();
    }

    @Bean
    @Scope("prototype")
    public ChannelHandler getResponseHandler() {
        return new ResponseHandler();
    }
}
