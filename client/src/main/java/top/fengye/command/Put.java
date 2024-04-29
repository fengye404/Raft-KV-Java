package top.fengye.command;

import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.spi.launcher.DefaultCommand;
import top.fengye.biz.Command;

/**
 * @author: FengYe
 * @date: 2024/4/29 下午11:57
 * @description: Put
 */
@Name("put")
@Summary("put")
public class Put extends DefaultCommand {

    private Command command;


    @Argument(index = 0)
    public void setKey(String key) {

    }

    @Argument(index = 1)
    public void setValue(String value) {

    }

    @Override
    public void run() throws CLIException {
        System.out.println("hello");
    }
}
