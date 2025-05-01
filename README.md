这个代码是检查maven引入的jar包是否有问题,包括导入失败，或者重复的jar包，用两种方式：1.依赖声明检查 2.物理文件检查
当你clone完整个项目之后只需要install一下到自己的本地仓库
打开你想检查的项目，在pom文件下导入插件：
```
<plugin>
    <groupId>com.fjutxiake</groupId>
    <artifactId>MavenCheckFordeduplication</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>verify</phase> <!-- 建议绑定到验证阶段,如果要绑定其他阶段请自行填写 -->
            <goals>
                <goal>check</goal> <!-- Mojo目标名check -->
            </goals>
        </execution>
    </executions>
</plugin>
```
再重新clean和verify就会输出信息了
注：如果有问题请提issues
