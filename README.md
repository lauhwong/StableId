Stable-Id
=========
固定android资源id，适配aapt和aapt2的资源编译模式。

使用
---

1.Maven仓库的SNAPSHOT版本，在buildscript中加入maven快照仓库，并加入classpath 依赖。
```groovy
repositories {
        maven {
           url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
dependencies {
        classpath 'com.github.lauhwong:stableid:1.0.0-SNAPSHOT'
    }
```
2.在application插件中引入stable-id插件。
```groovy
apply plugin: 'com.miracles.stableid'
```
3.配置public.xml文件，定义固定资源id.可以在build.gradle中做一下的extension dsl配置。
```groovy
stableXmlConfig{
   inXmlPath file("public.xml").absolutePath
}

```
或者，如果不配置该选项默认选择resDir/values/public.xml。
