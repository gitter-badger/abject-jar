//   Copyright 2014 Commonwealth Bank of Australia
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package au.com.cba.omnia.abjectjar

import sbt._
import Keys._
import java.util.jar.Attributes.Name._
import sbt.Defaults._
import sbt.Package.ManifestAttributes

object Plugin extends sbt.Plugin {

  val abjectJar = TaskKey[File]("abject-jar", "Creates a packaged jar in the legacy hadoop format")
 
  def allFiles(base: File): Seq[File] = {
    val finder: PathFinder = PathFinder(base).***
    finder.get
  }

  def compiledFiles(products: Seq[File]):Seq[(File, String)] = {
    products.map(f => {
      val topLevel = List.fromArray(f.list()).map(t => new File(f, t))
      val children = topLevel.flatMap(x => allFiles(x))
      children.flatMap(c => 
        Path.relativizeFile(f, c).map(relativePath => (c, relativePath.toString))
      )
    }).flatten
  }
 
  val abjectJarSettings: Seq[Project.Setting[_]] = inTask(abjectJar)(Seq(
    artifactPath <<= artifactPathSetting(artifact),
    cacheDirectory <<= cacheDirectory / abjectJar.key.label
  )) ++ Seq(
    mainClass in abjectJar <<= mainClass in run in Compile,
    packageOptions in abjectJar <++= (mainClass in abjectJar).map {
      case Some(mainClass) => Seq(ManifestAttributes((MAIN_CLASS, mainClass)))
      case _ => Seq()
    },
    mappings in abjectJar <<= (products in Compile, dependencyClasspath in Compile, internalDependencyClasspath in Runtime).map {(products, classpath, internal) =>
      val thisArtifactMapping = compiledFiles(products)
      val internalDeps = compiledFiles(Attributed.data(internal))
      val deps: Seq[(File, String)] = {
          val allDeps = Build.data(classpath).map(f => (f, (file("lib") / f.name).getPath))
          allDeps.filterNot(_._1 == artifact)
      }
      thisArtifactMapping ++ internalDeps ++ deps
    },
    abjectJar <<= (mappings in abjectJar, artifactPath in abjectJar, packageOptions in abjectJar, cacheDirectory in abjectJar, streams) map {
      (mappings, output, packOpts, cacheDir, s) =>
        val packageConf = new Package.Configuration(mappings, output, packOpts)
        Package(packageConf, cacheDir, s.log)
        output
    }
  )


}
