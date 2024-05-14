package top.fengye;

import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import org.apache.commons.collections4.CollectionUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.Console;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author: FengYe
 * @date: 2024/4/29 下午9:36
 * @description: Client
 */
public class Client {
    public static void main(String[] args) {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new DefaultParser())
                    .build();

            System.out.println("客户端启动成功");
            while (true) {
                String input = lineReader.readLine("client> ");

                // 检查用户是否想退出
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    String result = "";
                    String[] s = input.split(" ");
                    if (s.length == 0) {
                        result = "error input";
                    } else {
                        result = CommandProcessor.INSTANCE.processRequest(Arrays.asList(s));
                    }
                    System.out.println("结果: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("错误: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String input) {
        String res = "";
        input = input.strip();


        return res;
    }
}
