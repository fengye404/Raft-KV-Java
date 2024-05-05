# Raft-KV-Java

灵感来自于 MIT6.824，使用 Java 编写的 Raft 协议的 KV 玩具。通过 Vert.x 构建了全异步模型。

## 支持的特性

- [x] 投票选举
- [x] 日志复制
- [x] 日志恢复
- [x] KV状态机
- [x] 线性一致读
- [x] 状态持久化
- [ ] 日志压缩

## 使用方法

分别打包 server 和 client

```shell
mvn clean package
```

需要提前给单独给每个server编写json配置文件。
举例: 现在需要启动 3 个 raft server，则目录如下
```shell
├─node1
├──config.json
├──server-1.0.jar
├─node2
├──config.json
├──server-1.0.jar
└─node3
├──config.json
├──server-1.0.jar
```
config.json 内容:
```json
{
  "nodeId": "node1",
  "host": "127.0.0.1",
  "port": 8080,
  "peers": [
    {
      "nodeId": "node2",
      "host": "127.0.0.1",
      "port": 8081
    },
    {
      "nodeId": "node3",
      "host": "127.0.0.1",
      "port": 8082
    }
  ]
}
```
依次启动
```shell
java -jar server-1.0.jar -r true -c ./config.json
# -r 参数表示是否从文件恢复启动，默认为 true
# -c 参数表示 json 配置文件的路径，默认为 ./config.json
```


