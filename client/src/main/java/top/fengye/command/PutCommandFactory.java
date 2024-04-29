package top.fengye.command;

import io.vertx.core.spi.launcher.DefaultCommandFactory;

/**
 * @author: FengYe
 * @date: 2024/4/30 上午12:07
 * @description: PutCommandFactory
 */
public class PutCommandFactory extends DefaultCommandFactory<Put> {
    public PutCommandFactory() {
        super(Put.class);
    }
}
