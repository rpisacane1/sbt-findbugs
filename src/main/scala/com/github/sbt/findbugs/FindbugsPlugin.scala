/*
 * This file is part of sbt-findbugs
 *
 * Copyright (c) Joachim Hofer & contributors
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.sbt.findbugs

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object FindbugsPlugin extends AutoPlugin {

  object autoImport extends FindbugsKeys

  import autoImport._ // scalastyle:ignore import.grouping

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    findbugs := FindbugsRunner.runFindBugs(
      findBugsClasspath.value,
      findbugsPathSettings.value,
      findbugsFilterSettings.value,
      findbugsMiscSettings.value,
      javaHome.value,
      streams.value
    ),
    findbugs := findbugs.dependsOn(compile in Compile).value,
    findbugsReportType := Some(FindbugsReport.Xml),
    findbugsPriority := FindbugsPriority.Medium,
    findbugsEffort := FindbugsEffort.Default,
    findbugsReportPath := Some(crossTarget.value / "findbugs" / "report.xml"),
    findbugsMaxMemory := 1024,
    findbugsAnalyzeNestedArchives := true,
    findbugsSortReportByClassNames := false,
    findbugsAnalyzedPath := Seq((classDirectory in Compile).value),
    findbugsAuxiliaryPath := (dependencyClasspath in Compile).value.files,
    findbugsOnlyAnalyze := None,
    findbugsIncludeFilters := None,
    findbugsExcludeFilters := None
  )

  private lazy val findbugsFilterSettings = Def.task {
    FilterSettings(findbugsIncludeFilters.value, findbugsExcludeFilters.value)
  }

  private lazy val findbugsMiscSettings = Def.task {
    MiscSettings(
      findbugsReportType.value,
      findbugsPriority.value,
      findbugsOnlyAnalyze.value,
      findbugsMaxMemory.value,
      findbugsAnalyzeNestedArchives.value,
      findbugsSortReportByClassNames.value,
      findbugsEffort.value
    )
  }

  private lazy val findbugsPathSettings = Def.task {
    PathSettings(findbugsReportPath.value, findbugsAnalyzedPath.value, findbugsAuxiliaryPath.value)
  }

  private lazy val findBugsClasspath = Def.task {
    // TODO is this the best way?
    Project.extract(state.value).currentUnit.unit.plugins.fullClasspath
  }
}
