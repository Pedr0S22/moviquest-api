package dev.Pedro.movies_api.logging;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class MongoLogAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setApplicationContext'");
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'append'");
    }

}
