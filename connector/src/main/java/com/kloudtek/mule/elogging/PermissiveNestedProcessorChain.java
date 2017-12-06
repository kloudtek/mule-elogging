package com.kloudtek.mule.elogging;

import org.mule.DefaultMuleEvent;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.processor.chain.NestedProcessorChain;

public class PermissiveNestedProcessorChain extends NestedProcessorChain {
    private MuleEvent event;
    private MessageProcessor chain;

    public PermissiveNestedProcessorChain(MuleEvent event, MuleContext muleContext, MessageProcessor chain) {
        super(event, muleContext, chain);
        this.event = event;
        this.chain = chain;
    }

    public MessageProcessor getChain() {
        return chain;
    }

    @Override
    public MuleMessage process() throws Exception {
        MuleEvent muleEvent;
        muleEvent = new DefaultMuleEvent(event.getMessage(), event);
        return chain.process(muleEvent).getMessage();
    }
}
