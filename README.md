# JAVA 序号工具包 Sequence
[![CodeFactor](https://www.codefactor.io/repository/github/power4j/sequence/badge/master)](https://www.codefactor.io/repository/github/power4j/sequence/overview/master)
[![codebeat badge](https://codebeat.co/badges/abec5291-8b69-408d-8515-ed65951f7eb5)](https://codebeat.co/projects/github-com-power4j-sequence-master)
[![codecov](https://codecov.io/gh/power4j/sequence/branch/master/graph/badge.svg)](https://codecov.io/gh/power4j/sequence)
[![travis-ci](https://travis-ci.org/power4j/sequence.svg)](https://travis-ci.org/github/power4j/sequence)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.power4j.kit/sequence/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.power4j.kit/sequence)

使用场景

- 业务需要根据一定的规则生成序号，如：
  - 连续性：序号连续自增地进行分配
  - 相关性：多租户隔离、按年、月、日分区
  - 特定格式: 序号输出的格式灵活可控
- 技术上需要满足
  - 高性能
  - 线程安全
  - 适用性好
  - 配置简单、容易集成

## 项目说明

- ***JDK 版本要求: `JDK8+`*** 

## 核心概念

- 号池(`SeqPool`): 一种设施，可以提供有限或者无限的序号。
- 同步器(`SeqSynchronizer`): 负责与某种后端(如数据库)交互,更新序号的当前值。
- 取号器(`SeqHolder`): 负责缓存从后端批量取出的序号，然后交给本地的号池来管理。
- 分区(`Partition`): 后端存储在保存序号信息时，序号的名称+分区表示一条唯一的记录,分区可以是静态的(一个字符串常量),也可以是动态分区(一个返回分区名称的函数)。分区并不是将序号的取值进行划分，主要是用于多租户等需要二次分类的场景以及避免号池耗尽。

注意事项:
- 如果使用动态分区,不宜变化过于频繁，比如用系统时间作为分区。比较合适的是按年份、月份进行分区。
- 每一个分区的序号取值独立的，比如按年份进行分区，那么每年都有`Long.MAX`个序号可用。

## 使用方法


引入依赖
```xml
<dependency>
    <groupId>com.power4j.kit</groupId>
    <artifactId>sequence-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

开启配置(JDBC)
```yaml
power4j:
  sequence:
    # 数据同步使用的后端支持(如: mysql,oracle,redis),跟你实际使用的数据源类型对应
    backend: mysql
```

也可以使用Redis，则配置方式为

```yaml
power4j:
  sequence:
    backend: redis
    lettuce-uri: "redis://127.0.0.1"
```

使用

```java
@RestController
@SpringBootApplication
public class SequenceExampleApplication {
    @Autowired
    private Sequence<Long> sequence;
    public static void main(String[] args) {
        SpringApplication.run(SequenceExampleApplication.class, args);
    }

    @GetMapping("/seq")
    public List<String> getSequence(@RequestParam(required = false) Integer size) {
        size = (size == null || size <= 0) ? 10 : size;
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(sequence.nextStr());
        }
        return list;
    }
}
```

## 性能测试结果
> 测试环境用的Redis,MySQL等外部服务都是默认安装，没有调整参数。

测试环境一: 腾讯云1核(2.6G)2G `CentOS7.6`
```shell
# Detecting actual CPU count: 1 detected
# JMH version: 1.23
# VM version: JDK 1.8.0_252, OpenJDK 64-Bit Server VM, 25.252-b09
# VM invoker: /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.252.b09-2.el7_8.x86_64/jre/bin/java
# VM options: -server -Xms32m -Xmx128m -Xmn64m -XX:CMSInitiatingOccupancyFraction=82 -Xss256k -XX:LargePageSizeInBytes=64m

Benchmark                      Mode  Cnt         Score          Error  Units
LettuceSeqHolderBench.getSeq  thrpt    3   3682420.572 ±  1058146.431  ops/s
LongSeqPoolBench.getSeq       thrpt    3  75273390.592 ± 72699905.480  ops/s
MongoSeqHolderBench.getSeq    thrpt    3   1305400.597 ±  8767008.399  ops/s
MySqlSeqHolderBench.getSeq    thrpt    3     90594.876 ±    67488.430  ops/s

```
> 分数: 裸奔: 7500万, redis: 360万,MySQL:9万,MongoDB 130万 

- 分数看看就好,纯跑分没什么意义。

## 效果演示

### 分配10个序号
![seq10](docs/assets/img/get10.png)

### 数据库中的记录
![seq-table](docs/assets/img/seq-table.png)

### Redis中的记录
![seq-redis](docs/assets/img/seq-redis.png)

## 开发计划

 - [X] 支持MySQL
 - [ ] 支持PostgreSQL
 - [ ] 支持H2
 - [ ] 支持Oracle
 - [x] 支持Redis
 - [x] 支持MongoDB
 - [X] Spring Boot 集成

 ## 贡献指南

 代码要求：
  - 统一风格，包含注释、代码缩进等与本项目保持一致
  - 保持代码整洁，比如注释掉的代码块等垃圾代码应该删除
  - 严格控制外部依赖，如果没有必要，请不要引入外部依赖
  - 请在类注释中保留你的作者信息，请不要害羞

 ### 数据库支持实现

 1. 参考[`MySqlSynchronizer`](sequence-core/src/main/java/com/power4j/kit/seq/persistent/provider/MySqlSynchronizer.java)的实现方式，实现某个特定数据库后端的支持
 2. 参考[`MySqlSynchronizerTest`](sequence-core/src/test/java/com/power4j/kit/seq/persistent/provider/MySqlSynchronizerTest.java) 编写单元测试，完成自测。如果你的代码能跑通测试，基本上应该没有严重bug

 