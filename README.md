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

## 效果演示

### 分配10个序号
![seq10](docs/assets/img/get10.png)

### 数据库中的记录
![seq-table](docs/assets/img/seq-table.png)

### Redis中的记录
![seq-redis](docs/assets/img/seq-redis.png)

## 性能测试结果

待整理 ...

## 开发计划

 - [X] 支持MySQL
 - [ ] 支持PostgreSQL
 - [ ] 支持H2
 - [ ] 支持Oracle
 - [x] 支持Redis
 - [ ] 支持MongoDB
 - [X] Spring Boot 集成

 ## 贡献指南

 代码要求：
  - 统一风格，包含注释、代码缩进等与本项目保持一致
  - 保持代码整洁，比如注释掉的代码块等垃圾代码应该删除
  - 严格控制外部依赖，如果没有必要，请不要引入外部依赖
  - 请在类注释中保留你的作者信息

 ### 数据库支持实现

 1. 参考[`MySqlSynchronizer`](sequence-core/src/main/java/com/power4j/kit/seq/persistent/provider/MySqlSynchronizer.java)的实现方式，实现某个特定数据库后端的支持
 2. 参考[`MySqlSynchronizerTest`](sequence-core/src/test/java/com/power4j/kit/seq/persistent/provider/MySqlSynchronizerTest.java) 编写单元测试，完成自测。如果你的代码能跑通测试，基本上应该没有严重bug

 