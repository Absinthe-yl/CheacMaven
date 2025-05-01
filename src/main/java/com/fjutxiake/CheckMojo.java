package com.fjutxiake;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;

import java.util.*;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY)
public class CheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        getLog().info("开始检查项目依赖...");

        // 1. 收集所有依赖并按groupId:artifactId分组
        Map<String, List<Artifact>> dependencyMap = new HashMap<>();
        for (Artifact artifact : project.getArtifacts()) {
            String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
            dependencyMap.computeIfAbsent(key, k -> new ArrayList<>()).add(artifact);
        }

        // 2. 检查并输出重复依赖
        boolean hasDuplicates = false;
        for (Map.Entry<String, List<Artifact>> entry : dependencyMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                hasDuplicates = true;
                getLog().warn("发现重复依赖: " + entry.getKey());
                for (Artifact artifact : entry.getValue()) {
                    getLog().warn(" - 版本: " + artifact.getVersion() +
                            " (scope: " + artifact.getScope() + ")");
                }
            }
        }

        // 3. 输出最终结论
        if (!hasDuplicates) {
            getLog().info("√ 没有发现重复依赖");
        } else {
            getLog().warn("× 发现重复依赖，请处理以上问题");
        }
    }
}