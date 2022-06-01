## 1.5.1
`2022-06-01`
- 🔥 升级到Java 11
- 🔥 升级Spring Boot `2.6.8`
- 🔥 插件升级到最新版本

## 1.5.0
`2021-09-03`
- 🔥 支持`H2`(GH PR #48 from lishangbu/master)
- 🔥 新增SequenceRegistry,用于支持动态创建发号器的场景
- 🔥 新增InMemorySeqSynchronizer,用于测试

## 1.4.0
`2020-10-08`
- Fix duplicate key error #5
- 一些依赖升级


## 1.3.0

`2020-08-03`

- 🔥 支持`PostgreSQL`


## 1.2.0

`2020-07-21`

- 🔥 支持`MongoDB`

### 注意事项

- 使用方需要引入MongoDB驱动包


## 1.1.1

`2020-07-13`

- 🔥 支持`Redis`，基于`Lettuce`


### 注意事项

- 使用方需要引入Lettuce驱动包
- 如果需要支持连接池，还需引入`commons-pool2`


## 1.0.1

`2020-07-07`

- 🔥 支持`MySQL`
- 🔥Spring Boot 集成

