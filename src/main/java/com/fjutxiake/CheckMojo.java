package com.fjutxiake;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.*;

/**
 * 检查项目中所有JAR包依赖的插件
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY)
public class CheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * 是否包含测试范围的依赖
     */
    @Parameter(property = "includeTestScope", defaultValue = "false")
    private boolean includeTestScope;

    /**
     * 需要排除的groupId模式（逗号分隔）
     */
    @Parameter(property = "excludeGroups")
    private String excludeGroups;

    public void execute() throws MojoExecutionException {
        getLog().info("开始检查项目JAR包依赖...");

        // 获取所有依赖的JAR包
        Set<Artifact> artifacts = project.getArtifacts();
        Set<String> excludedGroups = parseExcludedGroups();

        // 统计JAR包信息
        Map<String, List<Artifact>> jarMap = new HashMap<>();
        int totalJars = 0;
        int problematicJars = 0;

        for (Artifact artifact : artifacts) {
            // 跳过非JAR类型的依赖
            if (!"jar".equals(artifact.getType())) {
                continue;
            }

            // 跳过排除的groupId
            if (excludedGroups.contains(artifact.getGroupId())) {
                continue;
            }

            // 跳过测试范围的依赖（如果配置）
            if (!includeTestScope && "test".equals(artifact.getScope())) {
                continue;
            }

            // 检查JAR文件是否存在
            File jarFile = artifact.getFile();
            String jarStatus = (jarFile != null && jarFile.exists()) ? "存在" : "缺失";

            // 按文件名分组（处理不同版本的同名JAR）
            String jarName = getJarName(artifact);
            jarMap.computeIfAbsent(jarName, k -> new ArrayList<>()).add(artifact);

            // 记录问题JAR
            if ("缺失".equals(jarStatus)) {
                problematicJars++;
            }
            totalJars++;
        }

        // 输出检查结果
        printReport(jarMap, totalJars, problematicJars);
    }

    private Set<String> parseExcludedGroups() {
        if (excludeGroups == null || excludeGroups.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(excludeGroups.split(",")));
    }

    private String getJarName(Artifact artifact) {
        return artifact.getArtifactId() + ".jar";
    }

    private void printReport(Map<String, List<Artifact>> jarMap,
                             int totalJars, int problematicJars) {
        getLog().info("====== JAR包依赖检查报告 ======");
        getLog().info("扫描范围: " + (includeTestScope ? "所有依赖" : "非test范围依赖"));
        getLog().info("总计JAR包: " + totalJars);
        getLog().info("问题JAR包: " + problematicJars);

        // 输出同名不同版本的JAR
        boolean hasDuplicateJars = false;
        for (Map.Entry<String, List<Artifact>> entry : jarMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                hasDuplicateJars = true;
                getLog().warn("发现同名不同版本的JAR: " + entry.getKey());
                for (Artifact artifact : entry.getValue()) {
                    String status = (artifact.getFile() != null && artifact.getFile().exists())
                            ? "存在" : "缺失";
                    getLog().warn(" - " + artifact.getGroupId() + ":" +
                            artifact.getVersion() + " (" + status + ")");
                }
            }
        }

        // 输出缺失的JAR
        if (problematicJars > 0) {
            getLog().warn("====== 缺失的JAR包 ======");
            for (List<Artifact> artifacts : jarMap.values()) {
                for (Artifact artifact : artifacts) {
                    if (artifact.getFile() == null || !artifact.getFile().exists()) {
                        getLog().warn(" - " + artifact.getId());
                    }
                }
            }
        }

        // 总结报告
        if (problematicJars == 0 && !hasDuplicateJars) {
            getLog().info("√ 所有JAR包依赖正常");
        } else {
            getLog().warn("× 发现JAR包依赖问题，请处理以上警告");
        }
    }
}