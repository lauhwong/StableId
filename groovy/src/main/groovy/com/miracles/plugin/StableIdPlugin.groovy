package com.miracles.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.variant.ApplicationVariantData
import com.miracles.plugin.extension.StableXmlConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by lxw
 */
class StableIdPlugin implements Plugin<Project> {
    private Project mProject

    @Override
    void apply(Project project) {
        mProject = project;
        project.extensions.create('stableXmlConfig', StableXmlConfig)
        project.afterEvaluate {
            def isAppPlugin = project.plugins.hasPlugin('com.android.application')
            if (isAppPlugin) {
                StableXmlConfig pxConfig = project.extensions.getByName('stableXmlConfig')
                project.android.applicationVariants.each { ApplicationVariant variant ->
                    ApplicationVariantData variantData = variant.getVariantData()
                    def scope = variantData.getScope()
                    def prName = scope.getProcessResourcesTask().name
                    def prTask = project.tasks.getByName(prName)
                    def enableAapt2 = false
                    try {
                        enableAapt2 = prTask.isAapt2Enabled()
                    } catch (Exception ex) {
                        log(ex.getMessage())
                    }
                    def inXmlPath = pxConfig.inXmlPath
                    if (!inXmlPath) {
                        inXmlPath = "${project.android.sourceSets.main.res.srcDirs[0]}/values/public.xml"
                    }
                    File inXmlFile
                    if (!inXmlPath || !(inXmlFile = new File(inXmlPath)).exists()) {
                        log('public.xml is not exist,so do nothing...')
                    } else {
                        log("public.xml path is $inXmlPath...")
                        def aaptOpt = prTask.getAaptOptions()
                        if (enableAapt2) {
                            log('aapt2 is enabled,set additional params !')
                            def resultPath = xml2AaptArg(project.android.defaultConfig.applicationId, inXmlFile)
                            aaptOpt.additionalParameters('--stable-ids', resultPath)
                        } else {
                            log('aapt2 is disabled,hook merged resource task !')
                            def mrName = scope.getMergeResourcesTask().name
                            def mrTask = project.tasks.getByName(mrName)
                            mrTask.doLast {
                                def toDir = new File(mrTask.outputDir, "values")
                                project.copy {
                                    from(inXmlFile.getParentFile()) {
                                        include 'public.xml'
                                        rename 'public.xml' 'public.xml'
                                    }
                                    into toDir
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private void log(msg) {
        mProject.logger.error msg
    }

    private def xml2AaptArg(packageId, xmlFile) {
        def result = new File(mProject.buildDir, "xml2AaptArg/public.txt")
        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs()
        }
        def sb = new StringBuilder();
        def nodes = new XmlParser().parse(xmlFile)
        nodes.each {
            sb.append("${packageId}:${it.@type}/${it.@name} = ${it.@id}\n")
        }
        result.write(sb.toString())
        return result.getAbsolutePath()
    }

}
