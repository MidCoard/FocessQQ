# FocessQQ - 轻量级QQ机器人框架
这是一个基于mirai的机器人框架，是一个面向开发者的框架。

## 命令行

```
--help 显示帮助

--user <id> <password> 设置默认的QQ号和密码

--admin <id> 设置管理员QQ号

--server <port> 设置服务端开放端口 详情见通信

--client <localhost> <localport> <host> <port> <name> 设置客户端开放地址与端口以及链接服务端地址与端口 详情见通信

--client <host> <port> <name> 设置客户端开放地址与端口 详情见通信

--udp <port> 设置UDP端口 详情见通信

--sided 设置单端 详情见通信

--multi 设置可以多个同名客户端链接 详情见通信
```


## 通信

本框架实现了一个简单的通信模块，可以通过网络在运行本框架的不同终端之间进行通信。

如果在启动时加上启动参数 **--server** 则会在相应的端口启动一个服务端。

如果在启动时加上启动参数 **--client** 则会在相应的地址与端口启动一个相应名字的客户端。

如果在启动时加上启动参数 **--udp** 则会在相应的端口启动一个UDP服务端。

如果启动UDP服务端的时候加上启动参数 **--multi** 则会**允许多个相同名字**的客户端链接。

如果在启动时加上启动参数 **--sided** 则**客户端**或者**服务端**都会只含有**链接服务端**或者**链接客户端**的功能。没有 --sided 启动参数时既**可以链接服务端也可以链接客户端**



## 开发

Maven 依赖
```xml
<dependency>
    <groupId>top.focess</groupId>
    <artifactId>focess-qq</artifactId>
    <version>4.0.8.3000</version>
</dependency>
```

Gradle 依赖
```gradle
implementation 'top.focess:focess-qq:4.0.8.3000'
```

开发文档移步本项目[Wiki](https://github.com/MIdCoard/MiraiQQ/wiki)

示例插件 [FocessQQ-SendPlugin](https://github.com/MidCoard/FocessQQ-SendPlugin)

## 许可证
本项目使用 [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html) 许可证

## 注意事项
本框架运行需要指定一个默认机器人。




